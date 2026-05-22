package id.ac.ui.cs.advprog.liga.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserStatsResponse {
  private String userId;
  private long totalCompleted;
  private long completionFrequency;
  private double averageAccuracy;
}