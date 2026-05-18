package id.ac.ui.cs.advprog.liga.strategy;

import id.ac.ui.cs.advprog.liga.model.Clan;

public interface ScoringStrategy {
    int computeScore(Clan clan);
}