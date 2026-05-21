package id.ac.ui.cs.advprog.liga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clan_members")
@Getter
@Setter
@NoArgsConstructor
public class ClanMember {

    @Id
    private String userId;

    // Which clan this student is currently in. Null if not in any clan.
    private String clanId;

    // Accumulated quiz scores this season (sum of per-quiz scores 0-100)
    private int quizScore = 0;

    // Accumulated mission reward points this season
    private int missionScore = 0;

    // Total quizzes completed across all time (never reset — used for accuracy)
    private int totalQuizzes = 0;

    // Total accuracy points accumulated across all time (never reset)
    // We store the sum so we can compute the true average: totalAccuracy / totalQuizzes
    private double totalAccuracy = 0.0;

    public ClanMember(String userId) {
        this.userId = userId;
    }

    // Returns lifetime average accuracy (0.0 to 1.0)
    // Returns 0.0 if the student has never completed a quiz
    public double getAccuracy() {
        if (totalQuizzes == 0) return 0.0;
        return totalAccuracy / totalQuizzes;
    }

    // Combined season score used by scoring strategies
    public int getSeasonScore() {
        return quizScore + missionScore;
    }
}