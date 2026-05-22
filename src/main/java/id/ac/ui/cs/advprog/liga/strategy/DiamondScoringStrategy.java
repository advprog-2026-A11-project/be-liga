package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.ClanMember;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiamondScoringStrategy implements ScoringStrategy {

  @Override
  public int computeScore(List<ClanMember> members) {
    if (members.isEmpty()) {
      return 0;
    }
    double weightedTotal = members.stream()
        .mapToDouble(m -> (m.getSeasonScore() * 0.6) + (m.getAccuracy() * 100 * 0.4))
        .sum();
    return (int) Math.round(weightedTotal / members.size());
  }
}