package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinalFourModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Final Four model builds semifinal, championship, and champion results")
    void finalFourModel_buildsSemisChampionshipAndChampion() throws Exception {
        Region east = new Region(RegionType.EAST, DEFAULT_TEST_YEAR);
        Region midwest = new Region(RegionType.MIDWEST, DEFAULT_TEST_YEAR);
        Region south = new Region(RegionType.SOUTH, DEFAULT_TEST_YEAR);
        Region west = new Region(RegionType.WEST, DEFAULT_TEST_YEAR);

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
