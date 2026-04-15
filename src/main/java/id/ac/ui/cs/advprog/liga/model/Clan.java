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
  private String leaderId; // NEW: Keeps track of the clan leader's Supabase User ID

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "clan_members", joinColumns = @JoinColumn(name = "clan_id"))
  private List<ClanMember> members = new ArrayList<>();

  public Clan() {
    this.clanId = UUID.randomUUID().toString();
  }

  @Transient
  public int getClanScore() {
    int score = 0;
    for (ClanMember member: members) {
      score += member.getScore();
    }
    return score;
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