package id.ac.ui.cs.advprog.liga.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ClanMemberTest {

  @Test
  void testNoArgsConstructor() {
    ClanMember member = new ClanMember();
    assertNotNull(member);
    assertNull(member.getUserId());
    assertEquals(0, member.getScore());
  }

  @Test
  void testAllArgsConstructor() {
    ClanMember member = new ClanMember("user-123", 750);
    assertEquals("user-123", member.getUserId());
    assertEquals(750, member.getScore());
  }

  @Test
  void testSetScore() {
    ClanMember member = new ClanMember("user-123", 0);
    member.setScore(500);
    assertEquals(500, member.getScore());
  }

  @Test
  void testEquality_SameUserIdAndScore() {
    ClanMember m1 = new ClanMember("user-1", 100);
    ClanMember m2 = new ClanMember("user-1", 100);
    assertEquals(m1, m2);
  }

  @Test
  void testEquality_DifferentScore() {
    ClanMember m1 = new ClanMember("user-1", 100);
    ClanMember m2 = new ClanMember("user-1", 200);
    assertNotEquals(m1, m2);
  }
}