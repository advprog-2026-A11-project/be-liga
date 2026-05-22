package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClanServiceImpl implements ClanService {

  private final ClanRepository clanRepository;
  private final ClanMemberRepository clanMemberRepository;

  public ClanServiceImpl(ClanRepository clanRepository,
      ClanMemberRepository clanMemberRepository) {
    this.clanRepository = clanRepository;
    this.clanMemberRepository = clanMemberRepository;
  }

  @Override
  public Clan create(Clan clan) {
    return clanRepository.save(clan);
  }

  @Override
  public List<Clan> findAll() {
    List<Clan> clans = clanRepository.findAll();
    clans.sort((c1, c2) -> Integer.compare(c2.getSeasonScore(), c1.getSeasonScore()));
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
    // Remove all members from this clan (they stay in the registry, just unlinked)
    List<ClanMember> members = clanMemberRepository.findByClanId(id);
    for (ClanMember member : members) {
      member.setClanId(null);
    }
    clanMemberRepository.saveAll(members);
    clanRepository.deleteById(id);
  }

  @Override
  public void addMember(String clanId, String userId) {
    Clan clan = findById(clanId);
    if (clan == null) {
      return;
    }
    // Get or create the student's registry entry
    ClanMember member = clanMemberRepository.findByUserId(userId)
        .orElseGet(() -> new ClanMember(userId));

    if (member.getClanId() != null) {
      return; // already in a clan
    }

    member.setClanId(clanId);
    clanMemberRepository.save(member);
  }

  @Override
  public void removeMemberByUserId(String clanId, String userId) {
    clanMemberRepository.findByUserId(userId).ifPresent(member -> {
      if (clanId.equals(member.getClanId())) {
        member.setClanId(null);
        clanMemberRepository.save(member);
      }
    });
  }

  @Override
  public void applyToClan(String clanId, String userId) {
    Clan clan = findById(clanId);
    if (clan != null && !clan.getApplicantIds().contains(userId)) {
      clan.getApplicantIds().add(userId);
      clanRepository.save(clan);
    }
  }

  @Override
  public void acceptApplicant(String clanId, String applicantId) {
    Clan clan = findById(clanId);
    if (clan == null || !clan.getApplicantIds().contains(applicantId)) {
      return;
    }
    
    clan.getApplicantIds().remove(applicantId);
    clanRepository.save(clan);
    addMember(clanId, applicantId);
  }

  @Override
  public void rejectApplicant(String clanId, String applicantId) {
    Clan clan = findById(clanId);
    if (clan != null) {
      clan.getApplicantIds().remove(applicantId);
      clanRepository.save(clan);
    }
  }

  @Override
  public void cancelApplication(String clanId, String userId) {
    rejectApplicant(clanId, userId);
  }

  @Override
  public boolean isUserInAnyClan(String userId) {
    return clanMemberRepository.findByUserId(userId)
        .map(m -> m.getClanId() != null)
        .orElse(false);
  }

  @Override
  public boolean hasPendingApplication(String userId) {
    return clanRepository.findAll().stream()
        .anyMatch(clan -> clan.getApplicantIds().contains(userId));
  }
}