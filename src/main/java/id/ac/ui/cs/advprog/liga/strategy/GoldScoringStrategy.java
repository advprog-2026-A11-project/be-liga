package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.Clan;
import org.springframework.stereotype.Component;

@Component
public class GoldScoringStrategy implements ScoringStrategy {

    @Override
    public int computeScore(Clan clan) {
        if (clan.getMembers().isEmpty()) return 0;
        return clan.getRawScore() / clan.getMembers().size();
    }
}