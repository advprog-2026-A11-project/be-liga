package id.ac.ui.cs.advprog.liga.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SuppressWarnings("checkstyle:MethodName")
@ExtendWith(MockitoExtension.class)
class AchievementClientTest {

  private WebClient webClient;
  private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
  private WebClient.RequestHeadersSpec requestHeadersSpec;
  private WebClient.ResponseSpec responseSpec;
  private WebClient.RequestBodyUriSpec requestBodyUriSpec;
  private WebClient.RequestBodySpec requestBodySpec;
  private AchievementClient achievementClient;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    webClient = mock(WebClient.class);
    requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    responseSpec = mock(WebClient.ResponseSpec.class);
    requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    requestBodySpec = mock(WebClient.RequestBodySpec.class);
    achievementClient = new AchievementClient(webClient);
  }

  @Test
  @SuppressWarnings("unchecked")
  void getMissionScore_returnsScoreWhenResponseValid() {
    Map<String, Object> data = new HashMap<>();
    data.put("score", 15);
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("data", data);

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), anyString()))
        .thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(response));

    int result = achievementClient.getMissionScore("user-1");
    assertEquals(15, result);
  }

  @Test
  @SuppressWarnings("unchecked")
  void getMissionScore_returnsZeroWhenResponseIsNull() {
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), anyString()))
        .thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.empty());

    int result = achievementClient.getMissionScore("user-1");
    assertEquals(0, result);
  }

  @Test
  @SuppressWarnings("unchecked")
  void getMissionScore_returnsZeroOnException() {
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), anyString()))
        .thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("timeout"));

    int result = achievementClient.getMissionScore("user-1");
    assertEquals(0, result);
  }

  @Test
  @SuppressWarnings("unchecked")
  void notifyClanPromoted_doesNotThrowOnSuccess() {
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

    achievementClient.notifyClanPromoted("clan-1", "Diamond", List.of("user-1"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void notifyClanPromoted_doesNotThrowOnException() {
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("unreachable"));

    achievementClient.notifyClanPromoted("clan-1", "Diamond", List.of("user-1"));
  }
}