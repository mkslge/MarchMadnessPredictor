package org.example.marchmadness.factories;

import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.models.Team;

public class ModelFactory {

    /**
     * Command: Create a team model with identity metadata.
     * Preconditions: `name` is non-null and `seed` represents a tournament seed.
     * Postconditions: Returns a new `Team` instance.
     */
    public Team createTeam(int year, String name, int seed) {
        return new Team(year, name, seed);
    }

    /**
     * Command: Create a game model between two teams.
     * Preconditions: `team1` and `team2` are non-null.
     * Postconditions: Returns a new simulated `Game` instance.
     */
    public Game createGame(Team team1, Team team2) {
        return new Game(team1, team2);
    }

    /**
     * Command: Create a regional tournament model.
     * Preconditions: `type` is non-null and dataset exists for the year/type pair.
     * Postconditions: Returns a simulated `Region` instance.
     */
    public Region createRegion(RegionType type, int year) {
        return new Region(type, year);
    }

    /**
     * Command: Create a Final Four model from four region winners.
     * Preconditions: All regions are non-null and already have winners.
     * Postconditions: Returns a simulated `FinalFour` instance.
     */
    public FinalFour createFinalFour(Region east, Region midwest, Region south, Region west) {
        return new FinalFour(east, midwest, south, west);
    }

    /**
     * Command: Create a bracket model for a tournament year.
     * Preconditions: `year` is a valid tournament year.
     * Postconditions: Returns a new `Bracket` instance (not yet run).
     */
    public Bracket createBracket(int year) {
        return new Bracket(year);
    }
}
