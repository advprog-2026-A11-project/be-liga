package id.ac.ui.cs.advprog.liga.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClanTest {

  private Clan clan;

  @BeforeEach
  void setUp() {
    clan = new Clan();
    clan.setClanName("Test Clan");
    clan.setLeaderId("leader-001");
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testClanIdIsGeneratedOnCreation() {
    assertNotNull(clan.getClanId());
    assertFalse(clan.getClanId().isBlank());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testTwoClansHaveDifferentIds() {
    Clan another = new Clan();
    assertNotEquals(clan.getClanId(), another.getClanId());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetClanScore_NoMembers_ReturnsZero() {
    assertEquals(0, clan.getClanScore());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetClanScore_WithMembers_ReturnsSumOfScores() {
    clan.getMembers().add(new ClanMember("user-1", 500));
    clan.getMembers().add(new ClanMember("user-2", 300));
    assertEquals(800, clan.getClanScore());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetRankTier_Bronze_ScoreLessThan1000() {
    clan.getMembers().add(new ClanMember("user-1", 999));
    assertEquals("Bronze", clan.getRankTier());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetRankTier_Silver_ScoreExactly1000() {
    clan.getMembers().add(new ClanMember("user-1", 1000));
    assertEquals("Silver", clan.getRankTier());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetRankTier_Silver_ScoreBetween1000And1999() {
    clan.getMembers().add(new ClanMember("user-1", 1500));
    assertEquals("Silver", clan.getRankTier());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetRankTier_Gold_ScoreExactly2000() {
    clan.getMembers().add(new ClanMember("user-1", 2000));
    assertEquals("Gold", clan.getRankTier());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetRankTier_Platinum_ScoreExactly3000() {
    clan.getMembers().add(new ClanMember("user-1", 3000));
    assertEquals("Platinum", clan.getRankTier());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetRankTier_Diamond_ScoreExactly4000() {
    clan.getMembers().add(new ClanMember("user-1", 4000));
    assertEquals("Diamond", clan.getRankTier());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetRankTier_Diamond_ScoreAbove4000() {
    clan.getMembers().add(new ClanMember("user-1", 5000));
    assertEquals("Diamond", clan.getRankTier());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testGetRankTier_NoMembers_IsBronze() {
    assertEquals("Bronze", clan.getRankTier());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testMembersListIsInitiallyEmpty() {
    Clan newClan = new Clan();
    assertNotNull(newClan.getMembers());
    assertTrue(newClan.getMembers().isEmpty());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testApplicantIdsListIsInitiallyEmpty() {
    Clan newClan = new Clan();
    assertNotNull(newClan.getApplicantIds());
    assertTrue(newClan.getApplicantIds().isEmpty());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testSetAndGetClanName() {
    clan.setClanName("New Name");
    assertEquals("New Name", clan.getClanName());
  }

  @Test
  @SuppressWarnings("checkstyle:MethodName")
  void testSetAndGetLeaderId() {
    clan.setLeaderId("new-leader");
    assertEquals("new-leader", clan.getLeaderId());
  }
}