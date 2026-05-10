package org.example.marchmadness.simulation;

import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Team;
import org.example.marchmadness.simulation.winner.StochasticWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

import static java.lang.Math.sqrt;

public class GameSimulator {
    private static final int STANDARD_DEVIATION = 11;

    private final WinnerSelectionStrategy winnerSelectionStrategy;

    public GameSimulator() {
        this(new StochasticWinnerSelectionStrategy());
    }

    /**
     * Command: Create a reusable game simulator.
     * Preconditions: Winner selection strategy is non-null.
     * Postconditions: Simulator can evaluate any matchup using the provided strategy.
     */
    public GameSimulator(WinnerSelectionStrategy winnerSelectionStrategy) {
        if (winnerSelectionStrategy == null) {
            throw new IllegalArgumentException("Winner selection strategy cannot be null");
        }
        this.winnerSelectionStrategy = winnerSelectionStrategy;
    }

    /**
     * Command: Simulate a game between two teams.
     * Preconditions: Teams are non-null with populated tempo/efficiency values.
     * Postconditions: Returns a completed game result with matchup participants, winner, and loser.
     */
    public Game simulate(Team team1, Team team2) {
        if (team1 == null || team2 == null) {
            throw new IllegalArgumentException("Teams cannot be null");
        }
        double oddsOutOf100 = calculateOdds(team1, team2);
        return calculateWinner(team1, team2, oddsOutOf100);
    }

    /**
     * Command: Calculate the underdog win probability for the current matchup.
     * Preconditions: `team1` and `team2` are non-null with populated tempo/efficiency values.
     * Postconditions: Returns team2's win probability for the current matchup.
     */
    private double calculateOdds(Team team1, Team team2) {
        double pointDiff = (team1.getAdjEM() - team2.getAdjEM()) * (team1.getAdjT() + team2.getAdjT()) / 200;
        double cdf = 0.5 * (1 + erf((0 - pointDiff) / (STANDARD_DEVIATION * sqrt(2))));
        return cdf * 100;
    }

    /**
     * Command: Simulate and select a winner for the current matchup.
     * Preconditions: Teams are non-null and odds have been calculated for the matchup.
     * Postconditions: Returns a `Game` result with winner and loser populated.
     */
    private Game calculateWinner(Team team1, Team team2, double oddsOutOf100) {
        Team selectedWinner = winnerSelectionStrategy.selectWinner(team1, team2, oddsOutOf100);
        Team winner;
        Team loser;
        if (selectedWinner == team2) {
            winner = new Team(team2);
            loser = new Team(team1);
        } else {
            winner = new Team(team1);
            loser = new Team(team2);
        }
        return new Game(team1, team2, oddsOutOf100, winner, loser);
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
