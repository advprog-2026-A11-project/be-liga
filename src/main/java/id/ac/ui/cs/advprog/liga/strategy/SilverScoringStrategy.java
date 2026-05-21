package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.ClanMember;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SilverScoringStrategy implements ScoringStrategy {

  @Override
  public int computeScore(List<ClanMember> members) {
    int raw = members.stream().mapToInt(ClanMember::getSeasonScore).sum();
    return raw + (members.size() * 5);
  }
}