package org.example.marchmadness.storage;

import org.example.marchmadness.models.Bracket;

import java.util.HashMap;
import java.util.Map;

public class BracketStore {
    private final Map<Integer, Bracket> bracketsByYear = new HashMap<>();

    public void save(int year, Bracket bracket) {
        bracketsByYear.put(year, bracket);
    }

    public Bracket get(int year) {
        return bracketsByYear.get(year);
    }
}
