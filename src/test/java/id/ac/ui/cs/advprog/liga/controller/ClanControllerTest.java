package id.ac.ui.cs.advprog.liga.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.service.ClanService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class ClanControllerTest {

  @Mock
  private ClanService service;

  @InjectMocks
  private ClanController controller;

  private Clan clan;
  private Jwt jwt;

  private final String CLAN_ID = "clan-001";
  private final String LEADER_ID = "leader-001";
  private final String USER_ID = "user-001";

  @BeforeEach
  void setUp() {
    clan = new Clan();
    clan.setClanId(CLAN_ID);
    clan.setClanName("Test Clan");
    clan.setLeaderId(LEADER_ID);

    jwt = mock(Jwt.class);
  }

  // Helper to make JWT return yomu_user_id
  private void mockJwtUser(String userId) {
    when(jwt.getClaimAsString("yomu_user_id")).thenReturn(userId);
  }

  // Helper to make JWT fallback to subject (yomu_user_id is null)
  private void mockJwtSubject(String userId) {
    when(jwt.getClaimAsString("yomu_user_id")).thenReturn(null);
    when(jwt.getSubject()).thenReturn(userId);
  }

  // ===================== listClans =====================

  @Test
  void testListClans_ReturnsAllClans() {
    when(service.findAll()).thenReturn(List.of(clan));
    List<Clan> result = controller.listClans();
    assertEquals(1, result.size());
  }

  // ===================== createClan =====================

  @Test
  void testCreateClan_Success() {
    mockJwtUser(USER_ID);
    when(service.isUserInAnyClan(USER_ID)).thenReturn(false);
    when(service.hasPendingApplication(USER_ID)).thenReturn(false);
    when(service.create(clan)).thenReturn(clan);

    ResponseEntity<?> response = controller.createClan(clan, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(clan, response.getBody());
    verify(service).addMember(clan.getClanId(), USER_ID, 0);
  }

  @Test
  void testCreateClan_UserAlreadyInClan_ReturnsBadRequest() {
    mockJwtUser(USER_ID);
    when(service.isUserInAnyClan(USER_ID)).thenReturn(true);

    ResponseEntity<?> response = controller.createClan(clan, jwt);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("You are already in a clan.", response.getBody());
    verify(service, never()).create(any());
  }

  @Test
  void testCreateClan_UserHasPendingApplication_ReturnsBadRequest() {
    mockJwtUser(USER_ID);
    when(service.isUserInAnyClan(USER_ID)).thenReturn(false);
    when(service.hasPendingApplication(USER_ID)).thenReturn(true);

    ResponseEntity<?> response = controller.createClan(clan, jwt);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(service, never()).create(any());
  }

  @Test
  void testCreateClan_FallbackToSubjectWhenYomuUserIdBlank() {
    when(jwt.getClaimAsString("yomu_user_id")).thenReturn("  "); // blank
    when(jwt.getSubject()).thenReturn(LEADER_ID);
    when(service.isUserInAnyClan(LEADER_ID)).thenReturn(false);
    when(service.hasPendingApplication(LEADER_ID)).thenReturn(false);
    when(service.create(clan)).thenReturn(clan);

    ResponseEntity<?> response = controller.createClan(clan, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).addMember(clan.getClanId(), LEADER_ID, 0);
  }

  // ===================== detailClan =====================

  @Test
  void testDetailClan_Found_ReturnsOk() {
    when(service.findById(CLAN_ID)).thenReturn(clan);
    ResponseEntity<Clan> response = controller.detailClan(CLAN_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(clan, response.getBody());
  }

  @Test
  void testDetailClan_NotFound_Returns404() {
    when(service.findById(CLAN_ID)).thenReturn(null);
    ResponseEntity<Clan> response = controller.detailClan(CLAN_ID);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // ===================== editClan =====================

  @Test
  void testEditClan_Success() {
    mockJwtUser(LEADER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    Clan updatedClan = new Clan();
    updatedClan.setClanId(CLAN_ID);
    updatedClan.setClanName("New Name");

    ResponseEntity<?> response = controller.editClan(updatedClan, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("New Name", clan.getClanName());
    verify(service).update(clan);
  }

  @Test
  void testEditClan_NotLeader_ReturnsForbidden() {
    mockJwtUser(USER_ID); // not the leader
    when(service.findById(CLAN_ID)).thenReturn(clan);

    Clan updatedClan = new Clan();
    updatedClan.setClanId(CLAN_ID);
    updatedClan.setClanName("Hacked Name");

    ResponseEntity<?> response = controller.editClan(updatedClan, jwt);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(service, never()).update(any());
  }

  @Test
  void testEditClan_ClanNotFound_Returns404() {
    when(service.findById(any())).thenReturn(null);
    Clan updatedClan = new Clan();
    updatedClan.setClanId(CLAN_ID);

    ResponseEntity<?> response = controller.editClan(updatedClan, jwt);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // ===================== deleteClan =====================

  @Test
  void testDeleteClan_Success() {
    mockJwtUser(LEADER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.deleteClan(CLAN_ID, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).delete(CLAN_ID);
  }

  @Test
  void testDeleteClan_NotLeader_ReturnsForbidden() {
    mockJwtUser(USER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.deleteClan(CLAN_ID, jwt);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(service, never()).delete(any());
  }

  @Test
  void testDeleteClan_NotFound_Returns404() {
    when(service.findById(CLAN_ID)).thenReturn(null);
    ResponseEntity<?> response = controller.deleteClan(CLAN_ID, jwt);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // ===================== applyToClan =====================

  @Test
  void testApplyToClan_Success() {
    mockJwtUser(USER_ID);
    when(service.isUserInAnyClan(USER_ID)).thenReturn(false);
    when(service.hasPendingApplication(USER_ID)).thenReturn(false);

    ResponseEntity<?> response = controller.applyToClan(CLAN_ID, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).applyToClan(CLAN_ID, USER_ID);
  }

  @Test
  void testApplyToClan_AlreadyInClan_ReturnsBadRequest() {
    mockJwtUser(USER_ID);
    when(service.isUserInAnyClan(USER_ID)).thenReturn(true);

    ResponseEntity<?> response = controller.applyToClan(CLAN_ID, jwt);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(service, never()).applyToClan(any(), any());
  }

  @Test
  void testApplyToClan_AlreadyHasPendingApplication_ReturnsBadRequest() {
    mockJwtUser(USER_ID);
    when(service.isUserInAnyClan(USER_ID)).thenReturn(false);
    when(service.hasPendingApplication(USER_ID)).thenReturn(true);

    ResponseEntity<?> response = controller.applyToClan(CLAN_ID, jwt);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(service, never()).applyToClan(any(), any());
  }

  // ===================== cancelApplication =====================

  @Test
  void testCancelApplication_ReturnsOk() {
    mockJwtUser(USER_ID);
    ResponseEntity<?> response = controller.cancelApplication(CLAN_ID, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).cancelApplication(CLAN_ID, USER_ID);
  }

  // ===================== quitClan =====================

  @Test
  void testQuitClan_Success_NonLeader() {
    mockJwtUser(USER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan); // leader is LEADER_ID, not USER_ID

    ResponseEntity<?> response = controller.quitClan(CLAN_ID, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).removeMemberByUserId(CLAN_ID, USER_ID);
  }

  @Test
  void testQuitClan_Leader_ReturnsBadRequest() {
    mockJwtUser(LEADER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.quitClan(CLAN_ID, jwt);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(service, never()).removeMemberByUserId(any(), any());
  }

  @Test
  void testQuitClan_ClanNotFound_StillCallsRemove() {
    mockJwtUser(USER_ID);
    when(service.findById(CLAN_ID)).thenReturn(null);

    ResponseEntity<?> response = controller.quitClan(CLAN_ID, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).removeMemberByUserId(CLAN_ID, USER_ID);
  }

  // ===================== acceptApplicant =====================

  @Test
  void testAcceptApplicant_Success() {
    mockJwtUser(LEADER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.acceptApplicant(CLAN_ID, USER_ID, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).acceptApplicant(CLAN_ID, USER_ID);
  }

  @Test
  void testAcceptApplicant_NotLeader_ReturnsForbidden() {
    mockJwtUser(USER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.acceptApplicant(CLAN_ID, "applicant-999", jwt);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(service, never()).acceptApplicant(any(), any());
  }

  @Test
  void testAcceptApplicant_ClanNotFound_Returns404() {
    when(service.findById(CLAN_ID)).thenReturn(null);
    ResponseEntity<?> response = controller.acceptApplicant(CLAN_ID, USER_ID, jwt);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // ===================== rejectApplicant =====================

  @Test
  void testRejectApplicant_Success() {
    mockJwtUser(LEADER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.rejectApplicant(CLAN_ID, USER_ID, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).rejectApplicant(CLAN_ID, USER_ID);
  }

  @Test
  void testRejectApplicant_NotLeader_ReturnsForbidden() {
    mockJwtUser(USER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.rejectApplicant(CLAN_ID, "applicant-999", jwt);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(service, never()).rejectApplicant(any(), any());
  }

  @Test
  void testRejectApplicant_ClanNotFound_Returns404() {
    when(service.findById(CLAN_ID)).thenReturn(null);
    ResponseEntity<?> response = controller.rejectApplicant(CLAN_ID, USER_ID, jwt);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // ===================== kickMember =====================

  @Test
  void testKickMember_Success() {
    mockJwtUser(LEADER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.kickMember(CLAN_ID, USER_ID, jwt);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).removeMemberByUserId(CLAN_ID, USER_ID);
  }

  @Test
  void testKickMember_NotLeader_ReturnsForbidden() {
    mockJwtUser(USER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    ResponseEntity<?> response = controller.kickMember(CLAN_ID, "victim-user", jwt);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(service, never()).removeMemberByUserId(any(), any());
  }

  @Test
  void testKickMember_KickSelf_ReturnsBadRequest() {
    mockJwtUser(LEADER_ID);
    when(service.findById(CLAN_ID)).thenReturn(clan);

    // Leader tries to kick themselves
    ResponseEntity<?> response = controller.kickMember(CLAN_ID, LEADER_ID, jwt);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(service, never()).removeMemberByUserId(any(), any());
  }

  @Test
  void testKickMember_ClanNotFound_Returns404() {
    when(service.findById(CLAN_ID)).thenReturn(null);
    ResponseEntity<?> response = controller.kickMember(CLAN_ID, USER_ID, jwt);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}