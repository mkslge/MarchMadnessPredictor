package org.example.marchmadness.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.marchmadness.simulation.winner.StochasticWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

import java.io.IOException;

import static java.lang.Math.sqrt;

public class Game {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int STANDARD_DEVIATION = 11;

    private Team team1;
    private Team team2;

    @JsonProperty
    private Team winner;

    @JsonProperty
    private Team loser;

    private double oddsOutOf100;
    private final WinnerSelectionStrategy winnerSelectionStrategy;

    public Game(String name1, int seed1, double adjEM1, double adjT1,
                String name2, int seed2, double adjEM2, double adjT2) {
        team1 = new Team(name1, seed1, adjEM1, adjT1);
        team2 = new Team(name2, seed2, adjEM2, adjT2);
        winnerSelectionStrategy = new StochasticWinnerSelectionStrategy();
        this.calculateOdds();
        this.calculateWinner();
    }

    public Game(Team team1, Team team2) {
        this(team1, team2, new StochasticWinnerSelectionStrategy());
    }

    /**
     * Command: Create a game with an explicit winner selection strategy.
     * Preconditions: Teams and strategy are non-null.
     * Postconditions: Game is initialized and a winner/loser is calculated.
     */
    public Game(Team team1, Team team2, WinnerSelectionStrategy winnerSelectionStrategy) {
        if (winnerSelectionStrategy == null) {
            throw new IllegalArgumentException("Winner selection strategy cannot be null");
        }
        this.team1 = team1;
        this.team2 = team2;
        this.winnerSelectionStrategy = winnerSelectionStrategy;
        this.calculateOdds();
        this.calculateWinner();
    }

    public Game(String name1, int seed1, String name2, int seed2) {
        team1 = new Team(name1, seed1);
        team2 = new Team(name2, seed2);
        winnerSelectionStrategy = new StochasticWinnerSelectionStrategy();
    }

    Game() {
        team1 = new Team();
        team2 = new Team();
        winner = null;
        loser = null;
        winnerSelectionStrategy = new StochasticWinnerSelectionStrategy();
    }

    public Game(int year) {
        team1 = new Team(year);
        team2 = new Team(year);
        winner = null;
        loser = null;
        winnerSelectionStrategy = new StochasticWinnerSelectionStrategy();
    }

    /**
     * Command: Calculate the underdog win probability for the current matchup.
     * Preconditions: `team1` and `team2` are non-null with populated tempo/efficiency values.
     * Postconditions: Updates and returns `oddsOutOf100` for the current matchup.
     */
    public double calculateOdds() {
        double pointDiff = (team1.getAdjEM() - team2.getAdjEM()) * (team1.getAdjT() + team2.getAdjT()) / 200;
        double cdf = 0.5 * (1 + erf((0 - pointDiff) / (STANDARD_DEVIATION * sqrt(2))));
        oddsOutOf100 = cdf * 100;
        return oddsOutOf100;
    }

    /**
     * Command: Simulate and select a winner for the current matchup.
     * Preconditions: `calculateOdds()` has been run for the current teams.
     * Postconditions: Updates `winner` and `loser`, then returns winner name.
     */
    public String calculateWinner() {
        Team selectedWinner = winnerSelectionStrategy.selectWinner(team1, team2, oddsOutOf100);
        if (selectedWinner == team2) {
            winner = new Team(team2);
            loser = new Team(team1);
        } else {
            winner = new Team(team1);
            loser = new Team(team2);
        }
        return winner.getName();
    }

    /**
     * Command: Replace matchup participants and immediately resimulate.
     * Preconditions: Provided teams are non-null.
     * Postconditions: `team1`, `team2`, `oddsOutOf100`, `winner`, and `loser` are refreshed.
     */
    public void addTeams(Team team1, Team team2) {
        this.team1 = team1;
        this.team2 = team2;
        this.calculateOdds();
        this.calculateWinner();
    }

    public void setTeam1(Team other) {
        team1 = new Team(other);
        this.calculateOdds();
        this.calculateWinner();
    }

    public void setTeam2(Team other) {
        team2 = new Team(other);
        this.calculateOdds();
        this.calculateWinner();
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

    private static double erf(double x) {
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        int sign = 1;
        if (x < 0) {
            sign = -1;
        }
        x = Math.abs(x);

        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y;
    }
}
