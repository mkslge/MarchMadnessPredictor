package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.marchmadness.simulation.winner.StochasticWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

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
    private final WinnerSelectionStrategy winnerSelectionStrategy;

    public Region(RegionType region, int year) {
        this(region, year, new StochasticWinnerSelectionStrategy());
    }

    /**
     * Command: Create a region simulation with an explicit winner selection strategy.
     * Preconditions: Region type and strategy are non-null.
     * Postconditions: Region is initialized and simulation is run.
     */
    public Region(RegionType region, int year, WinnerSelectionStrategy winnerSelectionStrategy) {
        if (region == null) {
            throw new IllegalArgumentException("Region type cannot be null");
        }
        if (winnerSelectionStrategy == null) {
            throw new IllegalArgumentException("Winner selection strategy cannot be null");
        }
        this.year = year;
        this.region = region;
        this.teams = new ArrayList<>();
        this.games = new ArrayList<>();
        this.winnerSelectionStrategy = winnerSelectionStrategy;
        run();
    }

    /**
     * Command: Simulate a full regional bracket.
     * Preconditions: Region/year are valid and the source dataset exists.
     * Postconditions: Rounds and `regionWinner` are populated for this region.
     */
    public final void run() {
        initRounds();
        setTeams();
        runGames();
        setJsonProperties();
    }

    /**
     * Command: Load region teams and stamp them with the tournament year.
     * Preconditions: Dataset file exists and is readable.
     * Postconditions: `teams` contains the 16 seeded teams for this region/year.
     */
    private void setTeams() {
        mapTeams();
        for (Team team : teams) {
            team.setYear(this.year);
        }
    }

    /**
     * Command: Copy round results into JSON-facing properties.
     * Preconditions: `runGames()` completed successfully.
     * Postconditions: `fieldOf64`, `fieldOf32`, `sweet16`, and `elite8` are populated.
     */
    private void setJsonProperties() {
        fieldOf64 = new ArrayList<>(games.get(0));
        fieldOf32 = new ArrayList<>(games.get(1));
        sweet16 = new ArrayList<>(games.get(2));
        elite8 = new ArrayList<>(games.get(3));
    }

    /**
     * Command: Map teams from the region JSON resource.
     * Preconditions: `year` and `region` point to a valid dataset path.
     * Postconditions: `teams` is replaced with parsed team data from resource JSON.
     */
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

    /**
     * Command: Initialize empty round containers for this region simulation.
     * Preconditions: None.
     * Postconditions: `games` contains one list per round.
     */
    private void initRounds() {
        games.clear();
        for (int i = 0; i < NUM_ROUNDS; i++) {
            games.add(new ArrayList<>());
        }
    }

    /**
     * Command: Run all games in this regional bracket.
     * Preconditions: Teams are loaded and rounds initialized.
     * Postconditions: Each round has simulated games and a final winner exists.
     */
    private void runGames() {
        runFirstRound();
        runRest();
    }

    /**
     * Command: Simulate the opening round matchups.
     * Preconditions: `teams` contains 16 seeded teams.
     * Postconditions: First-round game list contains 8 simulated games.
     */
    private void runFirstRound() {
        int numFirstRoundGames = 8;
        for (int i = 0; i < numFirstRoundGames; i++) {
            games.get(FIRST_ROUND_INDEX).add(
                    new Game(teams.get(i), teams.get(NUM_TEAMS - 1 - i), winnerSelectionStrategy)
            );
        }
    }

    /**
     * Command: Simulate rounds 2 through 4 from previous round winners.
     * Preconditions: First round has already been simulated.
     * Postconditions: Remaining rounds are simulated and `regionWinner` is set.
     */
    private void runRest() {
        int numGames = 4;
        for (int r = SECOND_ROUND_INDEX; r < NUM_ROUNDS; r++) {
            for (int g = 0; g < numGames; g++) {
                int[] prevRoundIndices = getTeamIndices(g);
                Team t1 = games.get(r - 1).get(prevRoundIndices[0]).getWinner();
                Team t2 = games.get(r - 1).get(prevRoundIndices[1]).getWinner();
                games.get(r).add(new Game(t1, t2, winnerSelectionStrategy));
            }
            numGames /= 2;
        }

        setWinner();
    }

    /**
     * Command: Return source game indices used to build the next-round game.
     * Preconditions: `newIndex` is non-negative.
     * Postconditions: Returns two indices pointing to prior-round winners.
     */
    private int[] getTeamIndices(int newIndex) {
        return new int[]{newIndex * 2, newIndex * 2 + 1};
    }

    /**
     * Command: Promote the regional champion from the Elite 8 result.
     * Preconditions: Final regional game has been simulated.
     * Postconditions: `regionWinner` references the winning team.
     */
    private void setWinner() {
        regionWinner = games.get(FOURTH_ROUND_INDEX).get(0).getWinner();
    }

    public Team getWinner() {
        return regionWinner;
    }

    /**
     * Command: Serialize this region into a JSON string.
     * Preconditions: Region has been initialized.
     * Postconditions: Returns a JSON representation of the current region state.
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to serialize Region to JSON", exc);
        }
    }
}
