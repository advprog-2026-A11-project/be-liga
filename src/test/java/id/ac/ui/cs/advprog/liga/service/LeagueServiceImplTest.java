package id.ac.ui.cs.advprog.liga.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.liga.client.AchievementClient;
import id.ac.ui.cs.advprog.liga.dto.ScoreUpdateRequest;
import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.model.Season;
import id.ac.ui.cs.advprog.liga.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import id.ac.ui.cs.advprog.liga.repository.SeasonRepository;
import id.ac.ui.cs.advprog.liga.strategy.BronzeScoringStrategy;
import id.ac.ui.cs.advprog.liga.strategy.DiamondScoringStrategy;
import id.ac.ui.cs.advprog.liga.strategy.GoldScoringStrategy;
import id.ac.ui.cs.advprog.liga.strategy.ScoringStrategyFactory;
import id.ac.ui.cs.advprog.liga.strategy.SilverScoringStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("checkstyle:MethodName")
@ExtendWith(MockitoExtension.class)
class LeagueServiceImplTest {

  @Mock
  private ClanRepository clanRepository;

  @Mock
  private ClanMemberRepository clanMemberRepository;

  @Mock
  private SeasonRepository seasonRepository;

  @Mock
  private AchievementClient achievementClient;

  private LeagueServiceImpl leagueService;

  @BeforeEach
  void setUp() {
    BronzeScoringStrategy bronze = new BronzeScoringStrategy();
    SilverScoringStrategy silver = new SilverScoringStrategy();
    GoldScoringStrategy gold = new GoldScoringStrategy();
    DiamondScoringStrategy diamond = new DiamondScoringStrategy();
    ScoringStrategyFactory factory = new ScoringStrategyFactory(bronze, silver, gold, diamond);

    leagueService = new LeagueServiceImpl(
        clanRepository,
        clanMemberRepository,
        seasonRepository,
        factory,
        achievementClient);
  }

  // --- handleScoreUpdate: new member registration ---

  @Test
  void handleScoreUpdate_registersNewMemberWhenUnknown() {
    ClanMember savedMember = new ClanMember("user-new");
    when(clanMemberRepository.findByUserId("user-new"))
        .thenReturn(Optional.empty());
    // First save creates the member, second save persists score update
    when(clanMemberRepository.save(any(ClanMember.class))).thenReturn(savedMember);

    ScoreUpdateRequest request = new ScoreUpdateRequest(
        "user-new", 
        80, 
        true, 
        0.8
    );
    leagueService.handleScoreUpdate(request);

    verify(clanMemberRepository, atLeastOnce()).save(any(ClanMember.class));
  }

  // --- handleScoreUpdate: quiz ---

  @Test
  void handleScoreUpdate_incrementsQuizScoreAndAccuracy() {
    ClanMember member = new ClanMember("user-1");
    member.setQuizScore(50);
    member.setTotalQuizzes(2);
    member.setTotalAccuracy(1.6);
    when(clanMemberRepository.findByUserId("user-1"))
        .thenReturn(Optional.of(member));

    ScoreUpdateRequest request = new ScoreUpdateRequest(
        "user-1", 
        80, 
        true, 
        0.8
    );
    leagueService.handleScoreUpdate(request);

    assertEquals(130, member.getQuizScore());
    assertEquals(3, member.getTotalQuizzes());
    assertEquals(2.4, member.getTotalAccuracy(), 0.001);
    verify(clanMemberRepository).save(member);
  }

  // --- handleScoreUpdate: mission ---

  @Test
  void handleScoreUpdate_incrementsMissionScore() {
    ClanMember member = new ClanMember("user-1");
    member.setMissionScore(10);
    when(clanMemberRepository.findByUserId("user-1"))
        .thenReturn(Optional.of(member));

    ScoreUpdateRequest request = new ScoreUpdateRequest(
        "user-1", 
        15, 
        false, 
        0.0
    );
    leagueService.handleScoreUpdate(request);

    assertEquals(25, member.getMissionScore());
    assertEquals(0, member.getTotalQuizzes());
  }

  // --- handleScoreUpdate: recalculation ---

  @Test
  void handleScoreUpdate_recalculatesClanScoreWhenMemberInClan() {
    ClanMember member = new ClanMember("user-1");
    member.setClanId("clan-1");
    Clan clan = new Clan();
    clan.setClanId("clan-1");
    clan.setTier("Bronze");

    when(clanMemberRepository.findByUserId("user-1"))
        .thenReturn(Optional.of(member));
    when(clanRepository.findById("clan-1"))
        .thenReturn(Optional.of(clan));
    when(clanMemberRepository.findByClanId("clan-1"))
        .thenReturn(List.of(member));
    when(achievementClient.getMissionScore("user-1")).thenReturn(0);

    ScoreUpdateRequest request = new ScoreUpdateRequest(
        "user-1", 
        80, 
        true, 
        0.8
    );
    leagueService.handleScoreUpdate(request);

    verify(clanRepository).save(clan);
  }

