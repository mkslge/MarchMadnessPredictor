package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.marchmadness.factories.ModelFactory;
import org.example.marchmadness.generators.BracketGenerator;
import org.example.marchmadness.generators.FinalFourGenerator;
import org.example.marchmadness.generators.RegionGenerator;
import org.example.marchmadness.controllers.BracketController;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.models.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class MarchMadnessApplicationTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int DEFAULT_TEST_YEAR = 2024;
    private static final int TEST_YEAR_2023 = 2023;

    @Autowired
    private BracketController bracketController;

    /**
     * Command: Parse JSON text into a traversable node tree for assertions.
     * Preconditions: `json` is valid JSON text.
     * Postconditions: Returns a non-null parsed `JsonNode`.
     */
    private JsonNode parseJson(String json) throws Exception {
        return OBJECT_MAPPER.readTree(json);
    }

    @Test
    @DisplayName("Context loads and bracket generator returns a champion")
    void contextLoads() {
        BracketGenerator generator = new BracketGenerator(DEFAULT_TEST_YEAR);
        assertNotNull(generator.getBracket());
        assertNotNull(generator.getBracket().getChampion());
    }

    @Test
    @DisplayName("Team model serializes required seed and team fields")
    void teamModel_jsonShapeAndValues() throws Exception {
        Team team = new Team(DEFAULT_TEST_YEAR, "Maryland", 1);
        JsonNode node = parseJson(team.toJson());

        assertEquals(1, node.get("Seed").asInt());
        assertEquals("Maryland", node.get("Team").asText());
        assertTrue(node.has("AdjT"));
        assertTrue(node.has("AdjEM"));
    }

    @Test
    @DisplayName("Team copy constructor preserves equality and hash code contract")
    void teamModel_copyConstructorAndEqualityContract() {
        Team original = new Team(DEFAULT_TEST_YEAR, "Arizona", 2);
        Team copy = new Team(original);
        Team sameNameDifferentSeed = new Team(DEFAULT_TEST_YEAR, "Arizona", 10);

        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
        assertEquals(original, sameNameDifferentSeed);
    }

    @Test
    @DisplayName("Game model always produces distinct winner and loser participants")
    void gameModel_winnerAndLoserAreAlwaysDifferentParticipants() throws Exception {
        Team team1 = new Team(DEFAULT_TEST_YEAR, "Maryland", 1);
        Team team2 = new Team(DEFAULT_TEST_YEAR, "Arkansas St.", 16);

        Game game = new Game(team1, team2);
        JsonNode node = parseJson(game.toJson());

        String winner = node.get("winner").get("Team").asText();
        String loser = node.get("loser").get("Team").asText();

        assertTrue(winner.equals("Maryland") || winner.equals("Arkansas St."));
        assertTrue(loser.equals("Maryland") || loser.equals("Arkansas St."));
        assertNotEquals(winner, loser);
    }

    @Test
    @DisplayName("Game model recalculates outcome after participant changes")
    void gameModel_addTeamsAndSettersRecalculateResult() {
        Team alpha = new Team(DEFAULT_TEST_YEAR, "Alpha", 1);
        Team beta = new Team(DEFAULT_TEST_YEAR, "Beta", 2);
        Team gamma = new Team(DEFAULT_TEST_YEAR, "Gamma", 3);
        Team delta = new Team(DEFAULT_TEST_YEAR, "Delta", 4);

        Game game = new Game(alpha, beta);
        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());

        game.addTeams(gamma, delta);
        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());

        game.setTeam1(alpha);
        game.setTeam2(beta);
        assertNotNull(game.getWinner());
        assertNotNull(game.getLoser());
    }

    @Test
    @DisplayName("Region model simulates all rounds and outputs expected bracket sizes")
    void regionModel_simulatesAndProducesRoundJsonArrays() throws Exception {
        Region region = new Region(RegionType.WEST, DEFAULT_TEST_YEAR);
        JsonNode node = parseJson(region.toJson());

        assertEquals("WEST", node.get("region").asText());
        assertEquals(8, node.get("fieldOf64").size());
        assertEquals(4, node.get("fieldOf32").size());
        assertEquals(2, node.get("sweet16").size());
        assertEquals(1, node.get("elite8").size());
        assertNotNull(region.getWinner());
    }

    @Test
    @DisplayName("Final Four model builds semifinal, championship, and champion results")
    void finalFourModel_buildsSemisChampionshipAndChampion() throws Exception {
        Region east = new Region(RegionType.EAST, DEFAULT_TEST_YEAR);
        Region midwest = new Region(RegionType.MIDWEST, DEFAULT_TEST_YEAR);
        Region south = new Region(RegionType.SOUTH, DEFAULT_TEST_YEAR);
        Region west = new Region(RegionType.WEST, DEFAULT_TEST_YEAR);

        FinalFour finalFour = new FinalFour(east, midwest, south, west);
        JsonNode node = parseJson(finalFour.toJson());

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
    @DisplayName("Bracket model run initializes regions and champion for the year")
    void bracketModel_runProducesChampionAndExpectedRegionCount() throws Exception {
        Bracket bracket = new Bracket(DEFAULT_TEST_YEAR);
        bracket.run();

        JsonNode node = parseJson(bracket.toJson());
        assertEquals(DEFAULT_TEST_YEAR, node.get("year").asInt());
        assertEquals(4, node.get("regions").size());
        assertNotNull(bracket.getChampion());
        assertNotNull(bracket.getChampionName());
        assertFalse(bracket.getChampionName().isBlank());
    }

    @Test
    @DisplayName("ModelFactory creates all model types with valid derived state")
    void modelFactory_createsAllModelTypes() {
        ModelFactory factory = new ModelFactory();

        Team teamA = factory.createTeam(DEFAULT_TEST_YEAR, "Factory Team A", 1);
        Team teamB = factory.createTeam(DEFAULT_TEST_YEAR, "Factory Team B", 2);
        Game game = factory.createGame(teamA, teamB);
        Region east = factory.createRegion(RegionType.EAST, DEFAULT_TEST_YEAR);
        Region midwest = factory.createRegion(RegionType.MIDWEST, DEFAULT_TEST_YEAR);
        Region south = factory.createRegion(RegionType.SOUTH, DEFAULT_TEST_YEAR);
        Region west = factory.createRegion(RegionType.WEST, DEFAULT_TEST_YEAR);
        FinalFour finalFour = factory.createFinalFour(east, midwest, south, west);
        Bracket bracket = factory.createBracket(DEFAULT_TEST_YEAR);

        assertNotNull(teamA);
        assertNotNull(game);
        assertNotNull(east);
        assertNotNull(finalFour);
        assertNotNull(bracket);

        assertNotNull(game.getWinner());
        assertNotNull(east.getWinner());
        assertNotNull(finalFour.getChampion());
    }

    @Test
    @DisplayName("RegionGenerator returns all four region results with winners")
    void regionGenerator_returnsFourNonNullRegionResultsWithWinners() {
        RegionGenerator generator = new RegionGenerator(DEFAULT_TEST_YEAR);

        Region east = generator.getEastResult();
        Region midwest = generator.getMidwestResult();
        Region south = generator.getSouthResult();
        Region west = generator.getWestResult();

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
    @DisplayName("FinalFourGenerator returns a complete final four result")
    void finalFourGenerator_returnsValidFinalFourResult() {
        FinalFourGenerator generator = new FinalFourGenerator(DEFAULT_TEST_YEAR);
        FinalFour result = generator.getResult();

        assertNotNull(result);
        assertNotNull(result.getSouthVSWest());
        assertNotNull(result.getEastVSMidwest());
        assertNotNull(result.getChampionship());
        assertNotNull(result.getChampion());
    }

    @Test
    @DisplayName("BracketGenerator returns a completed bracket object")
    void bracketGenerator_returnsCompletedBracket() throws Exception {
        BracketGenerator generator = new BracketGenerator(DEFAULT_TEST_YEAR);
        Bracket bracket = generator.getBracket();
        JsonNode node = parseJson(bracket.toJson());

        assertNotNull(bracket);
        assertNotNull(bracket.getChampion());
        assertNotNull(bracket.getChampionName());
        assertEquals(DEFAULT_TEST_YEAR, node.get("year").asInt());
        assertEquals(4, node.get("regions").size());
    }

    @Test
    @DisplayName("Bracket champion must be one of the four Final Four participants")
    void bracketSimulation_championMustComeFromFinalFourParticipants() throws Exception {
        BracketGenerator generator = new BracketGenerator(DEFAULT_TEST_YEAR);
        Bracket bracket = generator.getBracket();

        Set<String> allowedChampions = new HashSet<>();
        JsonNode finalFourNode = parseJson(bracket.toJson()).get("finalFour");

        allowedChampions.add(finalFourNode.get("east").get("Team").asText());
        allowedChampions.add(finalFourNode.get("west").get("Team").asText());
        allowedChampions.add(finalFourNode.get("south").get("Team").asText());
        allowedChampions.add(finalFourNode.get("midwest").get("Team").asText());

        assertTrue(allowedChampions.contains(bracket.getChampionName()));
    }


    @Test
    @DisplayName("Controller endpoint path returns bracket for 2024 and remains healthy")
    void endpointPath_2024ReturnsBracket() {
        Bracket bracket = bracketController.generateBracket(DEFAULT_TEST_YEAR);
        assertNotNull(bracket);
        assertNotNull(bracket.getChampion());
    }
}
