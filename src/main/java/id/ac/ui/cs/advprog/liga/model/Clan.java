package id.ac.ui.cs.advprog.liga.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clans")
@Getter
@Setter
public class Clan {
  @Id
  private String clanId;
  private String clanName;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "clan_member_scores", joinColumns = @JoinColumn(name = "clan_id"))
  private List<Integer> memberScores;

  public Clan() {
    this.clanId = UUID.randomUUID().toString();
    this.memberScores = new ArrayList<>();
  }

  @Transient // Tells JPA not to try and save this as a column (it's calculated)
  public int getClanScore() {
    return memberScores.stream().mapToInt(Integer::intValue).sum();
  }

  @Transient
  public String getRankTier() {
    int score = getClanScore();
    if (score >= 4000) {
      return "Diamond";
    }
    if (score >= 3000) {
      return "Platinum";
    }
    if (score >= 2000) {
      return "Gold";
    }
    if (score >= 1000) {
      return "Silver";
    }
    return "Bronze";
  }
}