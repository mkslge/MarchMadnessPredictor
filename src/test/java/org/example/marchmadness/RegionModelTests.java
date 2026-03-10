package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegionModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Region model simulates all rounds and outputs expected bracket sizes")
    void regionModel_simulatesAndProducesRoundJsonArrays() throws Exception {
        Region region = new Region(RegionType.WEST, DEFAULT_TEST_YEAR);
        JsonNode node = TestJsonUtil.parseJson(region.toJson());

        assertEquals("WEST", node.get("region").asText());
        assertEquals(8, node.get("fieldOf64").size());
        assertEquals(4, node.get("fieldOf32").size());
        assertEquals(2, node.get("sweet16").size());
        assertEquals(1, node.get("elite8").size());
        assertNotNull(region.getWinner());
    }
}
