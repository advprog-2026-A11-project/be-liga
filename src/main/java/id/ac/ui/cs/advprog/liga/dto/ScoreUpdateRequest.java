package id.ac.ui.cs.advprog.liga.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreUpdateRequest {
    private String userId;
    private int score;
    private double accuracy;
}