package org.example.marchmadness.storage;

import org.example.marchmadness.models.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamStore {
    private final List<Team> teams = new ArrayList<>();

    public void add(Team team) {
        teams.add(team);
    }

    public List<Team> getAll() {
        return Collections.unmodifiableList(teams);
    }
}
