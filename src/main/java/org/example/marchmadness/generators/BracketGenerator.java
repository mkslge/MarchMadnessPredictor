package org.example.marchmadness.generators;

import org.example.marchmadness.models.Bracket;

public class BracketGenerator {
    Bracket result;
    public BracketGenerator(int year) {
       result = new Bracket(year);
       result.run();
    }

    public Bracket getBracket() {
        return result;
    }
}
