package org.example.marchmadness.storage;

import org.example.marchmadness.models.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameStore {
    private final List<Game> games = new ArrayList<>();

    public void add(Game game) {
        games.add(game);
    }

    public List<Game> getAll() {
        return Collections.unmodifiableList(games);
    }
}
