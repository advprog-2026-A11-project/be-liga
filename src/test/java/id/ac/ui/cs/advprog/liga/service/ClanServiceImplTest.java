package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanServiceImplTest {

  @Mock
  private ClanRepository clanRepository;

  @InjectMocks
  private ClanServiceImpl clanService;

  private Clan clan;
  private final String CLAN_ID = "clan-001";
  private final String LEADER_ID = "leader-001";
  private final String USER_ID = "user-001";

  @BeforeEach
  void setUp() {
    clan = new Clan();
    clan.setClanId(CLAN_ID);
    clan.setClanName("Test Clan");
    clan.setLeaderId(LEADER_ID);
  }

  // --- CRUD ---

  @Test
  void testCreate_SavesAndReturnsClan() {
    when(clanRepository.save(clan)).thenReturn(clan);
    Clan result = clanService.create(clan);
    assertEquals(clan, result);
    verify(clanRepository, times(1)).save(clan);
  }

  @Test
  void testFindAll_ReturnsSortedByScoreDescending() {
    Clan clan1 = new Clan();
    clan1.getMembers().add(new ClanMember("u1", 100));

    Clan clan2 = new Clan();
    clan2.getMembers().add(new ClanMember("u2", 500));

    Clan clan3 = new Clan();
    clan3.getMembers().add(new ClanMember("u3", 300));

    when(clanRepository.findAll()).thenReturn(new ArrayList<>(List.of(clan1, clan2, clan3)));
    List<Clan> result = clanService.findAll();

    assertEquals(500, result.get(0).getClanScore());
    assertEquals(300, result.get(1).getClanScore());
    assertEquals(100, result.get(2).getClanScore());
  }

  @Test
  void testFindById_ExistingId_ReturnsClan() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));
    Clan result = clanService.findById(CLAN_ID);
    assertEquals(clan, result);
  }

  @Test
  void testFindById_NonExistingId_ReturnsNull() {
    when(clanRepository.findById("unknown")).thenReturn(Optional.empty());
    assertNull(clanService.findById("unknown"));
  }

  @Test
  void testUpdate_CallsSave() {
    clanService.update(clan);
    verify(clanRepository, times(1)).save(clan);
  }

  @Test
  void testDelete_CallsDeleteById() {
    clanService.delete(CLAN_ID);
    verify(clanRepository, times(1)).deleteById(CLAN_ID);
  }

  // --- addMember ---

  @Test
  void testAddMember_ClanExists_UserNotAlreadyMember_AddsMember() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));
    clanService.addMember(CLAN_ID, USER_ID, 100);

    assertEquals(1, clan.getMembers().size());
    assertEquals(USER_ID, clan.getMembers().get(0).getUserId());
    assertEquals(100, clan.getMembers().get(0).getScore());
    verify(clanRepository).save(clan);
  }

  @Test
  void testAddMember_UserAlreadyMember_DoesNotAddDuplicate() {
    clan.getMembers().add(new ClanMember(USER_ID, 50));
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));

    clanService.addMember(CLAN_ID, USER_ID, 100);

    assertEquals(1, clan.getMembers().size());
    verify(clanRepository, never()).save(any());
  }

  @Test
  void testAddMember_ClanNotFound_DoesNothing() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());
    clanService.addMember(CLAN_ID, USER_ID, 100);
    verify(clanRepository, never()).save(any());
  }

  // --- removeMemberByUserId ---

  @Test
  void testRemoveMemberByUserId_RemovesMember() {
    clan.getMembers().add(new ClanMember(USER_ID, 100));
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));

    clanService.removeMemberByUserId(CLAN_ID, USER_ID);

    assertTrue(clan.getMembers().isEmpty());
    verify(clanRepository).save(clan);
  }

  @Test
  void testRemoveMemberByUserId_ClanNotFound_DoesNothing() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());
    clanService.removeMemberByUserId(CLAN_ID, USER_ID);
    verify(clanRepository, never()).save(any());
  }

  // --- editMemberScore ---

  @Test
  void testEditMemberScore_UpdatesScore() {
    clan.getMembers().add(new ClanMember(USER_ID, 100));
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));

    clanService.editMemberScore(CLAN_ID, USER_ID, 999);

    assertEquals(999, clan.getMembers().get(0).getScore());
    verify(clanRepository).save(clan);
  }

  @Test
  void testEditMemberScore_UserNotInClan_DoesNothing() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));
    clanService.editMemberScore(CLAN_ID, USER_ID, 999);
    verify(clanRepository, never()).save(any());
  }

  @Test
  void testEditMemberScore_ClanNotFound_DoesNothing() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());
    clanService.editMemberScore(CLAN_ID, USER_ID, 999);
    verify(clanRepository, never()).save(any());
  }

  // --- isUserInAnyClan ---

  @Test
  void testIsUserInAnyClan_UserIsMember_ReturnsTrue() {
    clan.getMembers().add(new ClanMember(USER_ID, 100));
    when(clanRepository.findAll()).thenReturn(new ArrayList<>(List.of(clan)));

    assertTrue(clanService.isUserInAnyClan(USER_ID));
  }

  @Test
  void testIsUserInAnyClan_UserIsNotMember_ReturnsFalse() {
    when(clanRepository.findAll()).thenReturn(new ArrayList<>(List.of(clan)));
    assertFalse(clanService.isUserInAnyClan(USER_ID));
  }

  // --- hasPendingApplication ---

  @Test
  void testHasPendingApplication_UserHasApplied_ReturnsTrue() {
    clan.getApplicantIds().add(USER_ID);
    when(clanRepository.findAll()).thenReturn(new ArrayList<>(List.of(clan)));

    assertTrue(clanService.hasPendingApplication(USER_ID));
  }

  @Test
  void testHasPendingApplication_UserHasNotApplied_ReturnsFalse() {
    when(clanRepository.findAll()).thenReturn(new ArrayList<>(List.of(clan)));
    assertFalse(clanService.hasPendingApplication(USER_ID));
  }

  // --- applyToClan ---

  @Test
  void testApplyToClan_AddsUserToApplicants() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));
    clanService.applyToClan(CLAN_ID, USER_ID);

    assertTrue(clan.getApplicantIds().contains(USER_ID));
    verify(clanRepository).save(clan);
  }

  @Test
  void testApplyToClan_UserAlreadyApplied_DoesNotAddDuplicate() {
    clan.getApplicantIds().add(USER_ID);
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));

    clanService.applyToClan(CLAN_ID, USER_ID);

    assertEquals(1, clan.getApplicantIds().size());
    verify(clanRepository, never()).save(any());
  }

  @Test
  void testApplyToClan_ClanNotFound_DoesNothing() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());
    clanService.applyToClan(CLAN_ID, USER_ID);
    verify(clanRepository, never()).save(any());
  }

  // --- acceptApplicant ---

  @Test
  void testAcceptApplicant_MovesUserFromApplicantsToMembers() {
    clan.getApplicantIds().add(USER_ID);
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));

    clanService.acceptApplicant(CLAN_ID, USER_ID);

    assertFalse(clan.getApplicantIds().contains(USER_ID));
    assertTrue(clan.getMembers().stream().anyMatch(m -> m.getUserId().equals(USER_ID)));
    assertEquals(0, clan.getMembers().stream()
        .filter(m -> m.getUserId().equals(USER_ID))
        .findFirst().get().getScore());
    verify(clanRepository).save(clan);
  }

  @Test
  void testAcceptApplicant_UserNotInApplicants_DoesNothing() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));
    clanService.acceptApplicant(CLAN_ID, USER_ID);
    assertTrue(clan.getMembers().isEmpty());
    verify(clanRepository, never()).save(any());
  }

  @Test
  void testAcceptApplicant_ClanNotFound_DoesNothing() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());
    clanService.acceptApplicant(CLAN_ID, USER_ID);
    verify(clanRepository, never()).save(any());
  }

  // --- rejectApplicant ---

  @Test
  void testRejectApplicant_RemovesUserFromApplicants() {
    clan.getApplicantIds().add(USER_ID);
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));

    clanService.rejectApplicant(CLAN_ID, USER_ID);

    assertFalse(clan.getApplicantIds().contains(USER_ID));
    verify(clanRepository).save(clan);
  }

  @Test
  void testRejectApplicant_ClanNotFound_DoesNothing() {
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());
    clanService.rejectApplicant(CLAN_ID, USER_ID);
    verify(clanRepository, never()).save(any());
  }

  // --- cancelApplication ---

  @Test
  void testCancelApplication_DelegatesToRejectApplicant() {
    clan.getApplicantIds().add(USER_ID);
    when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(clan));

    clanService.cancelApplication(CLAN_ID, USER_ID);

    assertFalse(clan.getApplicantIds().contains(USER_ID));
    verify(clanRepository).save(clan);
  }
}