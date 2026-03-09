package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.marchmadness.generators.BracketGenerator;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.models.Team;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MarchMadnessApplicationTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void contextLoads() {
        BracketGenerator generator = new BracketGenerator(2024);
        assertNotNull(generator.getBracket());
        assertNotNull(generator.getBracket().getChampion());
    }

    @Test
    void teamJson() throws Exception {
        Team team = new Team(2024, "Maryland", 1);
        JsonNode node = OBJECT_MAPPER.readTree(team.toJson());

        assertEquals(1, node.get("Seed").asInt());
        assertEquals("Maryland", node.get("Team").asText());
    }

    @Test
    void gameJson() throws Exception {
        Team team1 = new Team(2024, "Maryland", 1);
        Team team2 = new Team(2024, "Arkansas St.", 16);
        Game game = new Game(team1, team2);

        JsonNode node = OBJECT_MAPPER.readTree(game.toJson());
        String winner = node.get("winner").get("Team").asText();
        String loser = node.get("loser").get("Team").asText();

        assertTrue(winner.equals("Maryland") || winner.equals("Arkansas St."));
        assertTrue(loser.equals("Maryland") || loser.equals("Arkansas St."));
        assertFalse(winner.equals(loser));
    }

    @Test
    void regionJson() {
        Region region = new Region(RegionType.WEST, 2024);
        assertNotNull(region.getWinner());
        assertNotNull(region.toJson());
    }

    @Test
    void finalFourJson() {
        int year = 2024;
        Region east = new Region(RegionType.EAST, year);
        Region midwest = new Region(RegionType.MIDWEST, year);
        Region south = new Region(RegionType.SOUTH, year);
        Region west = new Region(RegionType.WEST, year);
        FinalFour finalFour = new FinalFour(east, midwest, south, west);

        assertNotNull(finalFour.getChampion());
        assertNotNull(finalFour.toJson());
    }

    @Test
    void bracketJson() {
        BracketGenerator bg = new BracketGenerator(2024);
        Bracket bracket = bg.getBracket();

        assertNotNull(bracket);
        assertNotNull(bracket.getChampion());
        assertNotNull(bracket.toJson());
    }
}
