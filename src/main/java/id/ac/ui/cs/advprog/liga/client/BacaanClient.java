package id.ac.ui.cs.advprog.liga.client;

import id.ac.ui.cs.advprog.liga.dto.UserStatsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class BacaanClient {

    private final WebClient webClient;

    public BacaanClient(@Qualifier("bacaanWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    // Fetches quiz stats for a single user from be-bacaan
    public UserStatsResponse getUserStats(String userId) {
        try {
            return webClient.get()
                    .uri("/api/student/readings/stats/{userId}", userId)
                    .retrieve()
                    .bodyToMono(UserStatsResponse.class)
                    .block();
        } catch (Exception e) {
            // If be-bacaan is down or returns an error, return empty stats
            // so be-liga doesn't crash
            UserStatsResponse empty = new UserStatsResponse();
            empty.setUserId(userId);
            return empty;
        }
    }
}