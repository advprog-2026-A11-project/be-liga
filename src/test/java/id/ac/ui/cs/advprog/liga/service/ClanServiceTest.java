package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClanServiceTest {
  @Mock
  private ClanRepository clanRepository;

  @InjectMocks
  private ClanServiceImpl clanService;

  @Test
  void testAddMember() {
    Clan clan = new Clan();
    when(clanRepository.findById(anyString())).thenReturn(clan);

    clanService.addMember("some-id", 100);
    assertEquals(1, clan.getMemberScores().size());
    assertEquals(100, clan.getMemberScores().get(0));
  }

  @Test
  void testDeleteMember() {
    Clan clan = new Clan();
    clan.getMemberScores().add(50);
    when(clanRepository.findById(anyString())).thenReturn(clan);

    clanService.deleteMember(clan.getClanId(), 0);
    assertTrue(clan.getMemberScores().isEmpty());
  }
}