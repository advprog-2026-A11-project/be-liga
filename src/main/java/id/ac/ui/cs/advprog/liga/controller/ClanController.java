package id.ac.ui.cs.advprog.liga.controller;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.service.ClanService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clan")
@CrossOrigin(origins = "${frontend.url}")
public class ClanController {

  @Autowired
  private ClanService service;

  // NEW HELPER METHOD: Extracts the yomu_user_id from the token
  // We include a fallback to getSubject() just to be completely safe during the
  // transition!
  private String getUserIdFromToken(Jwt jwt) {
    String yomuUserId = jwt.getClaimAsString("yomu_user_id");
    return (yomuUserId != null && !yomuUserId.isBlank()) ? yomuUserId : jwt.getSubject();
  }

  @GetMapping("/list")
  public List<Clan> listClans() {
    return service.findAll();
  }

  @PostMapping("/create")
  public ResponseEntity<?> createClan(@RequestBody Clan clan, @AuthenticationPrincipal Jwt jwt) {
    // FIX: Use our new helper method
    String userId = getUserIdFromToken(jwt);

    // 1. Check if user is already in a clan
    if (service.isUserInAnyClan(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are already in a clan.");
    }

    // 2. Check if user has a pending application
    if (service.hasPendingApplication(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("You cannot create a clan while you have a pending application. Please cancel it first.");
    }

    // 3. Set the creator as the leader
    clan.setLeaderId(userId);
    Clan createdClan = service.create(clan);

    // 4. Automatically add the leader as the first member
    service.addMember(createdClan.getClanId(), userId);

    return ResponseEntity.ok(createdClan);
  }

  @GetMapping("/detail/{id}")
  public ResponseEntity<Clan> detailClan(@PathVariable String id) {
    Clan clan = service.findById(id);
    return clan != null ? ResponseEntity.ok(clan) : ResponseEntity.notFound().build();
  }

  @PutMapping("/edit")
  public ResponseEntity<?> editClan(@RequestBody Clan updatedClan, @AuthenticationPrincipal Jwt jwt) {
    Clan existingClan = service.findById(updatedClan.getClanId());

    if (existingClan != null) {
      // FIX: Use our new helper method for the Security Check
      if (!existingClan.getLeaderId().equals(getUserIdFromToken(jwt))) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the clan leader can edit this clan.");
      }

      existingClan.setClanName(updatedClan.getClanName());
      service.update(existingClan);
      return ResponseEntity.ok(existingClan);
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> deleteClan(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    Clan clan = service.findById(id);
    if (clan == null) {
      return ResponseEntity.notFound().build();
    }

    // FIX: Use our new helper method for the Security Check
    if (!clan.getLeaderId().equals(getUserIdFromToken(jwt))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the clan leader can delete this clan.");
    }

    service.delete(id);
    return ResponseEntity.ok().build();
  }

  // --- Student Actions ---

  @PostMapping("/{id}/apply")
  public ResponseEntity<?> applyToClan(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    // FIX: Use our new helper method
    String userId = getUserIdFromToken(jwt);

    if (service.isUserInAnyClan(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are already in a clan.");
    }
    if (service.hasPendingApplication(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You already have a pending application to a clan.");
    }

    service.applyToClan(id, userId);
    return ResponseEntity.ok("Application sent successfully!");
  }

  @DeleteMapping("/{id}/cancel-application")
  public ResponseEntity<?> cancelApplication(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    // FIX: Use our new helper method
    String userId = getUserIdFromToken(jwt);
    service.cancelApplication(id, userId);
    return ResponseEntity.ok("Application canceled.");
  }

  @DeleteMapping("/{id}/quit")
  public ResponseEntity<?> quitClan(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    // FIX: Use our new helper method
    String userId = getUserIdFromToken(jwt);
    Clan clan = service.findById(id);

    if (clan != null && clan.getLeaderId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Clan leaders cannot quit. You must delete the clan.");
    }

    service.removeMemberByUserId(id, userId);
    return ResponseEntity.ok().build();
  }

  // --- Leader Actions ---

  @PostMapping("/{id}/accept/{applicantId}")
  public ResponseEntity<?> acceptApplicant(@PathVariable String id, @PathVariable String applicantId,
      @AuthenticationPrincipal Jwt jwt) {
    Clan clan = service.findById(id);
    if (clan == null) {
      return ResponseEntity.notFound().build();
    }
    // FIX: Use our new helper method
    if (!clan.getLeaderId().equals(getUserIdFromToken(jwt))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the clan leader can accept applicants.");
    }

    service.acceptApplicant(id, applicantId);
    return ResponseEntity.ok("Applicant accepted!");
  }

  @PostMapping("/{id}/reject/{applicantId}")
  public ResponseEntity<?> rejectApplicant(@PathVariable String id, @PathVariable String applicantId,
      @AuthenticationPrincipal Jwt jwt) {
    Clan clan = service.findById(id);
    if (clan == null) {
      return ResponseEntity.notFound().build();
    }

    // FIX: Use our new helper method
    if (!clan.getLeaderId().equals(getUserIdFromToken(jwt))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the clan leader can reject applicants.");
    }

    service.rejectApplicant(id, applicantId);
    return ResponseEntity.ok("Applicant rejected.");
  }

  @DeleteMapping("/{id}/kick/{memberId}")
  public ResponseEntity<?> kickMember(@PathVariable String id, @PathVariable String memberId,
      @AuthenticationPrincipal Jwt jwt) {
    Clan clan = service.findById(id);
    if (clan == null) {
      return ResponseEntity.notFound().build();
    }

    // FIX: Use our new helper method
    if (!clan.getLeaderId().equals(getUserIdFromToken(jwt))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the clan leader can kick members.");
    }

    if (clan.getLeaderId().equals(memberId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("You cannot kick yourself. To leave, you must delete the clan.");
    }

    service.removeMemberByUserId(id, memberId);
    return ResponseEntity.ok("Member kicked successfully.");
  }
}