package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.simulation.BracketSimulator;
import org.example.marchmadness.simulation.BracketSimulatorFactory;
import org.example.marchmadness.simulation.DeterministicBracketSimulator;
import org.example.marchmadness.simulation.SimulationMode;
import org.example.marchmadness.simulation.StochasticBracketSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationStrategyTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Simulator factory returns deterministic simulator for deterministic mode")
    void simulatorFactory_returnsDeterministicSimulator() {
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.DETERMINISTIC);
        assertTrue(simulator instanceof DeterministicBracketSimulator);
        assertEquals(SimulationMode.DETERMINISTIC, simulator.getMode());
    }

    @Test
    @DisplayName("Simulator factory returns stochastic simulator for stochastic mode")
    void simulatorFactory_returnsStochasticSimulator() {
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.STOCHASTIC);
        assertTrue(simulator instanceof StochasticBracketSimulator);
        assertEquals(SimulationMode.STOCHASTIC, simulator.getMode());
    }

    @Test
    @DisplayName("Simulator factory rejects null mode")
    void simulatorFactory_rejectsNullMode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BracketSimulatorFactory.create(null)
        );
        assertTrue(exception.getMessage().contains("Simulation mode cannot be null"));
    }

    @Test
    @DisplayName("Deterministic simulator is reproducible for the same year")
    void deterministicSimulator_isReproducible() {
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.DETERMINISTIC);

        Bracket first = simulator.simulate(DEFAULT_TEST_YEAR);
        Bracket second = simulator.simulate(DEFAULT_TEST_YEAR);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getChampionName(), second.getChampionName());
    }

    @Test
    @DisplayName("Stochastic simulator returns a valid bracket with champion from final four")
    void stochasticSimulator_returnsValidBracket() throws Exception {
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.STOCHASTIC);
        Bracket bracket = simulator.simulate(DEFAULT_TEST_YEAR);

        assertNotNull(bracket);
        assertNotNull(bracket.getChampion());

        Set<String> allowedChampions = new HashSet<>();
        JsonNode finalFourNode = TestJsonUtil.parseJson(bracket.toJson()).get("finalFour");
        allowedChampions.add(finalFourNode.get("east").get("Team").asText());
        allowedChampions.add(finalFourNode.get("west").get("Team").asText());
        allowedChampions.add(finalFourNode.get("south").get("Team").asText());
        allowedChampions.add(finalFourNode.get("midwest").get("Team").asText());

        assertTrue(allowedChampions.contains(bracket.getChampionName()));
    }
}
