package org.example.marchmadness.simulation;

import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.simulation.winner.StochasticWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

public class StochasticBracketSimulator implements BracketSimulator {
    private final WinnerSelectionStrategy winnerSelectionStrategy;

    public StochasticBracketSimulator() {
        this.winnerSelectionStrategy = new StochasticWinnerSelectionStrategy();
    }

    /**
     * Command: Simulate a stochastic bracket.
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
     * Postconditions: Returns `STOCHASTIC`.
     */
    @Override
    public SimulationMode getMode() {
        return SimulationMode.STOCHASTIC;
    }

    public WinnerSelectionStrategy getWinnerSelectionStrategy() {
        return winnerSelectionStrategy;
    }
}
