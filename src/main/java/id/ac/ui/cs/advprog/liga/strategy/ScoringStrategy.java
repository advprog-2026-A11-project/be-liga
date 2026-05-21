package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.ClanMember;
import java.util.List;

public interface ScoringStrategy {
    int computeScore(List<ClanMember> members);
}