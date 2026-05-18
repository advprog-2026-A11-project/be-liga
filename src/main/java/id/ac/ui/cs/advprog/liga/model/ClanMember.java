package id.ac.ui.cs.advprog.liga.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClanMember {

    private String userId;

    // Latest quiz score reported by be-bacaan
    private int score;

    // Latest quiz accuracy reported by be-bacaan (0.0 to 1.0)
    private double accuracy;
}