package org.example.marchmadness.simulation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.models.Team;
import org.example.marchmadness.simulation.winner.StochasticWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RegionSimulator {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int NUM_TEAMS = 16;
    private static final int NUM_ROUNDS = 4;
    private static final int FIRST_ROUND_INDEX = 0;
    private static final int SECOND_ROUND_INDEX = 1;
    private static final int FOURTH_ROUND_INDEX = 3;

    private final GameSimulator gameSimulator;

    public RegionSimulator() {
        this(new StochasticWinnerSelectionStrategy());
    }

    /**
     * Command: Create a reusable region simulator.
     * Preconditions: Winner selection strategy is non-null.
     * Postconditions: Simulator can evaluate any region/year using the provided strategy.
     */
    public RegionSimulator(WinnerSelectionStrategy winnerSelectionStrategy) {
        if (winnerSelectionStrategy == null) {
            throw new IllegalArgumentException("Winner selection strategy cannot be null");
        }
        this.gameSimulator = new GameSimulator(winnerSelectionStrategy);
    }

    /**
     * Command: Simulate a full regional bracket.
     * Preconditions: Region/year are valid and the source dataset exists.
     * Postconditions: Returns a completed region result with all rounds and winner populated.
     */
    public Region simulate(RegionType regionType, int year) {
        if (regionType == null) {
            throw new IllegalArgumentException("Region type cannot be null");
        }

        List<Team> teams = loadTeams(regionType, year);
        List<List<Game>> games = initRounds();
        runFirstRound(teams, games);
        runRest(games);

        Team regionWinner = games.get(FOURTH_ROUND_INDEX).get(0).getWinner();
        return new Region(
                regionType,
                year,
                games.get(0),
                games.get(1),
                games.get(2),
                games.get(3),
                regionWinner
        );
    }

    /**
     * Command: Load region teams and stamp them with the tournament year.
     * Preconditions: Dataset file exists and is readable.
     * Postconditions: Returns the 16 seeded teams for this region/year.
     */
    private List<Team> loadTeams(RegionType regionType, int year) {
        String resourcePath = "datasets/" + year + "/" + regionType + ".json";
        try (InputStream inputStream = RegionSimulator.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            JsonNode rootNode = OBJECT_MAPPER.readTree(inputStream);
            JsonNode regionArray = rootNode.get("Region");
            if (regionArray == null || !regionArray.isArray()) {
                throw new IllegalArgumentException(
                        "Dataset file `" + resourcePath + "` is missing a valid `Region` array"
                );
            }
            List<Team> teams = OBJECT_MAPPER.readValue(regionArray.toString(), new TypeReference<>() {});

            if (teams.size() != NUM_TEAMS) {
                throw new IllegalArgumentException("Expected " + NUM_TEAMS + " teams but found " + teams.size());
            }

            for (Team team : teams) {
                team.setYear(year);
            }
            return teams;
        } catch (IOException exc) {
            throw new IllegalArgumentException("Error while mapping teams for " + resourcePath, exc);
        }
    }

    /**
     * Command: Initialize empty round containers for a region simulation.
     * Preconditions: None.
     * Postconditions: Returns one mutable game list per region round.
     */
    private List<List<Game>> initRounds() {
        List<List<Game>> games = new ArrayList<>();
        for (int i = 0; i < NUM_ROUNDS; i++) {
            games.add(new ArrayList<>());
        }
        return games;
    }

    /**
     * Command: Simulate the opening round matchups.
     * Preconditions: Teams contains 16 seeded teams and games contains all round containers.
     * Postconditions: First-round game list contains 8 simulated games.
     */
    private void runFirstRound(List<Team> teams, List<List<Game>> games) {
        int numFirstRoundGames = 8;
        for (int i = 0; i < numFirstRoundGames; i++) {
            games.get(FIRST_ROUND_INDEX).add(
                    gameSimulator.simulate(teams.get(i), teams.get(NUM_TEAMS - 1 - i))
            );
        }
    }

    /**
     * Command: Simulate rounds 2 through 4 from previous round winners.
     * Preconditions: First round has already been simulated.
     * Postconditions: Remaining rounds are simulated.
     */
    private void runRest(List<List<Game>> games) {
        int numGames = 4;
        for (int roundIndex = SECOND_ROUND_INDEX; roundIndex < NUM_ROUNDS; roundIndex++) {
            for (int gameIndex = 0; gameIndex < numGames; gameIndex++) {
                int[] prevRoundIndices = getTeamIndices(gameIndex);
                Team team1 = games.get(roundIndex - 1).get(prevRoundIndices[0]).getWinner();
                Team team2 = games.get(roundIndex - 1).get(prevRoundIndices[1]).getWinner();
                games.get(roundIndex).add(gameSimulator.simulate(team1, team2));
            }
            numGames /= 2;
        }
    }

    /**
     * Command: Return source game indices used to build the next-round game.
     * Preconditions: `newIndex` is non-negative.
     * Postconditions: Returns two indices pointing to prior-round winners.
     */
    private int[] getTeamIndices(int newIndex) {
        return new int[]{newIndex * 2, newIndex * 2 + 1};
    }
}
