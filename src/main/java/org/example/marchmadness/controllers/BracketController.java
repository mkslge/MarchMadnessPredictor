package org.example.marchmadness.controllers;

import org.example.marchmadness.generators.BracketGenerator;
import org.example.marchmadness.models.Bracket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping(path="/bracket")
@RestController
public class BracketController {

    @GetMapping(path="/simulation/{year}")
    public Bracket generateBracket(@PathVariable int year ) {
        BracketGenerator bg = new BracketGenerator(year);
        return bg.getBracket();
    }
}
