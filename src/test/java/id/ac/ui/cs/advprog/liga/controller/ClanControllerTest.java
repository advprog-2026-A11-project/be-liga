package id.ac.ui.cs.advprog.liga.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.service.ClanService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@SuppressWarnings("checkstyle:MethodName")
@ExtendWith(MockitoExtension.class)
class ClanControllerTest {

  @Mock
  private ClanService service;

  @InjectMocks
  private ClanController controller;

  private Clan clan;
  private Jwt jwt;

  @BeforeEach
  void setUp() {
    clan = new Clan();
    clan.setClanName("TestClan");
    clan.setLeaderId("leader-1");

    jwt = mock(Jwt.class);
    // Use lenient so tests that don't touch the JWT don't fail
    lenient().when(jwt.getClaimAsString("yomu_user_id")).thenReturn("leader-1");
  }

  // --- listClans ---

  @Test
  void listClans_returnsAllClans() {
    when(service.findAll()).thenReturn(List.of(clan));
    List<Clan> result = controller.listClans();
    assertEquals(1, result.size());
  }

  // --- getClanMembers ---

  @Test
  void getClanMembers_returnsOkWithMemberList() {
    ClanMember member = new ClanMember("user-1");
    when(service.getMembersByClanId(clan.getClanId()))
        .thenReturn(List.of(member));
    ResponseEntity<List<ClanMember>> response = controller.getClanMembers(clan.getClanId());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  // --- getMembershipStatus ---

  @Test
  void getMembershipStatus_returnsTrueWhenInClan() {
    when(service.isUserInAnyClan("leader-1")).thenReturn(true);
    when(service.hasPendingApplication("leader-1")).thenReturn(false);
    ResponseEntity<Map<String, Object>> response = controller.getMembershipStatus(jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(true, response.getBody().get("inClan"));
    assertEquals(false, response.getBody().get("applying"));
  }

  // --- createClan ---

  @Test
  void createClan_returnsOkWhenValid() {
    when(service.isUserInAnyClan("leader-1")).thenReturn(false);
    when(service.hasPendingApplication("leader-1")).thenReturn(false);
    when(service.create(clan)).thenReturn(clan);
    ResponseEntity<?> response = controller.createClan(clan, jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).addMember(clan.getClanId(), "leader-1");
  }

  @Test
  void createClan_returnsBadRequestWhenAlreadyInClan() {
    when(service.isUserInAnyClan("leader-1")).thenReturn(true);
    ResponseEntity<?> response = controller.createClan(clan, jwt);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void createClan_returnsBadRequestWhenHasPendingApplication() {
    when(service.isUserInAnyClan("leader-1")).thenReturn(false);
    when(service.hasPendingApplication("leader-1")).thenReturn(true);
    ResponseEntity<?> response = controller.createClan(clan, jwt);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // --- detailClan ---

  @Test
  void detailClan_returnsOkWhenFound() {
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<Clan> response = controller.detailClan(clan.getClanId());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void detailClan_returnsNotFoundWhenMissing() {
    when(service.findById("missing")).thenReturn(null);
    ResponseEntity<Clan> response = controller.detailClan("missing");
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // --- editClan ---

  @Test
  void editClan_returnsOkWhenLeaderEdits() {
    Clan updated = new Clan();
    updated.setClanName("NewName");
    try {
      java.lang.reflect.Field field = Clan.class.getDeclaredField("clanId");
      field.setAccessible(true);
      field.set(updated, clan.getClanId());
    } catch (Exception ignored) {
      // test will fail naturally on assertion if reflection fails
    }
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.editClan(updated, jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void editClan_returnsForbiddenWhenNonLeaderEdits() {
    clan.setLeaderId("other-user");
    Clan updated = new Clan();
    try {
      java.lang.reflect.Field field = Clan.class.getDeclaredField("clanId");
      field.setAccessible(true);
      field.set(updated, clan.getClanId());
    } catch (Exception ignored) {
      // test will fail naturally on assertion if reflection fails
    }
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.editClan(updated, jwt);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  // --- deleteClan ---

  @Test
  void deleteClan_returnsOkWhenLeaderDeletes() {
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.deleteClan(clan.getClanId(), jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).delete(clan.getClanId());
  }

  @Test
  void deleteClan_returnsForbiddenWhenNonLeaderDeletes() {
    clan.setLeaderId("other-user");
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.deleteClan(clan.getClanId(), jwt);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void deleteClan_returnsNotFoundWhenClanMissing() {
    when(service.findById("missing")).thenReturn(null);
    ResponseEntity<?> response = controller.deleteClan("missing", jwt);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  // --- applyToClan ---

  @Test
  void applyToClan_returnsOkWhenEligible() {
    when(service.isUserInAnyClan("leader-1")).thenReturn(false);
    when(service.hasPendingApplication("leader-1")).thenReturn(false);
    ResponseEntity<?> response = controller.applyToClan(clan.getClanId(), jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).applyToClan(clan.getClanId(), "leader-1");
  }

  @Test
  void applyToClan_returnsBadRequestWhenAlreadyInClan() {
    when(service.isUserInAnyClan("leader-1")).thenReturn(true);
    ResponseEntity<?> response = controller.applyToClan(clan.getClanId(), jwt);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // --- cancelApplication ---

  @Test
  void cancelApplication_returnsOk() {
    ResponseEntity<?> response = controller.cancelApplication(clan.getClanId(), jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).cancelApplication(clan.getClanId(), "leader-1");
  }

  // --- quitClan ---

  @Test
  void quitClan_returnsBadRequestWhenLeaderTriesToQuit() {
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.quitClan(clan.getClanId(), jwt);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void quitClan_returnsOkWhenMemberQuits() {
    clan.setLeaderId("other-leader");
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.quitClan(clan.getClanId(), jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).removeMemberByUserId(clan.getClanId(), "leader-1");
  }

  // --- acceptApplicant ---

  @Test
  void acceptApplicant_returnsOkWhenLeaderAccepts() {
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.acceptApplicant(clan.getClanId(), "applicant-1", jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).acceptApplicant(clan.getClanId(), "applicant-1");
  }

  @Test
  void acceptApplicant_returnsForbiddenWhenNonLeaderAccepts() {
    clan.setLeaderId("other-user");
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.acceptApplicant(clan.getClanId(), "applicant-1", jwt);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  // --- rejectApplicant ---

  @Test
  void rejectApplicant_returnsOkWhenLeaderRejects() {
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.rejectApplicant(clan.getClanId(), "applicant-1", jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).rejectApplicant(clan.getClanId(), "applicant-1");
  }

  // --- kickMember ---

  @Test
  void kickMember_returnsOkWhenLeaderKicksNonLeader() {
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.kickMember(clan.getClanId(), "member-1", jwt);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(service).removeMemberByUserId(clan.getClanId(), "member-1");
  }

  @Test
  void kickMember_returnsBadRequestWhenLeaderKicksThemself() {
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.kickMember(clan.getClanId(), "leader-1", jwt);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void kickMember_returnsForbiddenWhenNonLeaderKicks() {
    clan.setLeaderId("other-user");
    when(service.findById(clan.getClanId())).thenReturn(clan);
    ResponseEntity<?> response = controller.kickMember(clan.getClanId(), "member-1", jwt);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}