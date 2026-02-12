package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Region {
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

    private String json;
    private final int NUM_GAMES = 15;
    private final int NUM_ROUNDS = 4;

    private final int FIRST_ROUND_INDEX = 0;
    private final int SECOND_ROUND_INDEX = 1;
    private final int THIRD_ROUND_INDEX = 2;
    private final int FOURTH_ROUND_INDEX = 3;



    public Region(RegionType region, int year) {
        this.year = year;
        this.region = region;
        teams = new ArrayList<>();
        games = new ArrayList<>();

        this.run();
    }

    public void run() {
        this.initRounds();

        this.setTeams();
        this.runGames();
        this.setJsonProperties();
    }

    private void setTeams() {
        mapTeams();
        for(Team team : teams) {
            team.setYear(this.year);
        }

    }

    /*Must be done AFTER running runGames*/
    private void setJsonProperties() {
       fieldOf64 = new ArrayList<>(games.get(0));
       fieldOf32 = new ArrayList<>(games.get(1));
       sweet16 = new ArrayList<>(games.get(2));
       elite8 = new ArrayList<>(games.get(3));
    }

    private void mapTeams() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            String resourcePath = "datasets/" + year + "/" + region.toString() + ".json";
            System.out.println("Loading: " + resourcePath);
            InputStream is = Region.class
                    .getClassLoader()
                    .getResourceAsStream(resourcePath);

            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            JsonNode rootNode = mapper.readTree(is);
            JsonNode regionArray = rootNode.get("Region");

            teams = mapper.readValue(
                    regionArray.toString(),
                    new TypeReference<ArrayList<Team>>() {}
            );
        } catch(Exception exc) {
            exc.printStackTrace();
            System.err.print(exc.toString());
            throw new IllegalArgumentException("Error in mapTeams");
        }


    }

    private void initRounds() {

        for(int i = 0; i < NUM_ROUNDS;i++) {
            games.add(new ArrayList<>());
        }
    }

    private void runGames() {
        runFirstRound();
        runRest();
    }

    private void runFirstRound() {
        int numFirstRoundGames = 8;
        for(int i = 0; i < numFirstRoundGames;i++) {
            games.get(0).add(new Game(teams.get(i), teams.get(NUM_GAMES - i)));
        }
    }

    private void runRest() {
        int numGames = 4;
        for(int r = SECOND_ROUND_INDEX; r < NUM_ROUNDS;r++) {

            for(int g = 0; g < numGames;g++) {
                int [] prevRoundIndicies = getTeamIndicies(g);


                Team t1 = games.get(r - 1).get(prevRoundIndicies[0]).getWinner();
                Team t2 = games.get(r - 1).get(prevRoundIndicies[1]).getWinner();
                games.get(r).add(new Game(t1, t2));
            }
            numGames /= 2;
        }

        setWinner();

    }

    private int [] getTeamIndicies(int newIndex) {
        return new int []{newIndex * 2, newIndex *2 + 1};
    }

    public void printRegion() {
        for (Team team : teams) {
            System.out.println(team.getName());
        }
    }

    private void setWinner() {
        regionWinner = games.get(FOURTH_ROUND_INDEX).get(0).winner;
    }
    public Team getWinner() {

        return regionWinner;
    }

    public String toJson() {
        if(json != null) {
            return json;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString;
            try {
                json = mapper.writeValueAsString(this);
            } catch(IOException exc) {
                exc.printStackTrace();
                return null;
            }
            return json;
        }
    }


}
