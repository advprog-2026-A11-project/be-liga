package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // Ensures database changes are saved properly
public class ClanServiceImpl implements ClanService {
  @Autowired
  private ClanRepository clanRepository;

  @Override
  public Clan create(Clan clan) {
    return clanRepository.save(clan); // .save() works for both create and update
  }

  @Override
  public List<Clan> findAll() {
    return clanRepository.findAll();
  }

  @Override
  public Clan findById(String id) {
    return clanRepository.findById(id).orElse(null);
  }

  @Override public void update(Clan clan) {
    clanRepository.save(clan);
  }

  @Override public void delete(String id) {
    clanRepository.deleteById(id);
  }

  @Override
  public void addMember(String clanId, int score) {
    Clan clan = findById(clanId);
    if (clan != null) {
      clan.getMemberScores().add(score);
      clanRepository.save(clan); // Must save to persist change
    }
  }

  @Override
  public void editMember(String clanId, int index, int score) {
    Clan clan = findById(clanId);
    if (clan != null && index < clan.getMemberScores().size()) {
      clan.getMemberScores().set(index, score);
      clanRepository.save(clan);
    }
  }

  @Override
  public void deleteMember(String clanId, int index) {
    Clan clan = findById(clanId);
    if (clan != null && index < clan.getMemberScores().size()) {
      clan.getMemberScores().remove(index);
      clanRepository.save(clan);
    }
  }
}