package org.example.marchmadness.controllers;

import org.example.marchmadness.generators.BracketGenerator;
import org.example.marchmadness.metadata.DatasetMetadata;
import org.example.marchmadness.models.Bracket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        DatasetMetadata.validateYearSupportedOrThrow(year);
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
        return DatasetMetadata.getAvailableYears();
    }
}
