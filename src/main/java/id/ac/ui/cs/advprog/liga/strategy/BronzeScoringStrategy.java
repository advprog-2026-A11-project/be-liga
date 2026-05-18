package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.Clan;
import org.springframework.stereotype.Component;

@Component
public class BronzeScoringStrategy implements ScoringStrategy {

    @Override
    public int computeScore(Clan clan) {
        return clan.getRawScore();
    }
}