package org.example.marchmadness.controllers;

import org.example.marchmadness.generators.BracketGenerator;
import org.example.marchmadness.util.DatasetUtil;
import org.example.marchmadness.models.Bracket;
import org.example.marchmadness.models.Game;
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
        DatasetUtil.validateYearSupportedOrThrow(year);
        BracketGenerator bracketGenerator = new BracketGenerator(year);
        return bracketGenerator.getBracket();
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
