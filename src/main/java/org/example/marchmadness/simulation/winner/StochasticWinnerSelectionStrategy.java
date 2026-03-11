package org.example.marchmadness.simulation.winner;

import org.example.marchmadness.models.Team;

import java.util.concurrent.ThreadLocalRandom;

public class StochasticWinnerSelectionStrategy implements WinnerSelectionStrategy {

    /**
     * Command: Select a winner probabilistically using the provided win probability.
     * Preconditions: Teams are non-null and probability input is in [0, 100].
     * Postconditions: Returns one of the two input teams based on stochastic sampling.
     */
    @Override
    public Team selectWinner(Team team1, Team team2, double team2WinProbabilityOutOf100) {
        double roll = 100 * ThreadLocalRandom.current().nextDouble();
        return roll < team2WinProbabilityOutOf100 ? team2 : team1;
    }
}
