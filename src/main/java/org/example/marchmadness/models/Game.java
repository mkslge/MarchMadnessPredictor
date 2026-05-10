package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Game {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty
    private final Team team1;

    @JsonProperty
    private final Team team2;

    @JsonProperty
    private final double oddsOutOf100;

    @JsonProperty
    private final Team winner;

    @JsonProperty
    private final Team loser;

    /**
     * Command: Create a completed game result.
     * Preconditions: All teams are non-null and winner/loser are participants in the matchup.
     * Postconditions: Game stores matchup participants, win probability, winner, and loser.
     */
    public Game(Team team1, Team team2, double oddsOutOf100, Team winner, Team loser) {
        if (team1 == null || team2 == null || winner == null || loser == null) {
            throw new IllegalArgumentException("Game teams cannot be null");
        }
        this.team1 = new Team(team1);
        this.team2 = new Team(team2);
        this.oddsOutOf100 = oddsOutOf100;
        this.winner = new Team(winner);
        this.loser = new Team(loser);
    }

    Game() {
        team1 = null;
        team2 = null;
        oddsOutOf100 = 0;
        winner = null;
        loser = null;
    }

    public Team getTeam1() {
        return this.team1;
    }

    public Team getTeam2() {
        return this.team2;
    }

    public double getOddsOutOf100() {
        return this.oddsOutOf100;
    }

    public Team getWinner() {
        return this.winner;
    }

    public Team getLoser() {
        return this.loser;
    }

    /**
     * Command: Serialize this game into a JSON string.
     * Preconditions: Game state is initialized.
     * Postconditions: Returns a JSON representation of the current game result.
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException exc) {
            throw new IllegalStateException("Failed to serialize Game to JSON", exc);
        }
    }

    public String toString() {
        return team1 + " vs " + team2;
    }
}
