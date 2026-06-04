package id.ac.ui.cs.advprog.liga.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

@SuppressWarnings("checkstyle:MethodName")
class ClanTest {

  @Test
  void constructor_generatesNonNullId() {
    Clan clan = new Clan();
    assertNotNull(clan.getClanId());
  }

  @Test
  void constructor_setsDefaultTierToBronze() {
    Clan clan = new Clan();
    assertEquals("Bronze", clan.getTier());
  }

  @Test
  void constructor_setsDefaultSeasonScoreToZero() {
    Clan clan = new Clan();
    assertEquals(0, clan.getSeasonScore());
  }

  @Test
  void constructor_setsDefaultScoreMultiplierToOne() {
    Clan clan = new Clan();
    assertEquals(1.0, clan.getScoreMultiplier(), 0.001);
  }

  @Test
  void constructor_initializesEmptyApplicantIds() {
    Clan clan = new Clan();
    assertNotNull(clan.getApplicantIds());
    assertTrue(clan.getApplicantIds().isEmpty());
  }

  @Test
  void setters_updateFields() {
    Clan clan = new Clan();
    clan.setClanName("TestClan");
    clan.setLeaderId("leader-123");
    clan.setTier("Gold");
    clan.setSeasonScore(500);
    clan.setScoreMultiplier(1.2);

    assertEquals("TestClan", clan.getClanName());
    assertEquals("leader-123", clan.getLeaderId());
    assertEquals("Gold", clan.getTier());
    assertEquals(500, clan.getSeasonScore());
    assertEquals(1.2, clan.getScoreMultiplier(), 0.001);
  }

  @Test
  void twoClans_haveDistinctIds() {
    Clan first = new Clan();
    Clan second = new Clan();
    assertTrue(!first.getClanId().equals(second.getClanId()));
  }
}