package id.ac.ui.cs.advprog.liga.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("checkstyle:MethodName")
@ExtendWith(MockitoExtension.class)
class ClanServiceImplTest {

  @Mock
  private ClanRepository clanRepository;

  @Mock
  private ClanMemberRepository clanMemberRepository;

  @InjectMocks
  private ClanServiceImpl clanService;

  private Clan clan;

  @BeforeEach
  void setUp() {
    clan = new Clan();
    clan.setClanName("TestClan");
    clan.setLeaderId("leader-1");
  }

  // --- create ---

  @Test
  void create_savesAndReturnsClan() {
    when(clanRepository.save(clan)).thenReturn(clan);
    Clan result = clanService.create(clan);
    assertNotNull(result);
    assertEquals("TestClan", result.getClanName());
    verify(clanRepository).save(clan);
  }

  // --- findAll ---

  @Test
  void findAll_returnsSortedBySeasonScoreDescending() {
    Clan low = new Clan();
    low.setSeasonScore(10);
    Clan high = new Clan();
    high.setSeasonScore(100);
    when(clanRepository.findAll()).thenReturn(new ArrayList<>(List.of(low, high)));

    List<Clan> result = clanService.findAll();
    assertEquals(100, result.get(0).getSeasonScore());
    assertEquals(10, result.get(1).getSeasonScore());
  }

  // --- findById ---

  @Test
  void findById_returnsClanWhenFound() {
    when(clanRepository.findById("clan-1")).thenReturn(Optional.of(clan));
    Clan result = clanService.findById("clan-1");
    assertNotNull(result);
  }

  @Test
  void findById_returnsNullWhenNotFound() {
    when(clanRepository.findById("missing")).thenReturn(Optional.empty());
    Clan result = clanService.findById("missing");
    assertNull(result);
  }

  // --- update ---

  @Test
  void update_savesClan() {
    clanService.update(clan);
    verify(clanRepository).save(clan);
  }

  // --- delete ---

  @Test
  void delete_unlinksAllMembersBeforeDeleting() {
    ClanMember member = new ClanMember("user-1");
    member.setClanId(clan.getClanId());
    when(clanMemberRepository.findByClanId(clan.getClanId()))
        .thenReturn(List.of(member));

    clanService.delete(clan.getClanId());

    assertNull(member.getClanId());
    verify(clanMemberRepository).saveAll(any());
    verify(clanRepository).deleteById(clan.getClanId());
  }

  // --- addMember ---

