package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.ClanMember;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SilverScoringStrategy implements ScoringStrategy {

  @Override
  public int computeScore(List<ClanMember> members) {
    int raw = members.stream().mapToInt(ClanMember::getSeasonScore).sum();
    return raw + (members.size() * 5);
  }
}