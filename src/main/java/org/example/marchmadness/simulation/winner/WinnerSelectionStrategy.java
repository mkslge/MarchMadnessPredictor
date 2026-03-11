package org.example.marchmadness.simulation.winner;

import org.example.marchmadness.models.Team;

public interface WinnerSelectionStrategy {

    /**
     * Command: Select a winner between two teams.
     * Preconditions: Teams are non-null and probability input is in [0, 100].
     * Postconditions: Returns one of the two input teams as winner.
     */
    Team selectWinner(Team team1, Team team2, double team2WinProbabilityOutOf100);
}
