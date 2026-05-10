package org.example.marchmadness.simulation;

import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.RegionType;
import org.example.marchmadness.simulation.winner.StochasticWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

import java.util.List;

public class StochasticBracketSimulator implements BracketSimulator {
    private final WinnerSelectionStrategy winnerSelectionStrategy;
    private final RegionSimulator regionSimulator;
    private final FinalFourSimulator finalFourSimulator;

    public StochasticBracketSimulator() {
        this.winnerSelectionStrategy = new StochasticWinnerSelectionStrategy();
        this.regionSimulator = new RegionSimulator(winnerSelectionStrategy);
        this.finalFourSimulator = new FinalFourSimulator(winnerSelectionStrategy);
    }

    /**
     * Command: Simulate a stochastic bracket.
     * Preconditions: Year datasets exist and are readable.
     * Postconditions: Returns a completed bracket simulation for `year`.
     */
    @Override
    public Bracket simulate(int year) {
        List<Region> regions = simulateRegions(year);
        FinalFour finalFour = finalFourSimulator.simulate(
                regions.get(0),
                regions.get(1),
                regions.get(2),
                regions.get(3)
        );
        return new Bracket(year, regions, finalFour);
    }

    /**
     * Command: Simulate all regions in fixed tournament order.
     * Preconditions: Year datasets exist for all four regions.
     * Postconditions: Returns EAST, MIDWEST, SOUTH, WEST region results.
     */
    private List<Region> simulateRegions(int year) {
        return List.of(
                regionSimulator.simulate(RegionType.EAST, year),
                regionSimulator.simulate(RegionType.MIDWEST, year),
                regionSimulator.simulate(RegionType.SOUTH, year),
                regionSimulator.simulate(RegionType.WEST, year)
        );
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
