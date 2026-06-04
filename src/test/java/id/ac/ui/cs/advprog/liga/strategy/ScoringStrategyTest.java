package id.ac.ui.cs.advprog.liga.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import id.ac.ui.cs.advprog.liga.model.ClanMember;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("checkstyle:MethodName")
class ScoringStrategyTest {

  private BronzeScoringStrategy bronzeStrategy;
  private SilverScoringStrategy silverStrategy;
  private GoldScoringStrategy goldStrategy;
  private DiamondScoringStrategy diamondStrategy;
  private ScoringStrategyFactory factory;

  private ClanMember memberWithScores(int quizScore, int missionScore, double accuracy) {
    ClanMember member = new ClanMember("user-" + quizScore);
    member.setQuizScore(quizScore);
    member.setMissionScore(missionScore);
    if (accuracy > 0.0) {
      member.setTotalQuizzes(1);
      member.setTotalAccuracy(accuracy);
    }
    return member;
  }

  @BeforeEach
  void setUp() {
    bronzeStrategy = new BronzeScoringStrategy();
    silverStrategy = new SilverScoringStrategy();
    goldStrategy = new GoldScoringStrategy();
    diamondStrategy = new DiamondScoringStrategy();
    factory = new ScoringStrategyFactory(
        bronzeStrategy, silverStrategy, goldStrategy, diamondStrategy);
  }

  // --- BronzeScoringStrategy ---

  @Test
  void bronze_returnsSumOfAllMemberSeasonScores() {
    List<ClanMember> members = List.of(
        memberWithScores(80, 10, 0.8),
        memberWithScores(60, 20, 0.6));
    assertEquals(170, bronzeStrategy.computeScore(members));
  }

  @Test
  void bronze_returnsZeroForEmptyList() {
    assertEquals(0, bronzeStrategy.computeScore(new ArrayList<>()));
  }

  @Test
  void bronze_returnsSingleMemberScore() {
    List<ClanMember> members = List.of(memberWithScores(100, 0, 1.0));
    assertEquals(100, bronzeStrategy.computeScore(members));
  }

  // --- SilverScoringStrategy ---

  @Test
  void silver_addsFivePointsPerMember() {
    List<ClanMember> members = List.of(
        memberWithScores(80, 10, 0.8),
        memberWithScores(60, 20, 0.6));
    int expected = 170 + (2 * 5);
    assertEquals(expected, silverStrategy.computeScore(members));
  }

  @Test
  void silver_returnsZeroPlusBonusForEmptyList() {
    assertEquals(0, silverStrategy.computeScore(new ArrayList<>()));
  }

  @Test
  void silver_singleMemberGetsFiveBonus() {
    List<ClanMember> members = List.of(memberWithScores(50, 10, 0.5));
    assertEquals(65, silverStrategy.computeScore(members));
  }

  // --- GoldScoringStrategy ---

  @Test
  void gold_returnsAverageSeasonScore() {
    List<ClanMember> members = List.of(
        memberWithScores(80, 20, 0.8),
        memberWithScores(60, 0, 0.6));
    int expected = 160 / 2;
    assertEquals(expected, goldStrategy.computeScore(members));
  }

  @Test
  void gold_returnsZeroForEmptyList() {
    assertEquals(0, goldStrategy.computeScore(new ArrayList<>()));
  }

  @Test
  void gold_singleMemberReturnsTheirOwnScore() {
    List<ClanMember> members = List.of(memberWithScores(90, 10, 0.9));
    assertEquals(100, goldStrategy.computeScore(members));
  }

  // --- DiamondScoringStrategy ---

  @Test
  void diamond_returnsZeroForEmptyList() {
    assertEquals(0, diamondStrategy.computeScore(new ArrayList<>()));
  }

  @Test
  void diamond_appliesWeightedFormula() {
    ClanMember member = memberWithScores(100, 0, 1.0);
    List<ClanMember> members = List.of(member);
    // score=100, accuracy=1.0
    // weighted = (100 * 0.6) + (1.0 * 100 * 0.4) = 60 + 40 = 100
    assertEquals(100, diamondStrategy.computeScore(members));
  }

  @Test
  void diamond_averagesAcrossMultipleMembers() {
    ClanMember first = memberWithScores(100, 0, 1.0);
    ClanMember second = memberWithScores(0, 0, 0.0);
    List<ClanMember> members = List.of(first, second);
    // first: (100*0.6)+(100*0.4) = 100
    // second: (0*0.6)+(0*0.4) = 0
    // average = 100/2 = 50
    assertEquals(50, diamondStrategy.computeScore(members));
  }

  // --- ScoringStrategyFactory ---

  @Test
  void factory_returnsBronzeStrategyForBronze() {
    ScoringStrategy strategy = factory.getStrategy("Bronze");
    assertEquals(bronzeStrategy, strategy);
  }

  @Test
  void factory_returnsSilverStrategyForSilver() {
    ScoringStrategy strategy = factory.getStrategy("Silver");
    assertEquals(silverStrategy, strategy);
  }

  @Test
  void factory_returnsGoldStrategyForGold() {
    ScoringStrategy strategy = factory.getStrategy("Gold");
    assertEquals(goldStrategy, strategy);
  }

  @Test
  void factory_returnsDiamondStrategyForPlatinum() {
    ScoringStrategy strategy = factory.getStrategy("Platinum");
    assertEquals(diamondStrategy, strategy);
  }

  @Test
  void factory_returnsDiamondStrategyForDiamond() {
    ScoringStrategy strategy = factory.getStrategy("Diamond");
    assertEquals(diamondStrategy, strategy);
  }

  @Test
  void factory_returnsBronzeStrategyForUnknownTier() {
    ScoringStrategy strategy = factory.getStrategy("Unknown");
    assertEquals(bronzeStrategy, strategy);
  }
}