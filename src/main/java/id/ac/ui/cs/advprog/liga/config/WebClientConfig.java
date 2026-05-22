package id.ac.ui.cs.advprog.liga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Value("${services.bacaan.url}")
  private String bacaanUrl;

  @Value("${services.achievement.url}")
  private String achievementUrl;

  @Bean(name = "bacaanWebClient")
  public WebClient bacaanWebClient() {
    return WebClient.builder().baseUrl(bacaanUrl).build();
  }

  @Bean(name = "achievementWebClient")
  public WebClient achievementWebClient() {
    return WebClient.builder().baseUrl(achievementUrl).build();
  }
}