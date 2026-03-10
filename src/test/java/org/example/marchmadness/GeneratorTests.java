package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.generators.BracketGenerator;
import org.example.marchmadness.generators.FinalFourGenerator;
import org.example.marchmadness.generators.RegionGenerator;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

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
        BracketGenerator generator = new BracketGenerator(DEFAULT_TEST_YEAR);
        Bracket bracket = generator.getBracket();

        Set<String> allowedChampions = new HashSet<>();
        JsonNode finalFourNode = TestJsonUtil.parseJson(bracket.toJson()).get("finalFour");

        allowedChampions.add(finalFourNode.get("east").get("Team").asText());
        allowedChampions.add(finalFourNode.get("west").get("Team").asText());
        allowedChampions.add(finalFourNode.get("south").get("Team").asText());
        allowedChampions.add(finalFourNode.get("midwest").get("Team").asText());

        assertTrue(allowedChampions.contains(bracket.getChampionName()));
    }
}
