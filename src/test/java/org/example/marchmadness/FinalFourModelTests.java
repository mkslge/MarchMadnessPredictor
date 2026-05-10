package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.simulation.RegionSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinalFourModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Final Four model builds semifinal, championship, and champion results")
    void finalFourModel_buildsSemisChampionshipAndChampion() throws Exception {
        RegionSimulator regionSimulator = new RegionSimulator();
        Region east = regionSimulator.simulate(RegionType.EAST, DEFAULT_TEST_YEAR);
        Region midwest = regionSimulator.simulate(RegionType.MIDWEST, DEFAULT_TEST_YEAR);
        Region south = regionSimulator.simulate(RegionType.SOUTH, DEFAULT_TEST_YEAR);
        Region west = regionSimulator.simulate(RegionType.WEST, DEFAULT_TEST_YEAR);

        FinalFour finalFour = new FinalFour(east, midwest, south, west);
        JsonNode node = TestJsonUtil.parseJson(finalFour.toJson());

        assertNotNull(finalFour.getSouthVSWest());
        assertNotNull(finalFour.getEastVSMidwest());
        assertNotNull(finalFour.getChampionship());
        assertNotNull(finalFour.getChampion());

        assertTrue(node.has("east"));
        assertTrue(node.has("midwest"));
        assertTrue(node.has("south"));
        assertTrue(node.has("west"));
        assertTrue(node.has("championship"));
        assertTrue(node.has("champion"));
    }
}