  @Test
  void handleScoreUpdate_doesNotRecalculateWhenMemberNotInClan() {
    ClanMember member = new ClanMember("user-1");
    when(clanMemberRepository.findByUserId("user-1"))
        .thenReturn(Optional.of(member));

    ScoreUpdateRequest request = new ScoreUpdateRequest(
        "user-1", 
        80, 
        true, 
        0.8
    );
    leagueService.handleScoreUpdate(request);

    verify(clanRepository, never()).findById(anyString());
  }

  // --- getCurrentSeason ---

  @Test
  void getCurrentSeason_returnsActiveSeasonWhenExists() {
    Season season = new Season();
    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.of(season));
    assertNotNull(leagueService.getCurrentSeason());
  }

  @Test
  void getCurrentSeason_returnsNullWhenNoActiveSeason() {
    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.empty());
    assertNull(leagueService.getCurrentSeason());
  }

  // --- endSeason: guard ---

  @Test
  void endSeason_throwsWhenNoActiveSeason() {
    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.empty());
    assertThrows(IllegalStateException.class, () -> leagueService.endSeason());
  }

  // --- endSeason: lifecycle ---

  @Test
  void endSeason_closesCurrentSeasonAndCreatesNew() {
    Season current = new Season();
    current.setSeasonNumber(1);
    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.of(current));
    when(clanRepository.findAll()).thenReturn(new ArrayList<>());
    when(clanMemberRepository.findAll()).thenReturn(new ArrayList<>());
    when(seasonRepository.save(any(Season.class))).thenReturn(new Season());

    leagueService.endSeason();

    assertEquals(false, current.isActive());
    assertNotNull(current.getEndDate());
  }

  // --- endSeason: promotion ---

  @Test
  void endSeason_promotesTopClansAndLeavesBottomInBronze() {
    Season current = new Season();
    current.setSeasonNumber(1);

    // Three clans so PROMOTION_SLOTS=2 promotes top two, bottom stays
    Clan topClan = new Clan();
    topClan.setTier("Bronze");
    topClan.setSeasonScore(300);

    Clan midClan = new Clan();
    midClan.setTier("Bronze");
    midClan.setSeasonScore(200);

    Clan bottomClan = new Clan();
    bottomClan.setTier("Bronze");
    bottomClan.setSeasonScore(50);

    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.of(current));
    when(clanRepository.findAll())
        .thenReturn(new ArrayList<>(List.of(topClan, midClan, bottomClan)));
    when(clanMemberRepository.findAll()).thenReturn(new ArrayList<>());
    when(seasonRepository.save(any(Season.class))).thenReturn(new Season());

    leagueService.endSeason();

    assertEquals("Silver", topClan.getTier());
    assertEquals("Silver", midClan.getTier());
    assertEquals("Bronze", bottomClan.getTier());
  }

  // --- endSeason: diamond notification ---

  @Test
  void endSeason_notifiesAchievementWhenClanPromotesToDiamond() {
    Season current = new Season();
    current.setSeasonNumber(1);

    Clan topClan = new Clan();
    topClan.setTier("Platinum");
    topClan.setSeasonScore(999);

    Clan midClan = new Clan();
    midClan.setTier("Platinum");
    midClan.setSeasonScore(500);

    Clan bottomClan = new Clan();
    bottomClan.setTier("Platinum");
    bottomClan.setSeasonScore(1);

    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.of(current));
    when(clanRepository.findAll())
        .thenReturn(new ArrayList<>(List.of(topClan, midClan, bottomClan)));
    lenient().when(clanMemberRepository.findByClanId(anyString()))
        .thenReturn(List.of());
    when(clanMemberRepository.findAll()).thenReturn(new ArrayList<>());
    when(seasonRepository.save(any(Season.class))).thenReturn(new Season());

    leagueService.endSeason();

    assertEquals("Diamond", topClan.getTier());
    verify(achievementClient, times(2)).notifyClanPromoted(
        anyString(), anyString(), anyList());
  }

  // --- endSeason: degradation ---

  @Test
  void endSeason_degradesBottomClanInSilverToBronze() {
    Season current = new Season();
    current.setSeasonNumber(1);

    Clan topClan = new Clan();
    topClan.setTier("Silver");
    topClan.setSeasonScore(300);

    Clan midClan = new Clan();
    midClan.setTier("Silver");
    midClan.setSeasonScore(200);

    Clan bottomClan = new Clan();
    bottomClan.setTier("Silver");
    bottomClan.setSeasonScore(10);

    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.of(current));
    when(clanRepository.findAll())
        .thenReturn(new ArrayList<>(List.of(topClan, midClan, bottomClan)));
    when(clanMemberRepository.findAll()).thenReturn(new ArrayList<>());
    when(seasonRepository.save(any(Season.class))).thenReturn(new Season());

    leagueService.endSeason();

    assertEquals("Bronze", bottomClan.getTier());
  }

  // --- endSeason: reset ---

  @Test
  void endSeason_resetsAllMemberSeasonScores() {
    Season current = new Season();
    current.setSeasonNumber(1);

    ClanMember member = new ClanMember("user-1");
    member.setQuizScore(100);
    member.setMissionScore(50);
    member.setTotalQuizzes(3);
    member.setTotalAccuracy(2.4);

    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.of(current));
    when(clanRepository.findAll()).thenReturn(new ArrayList<>());
    when(clanMemberRepository.findAll())
        .thenReturn(new ArrayList<>(List.of(member)));
    when(seasonRepository.save(any(Season.class))).thenReturn(new Season());

    leagueService.endSeason();

    assertEquals(0, member.getQuizScore());
    assertEquals(0, member.getMissionScore());
    // Accuracy must NOT be reset
    assertEquals(3, member.getTotalQuizzes());
    assertEquals(2.4, member.getTotalAccuracy(), 0.001);
  }

  @Test
  void endSeason_resetsClanSeasonScoreAndMultiplier() {
    Season current = new Season();
    current.setSeasonNumber(1);

    Clan clan = new Clan();
    clan.setTier("Bronze");
    clan.setSeasonScore(500);
    clan.setScoreMultiplier(1.2);

    when(seasonRepository.findByActiveTrue()).thenReturn(Optional.of(current));
    when(clanRepository.findAll()).thenReturn(new ArrayList<>(List.of(clan)));
    when(clanMemberRepository.findAll()).thenReturn(new ArrayList<>());
    when(seasonRepository.save(any(Season.class))).thenReturn(new Season());

    leagueService.endSeason();

    assertEquals(0, clan.getSeasonScore());
    assertEquals(1.0, clan.getScoreMultiplier(), 0.001);
  }

  // --- recalculateClanScore via handleScoreUpdate ---

  @Test
  void recalculateClanScore_appliesProductivityBuffWhenHalfMembersHaveMissions() {
    ClanMember memberA = new ClanMember("user-a");
    memberA.setClanId("clan-1");
    memberA.setQuizScore(100);
    memberA.setTotalQuizzes(1);
    memberA.setTotalAccuracy(0.8);

    ClanMember memberB = new ClanMember("user-b");
    memberB.setClanId("clan-1");
    memberB.setQuizScore(100);
    memberB.setTotalQuizzes(1);
    memberB.setTotalAccuracy(0.8);

    Clan clan = new Clan();
    clan.setClanId("clan-1");
    clan.setTier("Bronze");

    when(clanMemberRepository.findByUserId("user-a"))
        .thenReturn(Optional.of(memberA));
    when(clanRepository.findById("clan-1"))
        .thenReturn(Optional.of(clan));
    when(clanMemberRepository.findByClanId("clan-1"))
        .thenReturn(List.of(memberA, memberB));
    // 1 of 2 members has a mission score → 50% → buff applies
    when(achievementClient.getMissionScore("user-a")).thenReturn(10);
    when(achievementClient.getMissionScore("user-b")).thenReturn(0);

    ScoreUpdateRequest request = new ScoreUpdateRequest(
        "user-a", 
        0, 
        true, 
        0.8
    );
    leagueService.handleScoreUpdate(request);

    // Bronze raw = 200, multiplier = 1.2 → 240
    assertEquals(1.2, clan.getScoreMultiplier(), 0.001);
    assertEquals(240, clan.getSeasonScore());
  }

  @Test
  void recalculateClanScore_appliesDebuffWhenAverageAccuracyBelowFiftyPercent() {
    ClanMember member = new ClanMember("user-1");
    member.setClanId("clan-1");
    member.setQuizScore(100);
    member.setTotalQuizzes(1);
    member.setTotalAccuracy(0.3);

    Clan clan = new Clan();
    clan.setClanId("clan-1");
    clan.setTier("Bronze");

    when(clanMemberRepository.findByUserId("user-1"))
        .thenReturn(Optional.of(member));
    when(clanRepository.findById("clan-1"))
        .thenReturn(Optional.of(clan));
    when(clanMemberRepository.findByClanId("clan-1"))
        .thenReturn(List.of(member));
    when(achievementClient.getMissionScore("user-1")).thenReturn(0);

    ScoreUpdateRequest request = new ScoreUpdateRequest(
        "user-1", 
        0, 
        true, 
        0.3
    );
    leagueService.handleScoreUpdate(request);

    // Bronze raw = 100, multiplier = 0.8 → 80
    assertEquals(0.8, clan.getScoreMultiplier(), 0.001);
    assertEquals(80, clan.getSeasonScore());
  }
}