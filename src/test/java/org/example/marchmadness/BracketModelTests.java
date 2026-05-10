package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.simulation.FinalFourSimulator;
import org.example.marchmadness.simulation.RegionSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BracketModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Bracket model stores completed regions and champion for the year")
    void bracketModel_storesCompletedRegionsAndChampion() throws Exception {
        RegionSimulator regionSimulator = new RegionSimulator();
        List<Region> regions = List.of(
                regionSimulator.simulate(RegionType.EAST, DEFAULT_TEST_YEAR),
                regionSimulator.simulate(RegionType.MIDWEST, DEFAULT_TEST_YEAR),
                regionSimulator.simulate(RegionType.SOUTH, DEFAULT_TEST_YEAR),
                regionSimulator.simulate(RegionType.WEST, DEFAULT_TEST_YEAR)
        );
        FinalFour finalFour = new FinalFourSimulator().simulate(
                regions.get(0),
                regions.get(1),
                regions.get(2),
                regions.get(3)
        );
        Bracket bracket = new Bracket(DEFAULT_TEST_YEAR, regions, finalFour);

        JsonNode node = TestJsonUtil.parseJson(bracket.toJson());
        assertEquals(DEFAULT_TEST_YEAR, node.get("year").asInt());
        assertEquals(4, node.get("regions").size());
        assertNotNull(bracket.getChampion());
        assertNotNull(bracket.getChampionName());
        assertFalse(bracket.getChampionName().isBlank());
    }
}
