package id.ac.ui.cs.advprog.liga.model;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class Clan {
  private String clanId;
  private String clanName;
  private List<Integer> memberScores;

  public Clan() {
    this.clanId = UUID.randomUUID().toString();
    this.memberScores = new ArrayList<>();
  }

  // Calculated attribute: Sum of all member scores
  public int getClanScore() {
    return memberScores.stream().mapToInt(Integer::intValue).sum();
  }
}