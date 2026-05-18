package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.Clan;
import org.springframework.stereotype.Component;

@Component
public class SilverScoringStrategy implements ScoringStrategy {

    @Override
    public int computeScore(Clan clan) {
        int raw = clan.getRawScore();
        int memberCount = clan.getMembers().size();
        // Small participation bonus: +5 per member
        return raw + (memberCount * 5);
    }
}