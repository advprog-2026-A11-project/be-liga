package id.ac.ui.cs.advprog.liga.strategy;

import org.springframework.stereotype.Component;

@Component
public class ScoringStrategyFactory {

  private final BronzeScoringStrategy bronzeStrategy;
  private final SilverScoringStrategy silverStrategy;
  private final GoldScoringStrategy goldStrategy;
  private final DiamondScoringStrategy diamondStrategy;

  public ScoringStrategyFactory(
      BronzeScoringStrategy bronzeStrategy,
      SilverScoringStrategy silverStrategy,
      GoldScoringStrategy goldStrategy,
      DiamondScoringStrategy diamondStrategy) {
    this.bronzeStrategy = bronzeStrategy;
    this.silverStrategy = silverStrategy;
    this.goldStrategy = goldStrategy;
    this.diamondStrategy = diamondStrategy;
  }

  public ScoringStrategy getStrategy(String tier) {
    return switch (tier) {
      case "Silver" -> silverStrategy;
      case "Gold" -> goldStrategy;
      case "Diamond", "Platinum" -> diamondStrategy;
      default -> bronzeStrategy; // Bronze and anything unknown
    };
  }
}