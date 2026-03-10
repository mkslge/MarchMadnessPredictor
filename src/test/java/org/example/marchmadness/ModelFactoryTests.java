package org.example.marchmadness;

import org.example.marchmadness.factories.ModelFactory;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.models.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelFactoryTests {

    private static final int DEFAULT_TEST_YEAR = 2024;

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
}
