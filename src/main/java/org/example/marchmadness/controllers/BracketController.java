package org.example.marchmadness.controllers;

import org.example.marchmadness.util.DatasetUtil;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.Game;
import org.example.marchmadness.simulation.BracketSimulator;
import org.example.marchmadness.simulation.BracketSimulatorFactory;
import org.example.marchmadness.simulation.SimulationMode;
import org.example.marchmadness.util.SimulationUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RequestMapping(path="/bracket")
@RestController
public class BracketController {

    /**
     * Command: Generate a simulated bracket for a specific tournament year.
     * Preconditions: The requested `year` has datasets available in resources.
     * Postconditions: Returns a fully simulated `Bracket` object for that year.
     */
    @GetMapping(path="/simulation/{year}")
    public Bracket generateBracket(@PathVariable int year ) {
        return generateStochasticBracket(year);
    }

    /**
     * Command: Generate a stochastic bracket for a specific tournament year.
     * Preconditions: The requested `year` has datasets available in resources.
     * Postconditions: Returns a fully simulated stochastic `Bracket`.
     */
    @GetMapping(path="/simulation/stochastic/{year}")
    public Bracket generateStochasticBracket(@PathVariable int year) {
        DatasetUtil.validateYearSupportedOrThrow(year);
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.STOCHASTIC);
        return simulator.simulate(year);
    }

    /**
     * Command: Generate a deterministic bracket for a specific tournament year.
     * Preconditions: The requested `year` has datasets available in resources.
     * Postconditions: Returns a fully simulated deterministic `Bracket`.
     */
    @GetMapping(path="/simulation/deterministic/{year}")
    public Bracket generateDeterministicBracket(@PathVariable int year) {
        DatasetUtil.validateYearSupportedOrThrow(year);
        BracketSimulator simulator = BracketSimulatorFactory.create(SimulationMode.DETERMINISTIC);
        return simulator.simulate(year);
    }

    /**
     * Command: Return all available dataset years as a REST resource.
     * Preconditions: Dataset folders exist under `resources/datasets`.
     * Postconditions: Returns sorted available years.
     */
    @GetMapping(path="/years")
    public List<Integer> getAvailableYears() {
        return DatasetUtil.getAvailableYears();
    }

    /**
     * Command: Return all tournament teams for a specific year as a REST resource.
     * Preconditions: `year` is a valid supported dataset year.
     * Postconditions: Returns all teams that exist in that year's tournament datasets.
     */
    @GetMapping(path="/years/{year}/teams")
    public String[] getTeamsForYear(@PathVariable int year) {
        return DatasetUtil.getTeamsForYear(year);
    }

    /**
     * Command: Simulate a single game between two teams that may be from different years.
     * Preconditions: Both team names are valid and both years are available in datasets.
     * Postconditions: Returns a simulated `Game` with winner and loser.
     */
    @GetMapping(path="/game/simulation")
    public Game simulateGame(
            @RequestParam String team1,
            @RequestParam int year1,
            @RequestParam String team2,
            @RequestParam int year2
    ) {
        return SimulationUtil.simulateGame(team1, year1, team2, year2);
    }

}
