package org.example.marchmadness.controllers;

import org.example.marchmadness.generators.BracketGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;


@RequestMapping(path="/bracket")
@RestController
public class BracketController {

    @GetMapping(path="/simulation/{year}")
    public String generateBracket(@PathVariable int year ) {
        BracketGenerator bg = new BracketGenerator(year);
        return bg.getBracket().toJson();
    }


}