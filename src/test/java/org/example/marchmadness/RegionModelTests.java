package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.models.Team;
import org.example.marchmadness.simulation.RegionSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegionModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("RegionSimulator simulates all rounds and outputs expected bracket sizes")
    void regionSimulator_simulatesAndProducesRoundJsonArrays() throws Exception {
        Region region = new RegionSimulator().simulate(RegionType.WEST, DEFAULT_TEST_YEAR);
        JsonNode node = TestJsonUtil.parseJson(region.toJson());

        assertEquals("WEST", node.get("region").asText());
        assertEquals(8, node.get("fieldOf64").size());
        assertEquals(4, node.get("fieldOf32").size());
        assertEquals(2, node.get("sweet16").size());
        assertEquals(1, node.get("elite8").size());
        assertNotNull(region.getWinner());
    }

    @Test
    @DisplayName("Region model stores completed round results without simulating")
    void regionModel_storesCompletedRoundResults() {
        Team team1 = new Team(DEFAULT_TEST_YEAR, "Alpha", 1);
        Team team2 = new Team(DEFAULT_TEST_YEAR, "Beta", 2);
        Game game = new Game(team1, team2, 75.0, team1, team2);

        Region region = new Region(
                RegionType.EAST,
                DEFAULT_TEST_YEAR,
                List.of(game),
                List.of(),
                List.of(),
                List.of(),
                team1
        );

        assertEquals(RegionType.EAST, region.getRegion());
        assertEquals(DEFAULT_TEST_YEAR, region.getYear());
        assertEquals(team1, region.getWinner());
        assertEquals(1, region.collectGames().size());
    }
}
