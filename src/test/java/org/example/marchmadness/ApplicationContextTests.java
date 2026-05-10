package org.example.marchmadness;

import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.simulation.BracketSimulator;
import org.example.marchmadness.simulation.BracketSimulatorFactory;
import org.example.marchmadness.simulation.SimulationMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = MarchMadnessApplication.class)
class ApplicationContextTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Context loads and bracket simulator returns a champion")
    void contextLoads() {
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.STOCHASTIC);
        Bracket bracket = simulator.simulate(DEFAULT_TEST_YEAR);
        assertNotNull(bracket);
        assertNotNull(bracket.getChampion());
    }
}
