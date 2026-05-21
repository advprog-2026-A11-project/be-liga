package id.ac.ui.cs.advprog.liga.client;

import id.ac.ui.cs.advprog.liga.dto.MissionScoreResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AchievementClient {

  private final WebClient webClient;

  public AchievementClient(@Qualifier("achievementWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  // Fetches the total daily mission reward points for a user
  // Returns 0 if be-achievement is unreachable
  public int getMissionScore(String userId) {
    try {
      Map response = webClient.get()
          .uri("/api/student-progress/{userId}/score", userId)
          .retrieve()
          .bodyToMono(Map.class)
          .block();

      if (response != null && response.containsKey("data")) {
        Map data = (Map) response.get("data");
        Object score = data.get("score");
        if (score instanceof Integer)
          return (Integer) score;
      }
      return 0;
    } catch (Exception e) {
      return 0;
    }
  }

  // Notifies be-achievement that a clan has promoted to Diamond
  // Your teammate needs to create this endpoint on their side
  public void notifyClanPromoted(String clanId, String tier) {
    try {
      webClient.post()
          .uri("/api/events/clan-promoted")
          .bodyValue(Map.of("clanId", clanId, "tier", tier))
          .retrieve()
          .bodyToMono(Void.class)
          .block();
    } catch (Exception e) {
      // Log and continue — don't let achievement failures break league logic
      System.err.println("Failed to notify achievement service of clan promotion: "
          + e.getMessage());
    }
  }
}