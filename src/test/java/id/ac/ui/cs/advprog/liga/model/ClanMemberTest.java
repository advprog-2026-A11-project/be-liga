package id.ac.ui.cs.advprog.liga.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@SuppressWarnings("checkstyle:MethodName")
class ClanMemberTest {

  @Test
  void constructor_setsUserId() {
    ClanMember member = new ClanMember("user-1");
    assertEquals("user-1", member.getUserId());
  }

  @Test
  void constructor_setsDefaultScoresToZero() {
    ClanMember member = new ClanMember("user-1");
    assertEquals(0, member.getQuizScore());
    assertEquals(0, member.getMissionScore());
    assertEquals(0, member.getTotalQuizzes());
    assertEquals(0.0, member.getTotalAccuracy(), 0.001);
  }

  @Test
  void constructor_setsDefaultClanIdToNull() {
    ClanMember member = new ClanMember("user-1");
    assertEquals(null, member.getClanId());
  }

  @Test
  void getAccuracy_returnsZeroWhenNoQuizzes() {
    ClanMember member = new ClanMember("user-1");
    assertEquals(0.0, member.getAccuracy(), 0.001);
  }

  @Test
  void getAccuracy_returnsCorrectAverage() {
    ClanMember member = new ClanMember("user-1");
    member.setTotalQuizzes(4);
    member.setTotalAccuracy(3.2);
    assertEquals(0.8, member.getAccuracy(), 0.001);
  }

  @Test
  void getSeasonScore_returnsSumOfQuizAndMission() {
    ClanMember member = new ClanMember("user-1");
    member.setQuizScore(80);
    member.setMissionScore(20);
    assertEquals(100, member.getSeasonScore());
  }

  @Test
  void getSeasonScore_returnsZeroWhenBothAreZero() {
    ClanMember member = new ClanMember("user-1");
    assertEquals(0, member.getSeasonScore());
  }

  @Test
  void setters_updateFields() {
    ClanMember member = new ClanMember("user-1");
    member.setClanId("clan-abc");
    member.setQuizScore(50);
    member.setMissionScore(15);
    member.setTotalQuizzes(3);
    member.setTotalAccuracy(2.1);

    assertEquals("clan-abc", member.getClanId());
    assertEquals(50, member.getQuizScore());
    assertEquals(15, member.getMissionScore());
    assertEquals(3, member.getTotalQuizzes());
    assertEquals(2.1, member.getTotalAccuracy(), 0.001);
  }

  @Test
  void noArgsConstructor_setsAllDefaultsToZero() {
    ClanMember member = new ClanMember();
    assertEquals(0, member.getQuizScore());
    assertEquals(0, member.getMissionScore());
    assertEquals(0, member.getTotalQuizzes());
    assertEquals(0.0, member.getTotalAccuracy(), 0.001);
    assertEquals(0.0, member.getAccuracy(), 0.001);
  }
}