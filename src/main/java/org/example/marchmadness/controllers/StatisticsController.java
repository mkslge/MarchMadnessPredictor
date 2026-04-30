package org.example.marchmadness.controllers;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.example.marchmadness.entities.TeamYearStatistics;
import org.example.marchmadness.services.StatisticsService;


@RequestMapping(path="/statistics")
@RestController
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping(path="/{team}/{year}")
    public TeamYearStatistics getTeamStatistics(@PathVariable String team, @PathVariable int year) {
        return statisticsService.getTeamSeasonStatistics(team, year);
    }
}
