package org.example.marchmadness;

import org.example.marchmadness.controllers.BracketController;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.Game;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BracketControllerTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Autowired
    private BracketController bracketController;

    @Test
    @DisplayName("Controller endpoint path returns bracket for 2024 and remains healthy")
    void endpointPath_2024ReturnsBracket() {
        Bracket bracket = bracketController.generateBracket(DEFAULT_TEST_YEAR);
        assertNotNull(bracket);
        assertNotNull(bracket.getChampion());
    }

    @Test
    @DisplayName("Controller years endpoint returns available years list")
    void controllerYearsEndpoint_returnsAvailableYears() {
        List<Integer> years = bracketController.getAvailableYears();
        assertTrue(years.contains(2023));
        assertTrue(years.contains(2024));
    }

    @Test
    @DisplayName("Controller game simulation endpoint path returns a simulated cross-year game")
    void controllerGameSimulationEndpoint_returnsSimulatedGame() {
        Game game = bracketController.simulateGame("Connecticut", 2024, "Houston", 2023);
        assertNotNull(game);
        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());
    }
}
