package org.example.marchmadness;

import org.example.marchmadness.models.Game;
import org.example.marchmadness.util.SimulationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationUtilTests {

    @Test
    @DisplayName("Simulation utility simulates game across different years when both teams exist")
    void simulationUtil_simulatesCrossYearGame() {
        Game simulatedGame = SimulationUtil.simulateGame("Connecticut", 2024, "Houston", 2023);
        assertNotNull(simulatedGame);
        assertNotNull(simulatedGame.getWinner());
        assertNotNull(simulatedGame.getLoser());
    }

    @Test
    @DisplayName("Simulation utility throws for unavailable years with available-year context")
    void simulationUtil_throwsForUnavailableYear() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SimulationUtil.simulateGame("Connecticut", 1900, "Houston", 2023)
        );
        assertTrue(exception.getMessage().contains("Year 1900 is not available"));
        assertTrue(exception.getMessage().contains("Available years"));
    }

    @Test
    @DisplayName("Simulation utility throws when a team does not exist in provided year")
    void simulationUtil_throwsForTeamMissingInYear() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SimulationUtil.simulateGame("NotARealTeam", 2024, "Houston", 2023)
        );
        assertTrue(exception.getMessage().contains("does not exist in year 2024"));
    }

    @Test
    @DisplayName("Simulation utility throws when team names are blank")
    void simulationUtil_throwsForBlankTeamName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SimulationUtil.simulateGame(" ", 2024, "Houston", 2023)
        );
        assertTrue(exception.getMessage().contains("must be non-empty"));
    }
}
