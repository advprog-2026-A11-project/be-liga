package id.ac.ui.cs.advprog.liga.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClanTest {
  private Clan clan;

  @BeforeEach
  void setUp() {
    this.clan = new Clan();
  }

  @Test
  void testClanConstructor() {
    assertNotNull(clan.getClanId());
    assertNotNull(clan.getMemberScores());
    assertTrue(clan.getMemberScores().isEmpty());
  }

  @Test
  void testGetClanScore() {
    clan.getMemberScores().add(10);
    clan.getMemberScores().add(20);
    assertEquals(30, clan.getClanScore());
  }
}