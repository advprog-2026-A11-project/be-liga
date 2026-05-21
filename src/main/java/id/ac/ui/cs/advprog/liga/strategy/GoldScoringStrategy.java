package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.ClanMember;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoldScoringStrategy implements ScoringStrategy {

    @Override
    public int computeScore(List<ClanMember> members) {
        if (members.isEmpty()) return 0;
        return members.stream().mapToInt(ClanMember::getSeasonScore).sum() / members.size();
    }
}