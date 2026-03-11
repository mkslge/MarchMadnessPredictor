package org.example.marchmadness.simulation;

import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.simulation.winner.DeterministicWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

public class DeterministicBracketSimulator implements BracketSimulator {
    private final WinnerSelectionStrategy winnerSelectionStrategy;

    public DeterministicBracketSimulator() {
        this.winnerSelectionStrategy = new DeterministicWinnerSelectionStrategy();
    }

    /**
     * Command: Simulate a deterministic bracket.
     * Preconditions: Year datasets exist and are readable.
     * Postconditions: Returns a completed bracket simulation for `year`.
     */
    @Override
    public Bracket simulate(int year) {
        Bracket bracket = new Bracket(year, winnerSelectionStrategy);
        bracket.run();
        return bracket;
    }

    /**
     * Command: Report the simulation mode handled by this simulator.
     * Preconditions: None.
     * Postconditions: Returns `DETERMINISTIC`.
     */
    @Override
    public SimulationMode getMode() {
        return SimulationMode.DETERMINISTIC;
    }

    public WinnerSelectionStrategy getWinnerSelectionStrategy() {
        return winnerSelectionStrategy;
    }
}
