package org.example.marchmadness;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.marchmadness.models.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamModelTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

    @Test
    @DisplayName("Team model serializes required seed and team fields")
    void teamModel_jsonShapeAndValues() throws Exception {
        Team team = new Team(DEFAULT_TEST_YEAR, "Maryland", 1);
        JsonNode node = TestJsonUtil.parseJson(team.toJson());

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
}
