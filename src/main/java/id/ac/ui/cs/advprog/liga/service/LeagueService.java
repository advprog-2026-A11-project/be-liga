package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.dto.ScoreUpdateRequest;
import id.ac.ui.cs.advprog.liga.model.Season;

public interface LeagueService {

    // Called by be-bacaan when a student finishes a quiz
    void handleScoreUpdate(ScoreUpdateRequest request);

    // Called by admin to end the current season
    Season endSeason();

    // Returns the currently active season
    Season getCurrentSeason();
}