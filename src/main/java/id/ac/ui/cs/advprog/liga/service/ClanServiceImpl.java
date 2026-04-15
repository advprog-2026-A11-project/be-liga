package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClanServiceImpl implements ClanService {

  @Autowired
  private ClanRepository clanRepository;

  @Override
  public Clan create(Clan clan) {
    return clanRepository.save(clan);
  }

  @Override
  public List<Clan> findAll() {
    List<Clan> clans = clanRepository.findAll();
    clans.sort((c1, c2) -> Integer.compare(c2.getClanScore(), c1.getClanScore()));
    return clans;
  }

  @Override
  public Clan findById(String id) {
    return clanRepository.findById(id).orElse(null);
  }

  @Override
  public void update(Clan clan) {
    clanRepository.save(clan);
  }

  @Override
  public void delete(String id) {
    clanRepository.deleteById(id);
  }

  // --- Updated Member Operations ---

  @Override
  public void addMember(String clanId, String userId, int score) {
    Clan clan = findById(clanId);
    if (clan != null) {
      // Check if user is already in the clan to prevent duplicates
      boolean alreadyExists = clan.getMembers().stream()
              .anyMatch(member -> member.getUserId().equals(userId));

      if (!alreadyExists) {
        clan.getMembers().add(new ClanMember(userId, score));
        clanRepository.save(clan);
      }
    }
  }

  @Override
  public void removeMemberByUserId(String clanId, String userId) {
    Clan clan = findById(clanId);
    if (clan != null) {
      // Safely removes the member if the userId matches
      clan.getMembers().removeIf(member -> member.getUserId().equals(userId));
      clanRepository.save(clan);
    }
  }

  @Override
  public void editMemberScore(String clanId, String userId, int newScore) {
    Clan clan = findById(clanId);
    if (clan != null) {
      // Find the specific member and update their score
      clan.getMembers().stream()
              .filter(member -> member.getUserId().equals(userId))
              .findFirst()
              .ifPresent(member -> {
                member.setScore(newScore);
                clanRepository.save(clan);
              });
    }
  }

  @Override
  public boolean isUserInAnyClan(String userId) {
    // Checks all clans to see if this userId exists in any member list
    return findAll().stream()
            .anyMatch(clan -> clan.getMembers().stream()
                    .anyMatch(member -> member.getUserId().equals(userId)));
  }
}