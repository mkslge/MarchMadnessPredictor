package org.example.marchmadness.simulation;

import org.example.marchmadness.models.FinalFour;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.models.Region;
import org.example.marchmadness.models.Team;
import org.example.marchmadness.simulation.winner.StochasticWinnerSelectionStrategy;
import org.example.marchmadness.simulation.winner.WinnerSelectionStrategy;

public class FinalFourSimulator {
    private final GameSimulator gameSimulator;

    public FinalFourSimulator() {
        this(new StochasticWinnerSelectionStrategy());
    }

    /**
     * Command: Create a reusable Final Four simulator.
     * Preconditions: Winner selection strategy is non-null.
     * Postconditions: Simulator can build Final Four results using the provided strategy.
     */
    public FinalFourSimulator(WinnerSelectionStrategy winnerSelectionStrategy) {
        if (winnerSelectionStrategy == null) {
            throw new IllegalArgumentException("Winner selection strategy cannot be null");
        }
        this.gameSimulator = new GameSimulator(winnerSelectionStrategy);
    }

    /**
     * Command: Simulate the Final Four and championship games from regional winners.
     * Preconditions: All four regions are non-null and each has a winner.
     * Postconditions: Returns a completed Final Four result with champion populated.
     */
    public FinalFour simulate(Region east, Region midwest, Region south, Region west) {
        if (east == null || midwest == null || south == null || west == null) {
            throw new IllegalArgumentException("Regions cannot be null");
        }

        Team eastWinner = new Team(east.getWinner());
        Team midwestWinner = new Team(midwest.getWinner());
        Team southWinner = new Team(south.getWinner());
        Team westWinner = new Team(west.getWinner());

        Game southVSWest = gameSimulator.simulate(southWinner, westWinner);
        Game eastVSMidwest = gameSimulator.simulate(eastWinner, midwestWinner);
        Game championship = gameSimulator.simulate(southVSWest.getWinner(), eastVSMidwest.getWinner());

        return new FinalFour(
                eastWinner,
                midwestWinner,
                southWinner,
                westWinner,
                southVSWest,
                eastVSMidwest,
                championship,
                championship.getWinner()
        );
    }
}
