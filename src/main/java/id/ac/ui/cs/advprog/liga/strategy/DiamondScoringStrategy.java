package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import org.springframework.stereotype.Component;

@Component
public class DiamondScoringStrategy implements ScoringStrategy {

    @Override
    public int computeScore(Clan clan) {
        if (clan.getMembers().isEmpty()) return 0;

        double weightedTotal = 0.0;
        for (ClanMember member : clan.getMembers()) {
            // Score is weighted 60%, accuracy (scaled to 100) is weighted 40%
            double memberWeightedScore = (member.getScore() * 0.6)
                    + ((member.getAccuracy() * 100) * 0.4);
            weightedTotal += memberWeightedScore;
        }

        return (int) Math.round(weightedTotal / clan.getMembers().size());
    }
}