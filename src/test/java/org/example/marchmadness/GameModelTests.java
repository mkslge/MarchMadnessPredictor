package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Team;
import org.example.marchmadness.simulation.winner.DeterministicWinnerSelectionStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Game model always produces distinct winner and loser participants")
    void gameModel_winnerAndLoserAreAlwaysDifferentParticipants() throws Exception {
        Team team1 = new Team(DEFAULT_TEST_YEAR, "Maryland", 1);
        Team team2 = new Team(DEFAULT_TEST_YEAR, "Arkansas St.", 16);

        Game game = new Game(team1, team2);
        JsonNode node = TestJsonUtil.parseJson(game.toJson());

        String winner = node.get("winner").get("Team").asText();
        String loser = node.get("loser").get("Team").asText();

        assertTrue(winner.equals("Maryland") || winner.equals("Arkansas St."));
        assertTrue(loser.equals("Maryland") || loser.equals("Arkansas St."));
        assertNotEquals(winner, loser);
    }

    @Test
    @DisplayName("Game model recalculates outcome after participant changes")
    void gameModel_addTeamsAndSettersRecalculateResult() {
        Team alpha = new Team(DEFAULT_TEST_YEAR, "Alpha", 1);
        Team beta = new Team(DEFAULT_TEST_YEAR, "Beta", 2);
        Team gamma = new Team(DEFAULT_TEST_YEAR, "Gamma", 3);
        Team delta = new Team(DEFAULT_TEST_YEAR, "Delta", 4);

        Game game = new Game(alpha, beta);
        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());

        game.addTeams(gamma, delta);
        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());

        game.setTeam1(alpha);
        game.setTeam2(beta);
        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());
    }

    @Test
    @DisplayName("Game model supports deterministic winner strategy constructor")
    void gameModel_supportsDeterministicStrategyConstructor() {
        Team team1 = new Team(DEFAULT_TEST_YEAR, "Team One", 1);
        Team team2 = new Team(DEFAULT_TEST_YEAR, "Team Two", 16);
        Game game = new Game(team1, team2, new DeterministicWinnerSelectionStrategy());

        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());
    }

    @Test
    @DisplayName("Game model rejects null winner strategy")
    void gameModel_rejectsNullWinnerStrategy() {
        Team team1 = new Team(DEFAULT_TEST_YEAR, "Team One", 1);
        Team team2 = new Team(DEFAULT_TEST_YEAR, "Team Two", 16);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Game(team1, team2, null)
        );
        assertTrue(exception.getMessage().contains("Winner selection strategy cannot be null"));
    }
}
