package org.example.marchmadness.factories;

import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.models.Team;

public class ModelFactory {

    public Team createTeam(int year, String name, int seed) {
        return new Team(year, name, seed);
    }

    public Game createGame(Team team1, Team team2) {
        return new Game(team1, team2);
    }

    public Region createRegion(RegionType type, int year) {
        return new Region(type, year);
    }

    public FinalFour createFinalFour(Region east, Region midwest, Region south, Region west) {
        return new FinalFour(east, midwest, south, west);
    }

    public Bracket createBracket(int year) {
        return new Bracket(year);
    }
}
