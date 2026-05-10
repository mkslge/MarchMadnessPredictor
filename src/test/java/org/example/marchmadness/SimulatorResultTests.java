package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.simulation.BracketSimulator;
import org.example.marchmadness.simulation.BracketSimulatorFactory;
import org.example.marchmadness.simulation.FinalFourSimulator;
import org.example.marchmadness.simulation.RegionSimulator;
import org.example.marchmadness.simulation.SimulationMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorResultTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("RegionSimulator returns all four region results with winners")
    void regionSimulator_returnsFourNonNullRegionResultsWithWinners() {
        RegionSimulator regionSimulator = new RegionSimulator();

        Region east = regionSimulator.simulate(RegionType.EAST, DEFAULT_TEST_YEAR);
        Region midwest = regionSimulator.simulate(RegionType.MIDWEST, DEFAULT_TEST_YEAR);
        Region south = regionSimulator.simulate(RegionType.SOUTH, DEFAULT_TEST_YEAR);
        Region west = regionSimulator.simulate(RegionType.WEST, DEFAULT_TEST_YEAR);

        assertNotNull(east);
        assertNotNull(midwest);
        assertNotNull(south);
        assertNotNull(west);

        assertNotNull(east.getWinner());
        assertNotNull(midwest.getWinner());
        assertNotNull(south.getWinner());
        assertNotNull(west.getWinner());
    }

    @Test
    @DisplayName("FinalFourSimulator returns a complete final four result")
    void finalFourSimulator_returnsValidFinalFourResult() {
        RegionSimulator regionSimulator = new RegionSimulator();
        FinalFour result = new FinalFourSimulator().simulate(
                regionSimulator.simulate(RegionType.EAST, DEFAULT_TEST_YEAR),
                regionSimulator.simulate(RegionType.MIDWEST, DEFAULT_TEST_YEAR),
                regionSimulator.simulate(RegionType.SOUTH, DEFAULT_TEST_YEAR),
                regionSimulator.simulate(RegionType.WEST, DEFAULT_TEST_YEAR)
        );

        assertNotNull(result);
        assertNotNull(result.getSouthVSWest());
        assertNotNull(result.getEastVSMidwest());
        assertNotNull(result.getChampionship());
        assertNotNull(result.getChampion());
    }

    @Test
    @DisplayName("BracketSimulator returns a completed bracket object")
    void bracketSimulator_returnsCompletedBracket() throws Exception {
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.STOCHASTIC);
        Bracket bracket = simulator.simulate(DEFAULT_TEST_YEAR);
        JsonNode node = TestJsonUtil.parseJson(bracket.toJson());

        assertNotNull(bracket);
        assertNotNull(bracket.getChampion());
        assertNotNull(bracket.getChampionName());
        assertEquals(DEFAULT_TEST_YEAR, node.get("year").asInt());
        assertEquals(4, node.get("regions").size());
    }

    @Test
    @DisplayName("Bracket champion must be one of the four Final Four participants")
    void bracketSimulation_championMustComeFromFinalFourParticipants() throws Exception {
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.STOCHASTIC);
        Bracket bracket = simulator.simulate(DEFAULT_TEST_YEAR);

        Set<String> allowedChampions = new HashSet<>();
        JsonNode finalFourNode = TestJsonUtil.parseJson(bracket.toJson()).get("finalFour");

        allowedChampions.add(finalFourNode.get("east").get("Team").asText());
        allowedChampions.add(finalFourNode.get("west").get("Team").asText());
        allowedChampions.add(finalFourNode.get("south").get("Team").asText());
        allowedChampions.add(finalFourNode.get("midwest").get("Team").asText());

        assertTrue(allowedChampions.contains(bracket.getChampionName()));
    }
}
