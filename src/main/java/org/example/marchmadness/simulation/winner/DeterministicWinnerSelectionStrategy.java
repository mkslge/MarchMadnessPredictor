package org.example.marchmadness.simulation.winner;

import org.example.marchmadness.models.Team;

public class DeterministicWinnerSelectionStrategy implements WinnerSelectionStrategy {

    /**
     * Command: Select a winner deterministically from matchup probability.
     * Preconditions: Teams are non-null and probability input is in [0, 100].
     * Postconditions: Returns the most likely winner, with deterministic tiebreakers.
     */
    @Override
    public Team selectWinner(Team team1, Team team2, double team2WinProbabilityOutOf100) {
        if (team2WinProbabilityOutOf100 < 50.0) {
            return team1;
        }
        if (team2WinProbabilityOutOf100 > 50.0) {
            return team2;
        }

        if (team1.getAdjEM() > team2.getAdjEM()) {
            return team1;
        }
        if (team2.getAdjEM() > team1.getAdjEM()) {
            return team2;
        }

        return team1.getSeed() <= team2.getSeed() ? team1 : team2;
    }
}
