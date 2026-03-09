package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Region {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final int NUM_TEAMS = 16;
    private static final int NUM_ROUNDS = 4;
    private static final int FIRST_ROUND_INDEX = 0;
    private static final int SECOND_ROUND_INDEX = 1;
    private static final int FOURTH_ROUND_INDEX = 3;

    private List<Team> teams;
    private List<List<Game>> games;

    @JsonProperty
    private final RegionType region;

    @JsonProperty
    private List<Game> fieldOf64;

    @JsonProperty
    private List<Game> fieldOf32;

    @JsonProperty
    private List<Game> sweet16;

    @JsonProperty
    private List<Game> elite8;

    @JsonProperty
    private Team regionWinner;

    private final int year;

    public Region(RegionType region, int year) {
        this.year = year;
        this.region = region;
        this.teams = new ArrayList<>();
        this.games = new ArrayList<>();
        run();
    }

    public final void run() {
        initRounds();
        setTeams();
        runGames();
        setJsonProperties();
    }

    private void setTeams() {
        mapTeams();
        for (Team team : teams) {
            team.setYear(this.year);
        }
    }

    private void setJsonProperties() {
        fieldOf64 = new ArrayList<>(games.get(0));
        fieldOf32 = new ArrayList<>(games.get(1));
        sweet16 = new ArrayList<>(games.get(2));
        elite8 = new ArrayList<>(games.get(3));
    }

    private void mapTeams() {
        String resourcePath = "datasets/" + year + "/" + region + ".json";
        try (InputStream is = Region.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            JsonNode rootNode = OBJECT_MAPPER.readTree(is);
            JsonNode regionArray = rootNode.get("Region");
            teams = OBJECT_MAPPER.readValue(regionArray.toString(), new TypeReference<>() {});

            if (teams.size() != NUM_TEAMS) {
                throw new IllegalArgumentException("Expected " + NUM_TEAMS + " teams but found " + teams.size());
            }
        } catch (IOException exc) {
            throw new IllegalArgumentException("Error while mapping teams for " + resourcePath, exc);
        }
    }

    private void initRounds() {
        games.clear();
        for (int i = 0; i < NUM_ROUNDS; i++) {
            games.add(new ArrayList<>());
        }
    }

    private void runGames() {
        runFirstRound();
        runRest();
    }

    private void runFirstRound() {
        int numFirstRoundGames = 8;
        for (int i = 0; i < numFirstRoundGames; i++) {
            games.get(FIRST_ROUND_INDEX).add(new Game(teams.get(i), teams.get(NUM_TEAMS - 1 - i)));
        }
    }

    private void runRest() {
        int numGames = 4;
        for (int r = SECOND_ROUND_INDEX; r < NUM_ROUNDS; r++) {
            for (int g = 0; g < numGames; g++) {
                int[] prevRoundIndices = getTeamIndices(g);
                Team t1 = games.get(r - 1).get(prevRoundIndices[0]).getWinner();
                Team t2 = games.get(r - 1).get(prevRoundIndices[1]).getWinner();
                games.get(r).add(new Game(t1, t2));
            }
            numGames /= 2;
        }

        setWinner();
    }

    private int[] getTeamIndices(int newIndex) {
        return new int[]{newIndex * 2, newIndex * 2 + 1};
    }

    private void setWinner() {
        regionWinner = games.get(FOURTH_ROUND_INDEX).get(0).getWinner();
    }

    public Team getWinner() {
        return regionWinner;
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to serialize Region to JSON", exc);
        }
    }
}
