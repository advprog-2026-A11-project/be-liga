package id.ac.ui.cs.advprog.liga.controller;

import id.ac.ui.cs.advprog.liga.dto.ScoreUpdateRequest;
import id.ac.ui.cs.advprog.liga.model.Season;
import id.ac.ui.cs.advprog.liga.service.LeagueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "${frontend.url}")
public class LeagueController {

  private final LeagueService leagueService;

  public LeagueController(LeagueService leagueService) {
    this.leagueService = leagueService;
  }

  // Called by be-bacaan (internal, no JWT needed from user)
  // be-bacaan calls this right after a student submits a quiz
  @PostMapping("/api/clan/internal/score-update")
  public ResponseEntity<Void> receiveScoreUpdate(@RequestBody ScoreUpdateRequest request) {
    leagueService.handleScoreUpdate(request);
    return ResponseEntity.ok().build();
  }

  // Called by admin to end the current season and trigger promotions/degradations
  @PostMapping("/api/clan/admin/league/end-season")
  public ResponseEntity<Season> endSeason(@AuthenticationPrincipal Jwt jwt) {
    Season newSeason = leagueService.endSeason();
    return ResponseEntity.ok(newSeason);
  }

  // Returns info about the currently running season
  @GetMapping("/api/clan/league/current-season")
  public ResponseEntity<?> getCurrentSeason() {
    Season season = leagueService.getCurrentSeason();
    if (season == null) {
      return ResponseEntity.ok("No active season.");
    }
    return ResponseEntity.ok(season);
  }
}