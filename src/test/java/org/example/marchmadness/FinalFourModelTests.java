package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.models.Team;
import org.example.marchmadness.simulation.FinalFourSimulator;
import org.example.marchmadness.simulation.RegionSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinalFourModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("FinalFourSimulator builds semifinal, championship, and champion results")
    void finalFourSimulator_buildsSemisChampionshipAndChampion() throws Exception {
        RegionSimulator regionSimulator = new RegionSimulator();
        Region east = regionSimulator.simulate(RegionType.EAST, DEFAULT_TEST_YEAR);
        Region midwest = regionSimulator.simulate(RegionType.MIDWEST, DEFAULT_TEST_YEAR);
        Region south = regionSimulator.simulate(RegionType.SOUTH, DEFAULT_TEST_YEAR);
        Region west = regionSimulator.simulate(RegionType.WEST, DEFAULT_TEST_YEAR);

        FinalFour finalFour = new FinalFourSimulator().simulate(east, midwest, south, west);
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

    @Test
    @DisplayName("Final Four model stores completed results without simulating")
    void finalFourModel_storesCompletedResults() {
        Team east = new Team(DEFAULT_TEST_YEAR, "East", 1);
        Team midwest = new Team(DEFAULT_TEST_YEAR, "Midwest", 2);
        Team south = new Team(DEFAULT_TEST_YEAR, "South", 3);
        Team west = new Team(DEFAULT_TEST_YEAR, "West", 4);

        Game southVSWest = new Game(south, west, 60.0, south, west);
        Game eastVSMidwest = new Game(east, midwest, 40.0, east, midwest);
        Game championship = new Game(south, east, 45.0, east, south);
        FinalFour finalFour = new FinalFour(
                east,
                midwest,
                south,
                west,
                southVSWest,
                eastVSMidwest,
                championship,
                east
        );

        assertEquals(east, finalFour.getEast());
        assertEquals(midwest, finalFour.getMidwest());
        assertEquals(south, finalFour.getSouth());
        assertEquals(west, finalFour.getWest());
        assertEquals(southVSWest, finalFour.getSouthVSWest());
        assertEquals(eastVSMidwest, finalFour.getEastVSMidwest());
        assertEquals(championship, finalFour.getChampionship());
        assertEquals(east, finalFour.getChampion());
    }
}
