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
    private String userId;  // This will store the Supabase User ID from be-auth
    private int score;
}