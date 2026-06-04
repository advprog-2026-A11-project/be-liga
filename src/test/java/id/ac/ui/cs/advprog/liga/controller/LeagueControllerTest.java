package id.ac.ui.cs.advprog.liga.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.liga.dto.ScoreUpdateRequest;
import id.ac.ui.cs.advprog.liga.model.Season;
import id.ac.ui.cs.advprog.liga.service.LeagueService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@SuppressWarnings("checkstyle:MethodName")
@ExtendWith(MockitoExtension.class)
class LeagueControllerTest {

  @Mock
  private LeagueService leagueService;

  @InjectMocks
  private LeagueController controller;

  private Jwt jwt;

  @BeforeEach
  void setUp() {
    jwt = org.mockito.Mockito.mock(Jwt.class);
  }

  @Test
  void receiveScoreUpdate_returnsOkAndDelegatesToService() {
    ScoreUpdateRequest request = new ScoreUpdateRequest("user-1", 80, true, 0.8);
    ResponseEntity<Void> response = controller.receiveScoreUpdate(request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(leagueService).handleScoreUpdate(request);
  }

  @Test
  void endSeason_returnsOkWithNewSeason() {
    Season newSeason = new Season();
    newSeason.setSeasonNumber(2);
    when(leagueService.endSeason()).thenReturn(newSeason);
    ResponseEntity<Season> response = controller.endSeason(jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getSeasonNumber());
  }

  @Test
  void getCurrentSeason_returnsSeasonWhenActive() {
    Season season = new Season();
    season.setSeasonNumber(1);
    when(leagueService.getCurrentSeason()).thenReturn(season);
    ResponseEntity<?> response = controller.getCurrentSeason();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(season, response.getBody());
  }

  @Test
  void getCurrentSeason_returnsInactiveMapWhenNoSeason() {
    when(leagueService.getCurrentSeason()).thenReturn(null);
    ResponseEntity<?> response = controller.getCurrentSeason();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    Map<?, ?> body = (Map<?, ?>) response.getBody();
    assertEquals(false, body.get("active"));
  }
}