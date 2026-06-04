package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.dto.ScoreUpdateRequest;
import id.ac.ui.cs.advprog.liga.model.Season;

public interface LeagueService {
  void handleScoreUpdate(ScoreUpdateRequest request);

  Season endSeason();

  Season getCurrentSeason();
}