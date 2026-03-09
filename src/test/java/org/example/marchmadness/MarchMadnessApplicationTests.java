package org.example.marchmadness;

import org.example.marchmadness.generators.BracketGenerator;
import org.example.marchmadness.models.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class MarchMadnessApplicationTests {

    @Test
    void contextLoads() throws IOException {
        Bracket bracket = new Bracket(2024);
    }

    @Test
    public void teamJson() {
        Team team = new Team(2024, "Maryland", 1);
        assert(team.toJson().equals("{\"Seed\":1,\"Team\":\"Maryland\",\"AdjT\":0.0,\"AdjEM\":0.0}") );
    }

    @Test
    public void gameJson() {
        Team team1 = new Team(2024, "Maryland", 1);
        Team team2 = new Team(2024, "Arkansas St.", 16);
        Game game = new Game(team1, team2);

        String gameJson = new Game(team1, team2).toJson();
        boolean option1 = gameJson.equals("{\"winner\":{\"Seed\":1,\"Team\":\"Maryland\",\"AdjT\":0.0,\"AdjEM\":0.0},\"loser\":{\"Seed\":16,\"Team\":\"Arkansas St.\",\"AdjT\":0.0,\"AdjEM\":0.0}}");
        boolean option2 = gameJson.equals("{\"winner\":{\"Seed\":16,\"Team\":\"Arkansas St.\",\"AdjT\":0.0,\"AdjEM\":0.0},\"loser\":{\"Seed\":1,\"Team\":\"Maryland\",\"AdjT\":0.0,\"AdjEM\":0.0}}");
        assert(option1 || option2);
    }

    @Test public void regionJson() {
        Region r = new Region(RegionType.WEST, 2024);
        System.out.print(r.toJson());
    }

    @Test public void finalFourJson() {
        int year = 2024;
        Region east = new Region(RegionType.EAST, year);
        Region mw = new Region(RegionType.MIDWEST, year);
        Region south = new Region(RegionType.SOUTH, year);
        Region west = new Region(RegionType.WEST, year);
        FinalFour finalFour = new FinalFour(east, mw, south, west);
        System.out.println(finalFour.toJson());
    }

    @Test
    public void bracketJson() {
        BracketGenerator bg = new BracketGenerator(2024);


       System.out.println(bg.getBracket().toJson());

    }

}