  @Test
  void addMember_addsMemberWhenNotAlreadyInClan() {
    ClanMember member = new ClanMember("user-1");
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));
    when(clanMemberRepository.findByUserId("user-1")).thenReturn(Optional.of(member));

    clanService.addMember(clan.getClanId(), "user-1");

    assertEquals(clan.getClanId(), member.getClanId());
    verify(clanMemberRepository).save(member);
  }

  @Test
  void addMember_doesNothingWhenClanNotFound() {
    when(clanRepository.findById("missing")).thenReturn(Optional.empty());
    clanService.addMember("missing", "user-1");
    verify(clanMemberRepository, never()).save(any());
  }

  @Test
  void addMember_doesNothingWhenMemberAlreadyInClan() {
    ClanMember member = new ClanMember("user-1");
    member.setClanId("other-clan");
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));
    when(clanMemberRepository.findByUserId("user-1")).thenReturn(Optional.of(member));

    clanService.addMember(clan.getClanId(), "user-1");

    assertEquals("other-clan", member.getClanId());
    verify(clanMemberRepository, never()).save(any());
  }

  @Test
  void addMember_createsNewRegistryEntryWhenUserUnknown() {
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));
    when(clanMemberRepository.findByUserId("new-user")).thenReturn(Optional.empty());

    clanService.addMember(clan.getClanId(), "new-user");

    verify(clanMemberRepository).save(any(ClanMember.class));
  }

  // --- removeMemberByUserId ---

  @Test
  void removeMemberByUserId_setsClanIdToNull() {
    ClanMember member = new ClanMember("user-1");
    member.setClanId(clan.getClanId());
    when(clanMemberRepository.findByUserId("user-1")).thenReturn(Optional.of(member));

    clanService.removeMemberByUserId(clan.getClanId(), "user-1");

    assertNull(member.getClanId());
    verify(clanMemberRepository).save(member);
  }

  @Test
  void removeMemberByUserId_doesNothingWhenUserInDifferentClan() {
    ClanMember member = new ClanMember("user-1");
    member.setClanId("other-clan");
    when(clanMemberRepository.findByUserId("user-1")).thenReturn(Optional.of(member));

    clanService.removeMemberByUserId(clan.getClanId(), "user-1");

    assertEquals("other-clan", member.getClanId());
    verify(clanMemberRepository, never()).save(any());
  }

  // --- applyToClan ---

  @Test
  void applyToClan_addsUserToApplicantList() {
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));
    clanService.applyToClan(clan.getClanId(), "user-1");
    assertTrue(clan.getApplicantIds().contains("user-1"));
    verify(clanRepository).save(clan);
  }

  @Test
  void applyToClan_doesNothingWhenAlreadyApplied() {
    clan.getApplicantIds().add("user-1");
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));

    clanService.applyToClan(clan.getClanId(), "user-1");

    assertEquals(1, clan.getApplicantIds().size());
    verify(clanRepository, never()).save(any());
  }

  // --- acceptApplicant ---

  @Test
  void acceptApplicant_removesFromApplicantsAndAddsMember() {
    clan.getApplicantIds().add("user-1");
    ClanMember member = new ClanMember("user-1");
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));
    when(clanMemberRepository.findByUserId("user-1")).thenReturn(Optional.of(member));

    clanService.acceptApplicant(clan.getClanId(), "user-1");

    assertFalse(clan.getApplicantIds().contains("user-1"));
    assertEquals(clan.getClanId(), member.getClanId());
  }

  @Test
  void acceptApplicant_doesNothingWhenApplicantNotInList() {
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));
    clanService.acceptApplicant(clan.getClanId(), "user-not-applied");
    verify(clanMemberRepository, never()).save(any());
  }

  // --- rejectApplicant ---

  @Test
  void rejectApplicant_removesFromApplicantList() {
    clan.getApplicantIds().add("user-1");
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));

    clanService.rejectApplicant(clan.getClanId(), "user-1");

    assertFalse(clan.getApplicantIds().contains("user-1"));
    verify(clanRepository).save(clan);
  }

  // --- cancelApplication ---

  @Test
  void cancelApplication_behavesLikeReject() {
    clan.getApplicantIds().add("user-1");
    when(clanRepository.findById(clan.getClanId())).thenReturn(Optional.of(clan));

    clanService.cancelApplication(clan.getClanId(), "user-1");

    assertFalse(clan.getApplicantIds().contains("user-1"));
  }

  // --- isUserInAnyClan ---

  @Test
  void isUserInAnyClan_returnsTrueWhenMemberHasClanId() {
    ClanMember member = new ClanMember("user-1");
    member.setClanId("some-clan");
    when(clanMemberRepository.findByUserId("user-1")).thenReturn(Optional.of(member));
    assertTrue(clanService.isUserInAnyClan("user-1"));
  }

  @Test
  void isUserInAnyClan_returnsFalseWhenClanIdIsNull() {
    ClanMember member = new ClanMember("user-1");
    when(clanMemberRepository.findByUserId("user-1")).thenReturn(Optional.of(member));
    assertFalse(clanService.isUserInAnyClan("user-1"));
  }

  @Test
  void isUserInAnyClan_returnsFalseWhenUserNotRegistered() {
    when(clanMemberRepository.findByUserId("unknown")).thenReturn(Optional.empty());
    assertFalse(clanService.isUserInAnyClan("unknown"));
  }

  // --- hasPendingApplication ---

  @Test
  void hasPendingApplication_returnsTrueWhenApplicantInAnyClan() {
    clan.getApplicantIds().add("user-1");
    when(clanRepository.findAll()).thenReturn(List.of(clan));
    assertTrue(clanService.hasPendingApplication("user-1"));
  }

  @Test
  void hasPendingApplication_returnsFalseWhenNotAppliedAnywhere() {
    when(clanRepository.findAll()).thenReturn(List.of(clan));
    assertFalse(clanService.hasPendingApplication("user-1"));
  }

  // --- getMembersByClanId ---

  @Test
  void getMembersByClanId_delegatesToRepository() {
    ClanMember member = new ClanMember("user-1");
    when(clanMemberRepository.findByClanId(clan.getClanId()))
        .thenReturn(List.of(member));

    List<ClanMember> result = clanService.getMembersByClanId(clan.getClanId());

    assertEquals(1, result.size());
    assertEquals("user-1", result.get(0).getUserId());
  }
}