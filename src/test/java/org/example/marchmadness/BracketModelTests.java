package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Bracket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BracketModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Bracket model run initializes regions and champion for the year")
    void bracketModel_runProducesChampionAndExpectedRegionCount() throws Exception {
        Bracket bracket = new Bracket(DEFAULT_TEST_YEAR);
        bracket.run();

        JsonNode node = TestJsonUtil.parseJson(bracket.toJson());
        assertEquals(DEFAULT_TEST_YEAR, node.get("year").asInt());
        assertEquals(4, node.get("regions").size());
        assertNotNull(bracket.getChampion());
        assertNotNull(bracket.getChampionName());
        assertFalse(bracket.getChampionName().isBlank());
    }
}
