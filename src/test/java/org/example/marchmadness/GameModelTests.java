package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Team;
import org.example.marchmadness.simulation.GameSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("GameSimulator produces distinct winner and loser participants")
    void gameSimulator_winnerAndLoserAreAlwaysDifferentParticipants() throws Exception {
        Team team1 = new Team(DEFAULT_TEST_YEAR, "Maryland", 1);
        Team team2 = new Team(DEFAULT_TEST_YEAR, "Arkansas St.", 16);

        Game game = new GameSimulator().simulate(team1, team2);
        JsonNode node = TestJsonUtil.parseJson(game.toJson());

        String winner = node.get("winner").get("Team").asText();
        String loser = node.get("loser").get("Team").asText();

        assertTrue(winner.equals("Maryland") || winner.equals("Arkansas St."));
        assertTrue(loser.equals("Maryland") || loser.equals("Arkansas St."));
        assertNotEquals(winner, loser);
    }

    @Test
    @DisplayName("Game model stores a completed matchup result without simulating")
    void gameModel_storesCompletedMatchupResult() {
        Team alpha = new Team(DEFAULT_TEST_YEAR, "Alpha", 1);
        Team beta = new Team(DEFAULT_TEST_YEAR, "Beta", 2);

        Game game = new Game(alpha, beta, 75.0, alpha, beta);

        assertEquals(alpha, game.getTeam1());
        assertEquals(beta, game.getTeam2());
        assertEquals(75.0, game.getOddsOutOf100());
        assertEquals(alpha, game.getWinner());
        assertEquals(beta, game.getLoser());
    }

    @Test
    @DisplayName("GameSimulator uses injected winner strategy")
    void gameSimulator_usesInjectedWinnerStrategy() {
        Team team1 = new Team(DEFAULT_TEST_YEAR, "Team One", 1);
        Team team2 = new Team(DEFAULT_TEST_YEAR, "Team Two", 16);
        Game game = new GameSimulator((firstTeam, secondTeam, odds) -> firstTeam).simulate(team1, team2);

        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());
        assertEquals(team1, game.getWinner());
        assertEquals(team2, game.getLoser());
    }

    @Test
    @DisplayName("GameSimulator rejects null winner strategy")
    void gameSimulator_rejectsNullWinnerStrategy() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new GameSimulator(null)
        );
        assertTrue(exception.getMessage().contains("Winner selection strategy cannot be null"));
    }

    @Test
    @DisplayName("GameSimulator rejects null teams")
    void gameSimulator_rejectsNullTeams() {
        Team team = new Team(DEFAULT_TEST_YEAR, "Team One", 1);
        GameSimulator gameSimulator = new GameSimulator();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameSimulator.simulate(team, null)
        );
        assertTrue(exception.getMessage().contains("Teams cannot be null"));
    }
}
