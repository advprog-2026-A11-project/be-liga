package id.ac.ui.cs.advprog.liga.repository;

import id.ac.ui.cs.advprog.liga.model.Clan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClanRepositoryTest {
  private ClanRepository clanRepository;

  @BeforeEach
  void setUp() {
    clanRepository = new ClanRepository();
  }

  @Test
  void testCreateAndFindById() {
    Clan clan = new Clan();
    clan.setClanName("Alpha");
    clanRepository.create(clan);

    Clan found = clanRepository.findById(clan.getClanId());
    assertEquals("Alpha", found.getClanName());
  }

  @Test
  void testUpdate() {
    Clan clan = new Clan();
    clan.setClanName("Old Name");
    clanRepository.create(clan);

    clan.setClanName("New Name");
    clanRepository.update(clan);

    assertEquals("New Name", clanRepository.findById(clan.getClanId()).getClanName());
  }

  @Test
  void testDelete() {
    Clan clan = new Clan();
    clanRepository.create(clan);
    clanRepository.delete(clan.getClanId());
    assertNull(clanRepository.findById(clan.getClanId()));
  }
}