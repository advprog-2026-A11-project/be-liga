package id.ac.ui.cs.advprog.liga.controller;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.service.ClanService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clan")
@CrossOrigin(origins = "http://localhost:3000")
public class ClanController {

  @Autowired
  private ClanService service;

  @GetMapping("/list")
  public List<Clan> listClans() {
    return service.findAll();
  }

  @PostMapping("/create")
  public ResponseEntity<?> createClan(@RequestBody Clan clan, @AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject();

    // 1. Check if user is already in a clan
    if (service.isUserInAnyClan(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are already in a clan.");
    }

    // 2. Check if user has a pending application
    if (service.hasPendingApplication(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You cannot create a clan while you have a pending application. Please cancel it first.");
    }

    // 3. Set the creator as the leader
    clan.setLeaderId(userId);
    Clan createdClan = service.create(clan);

    // 4. Automatically add the leader as the first member with 0 score
    service.addMember(createdClan.getClanId(), userId, 0);

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
      // Security Check: Only the leader can edit the clan
      if (!existingClan.getLeaderId().equals(jwt.getSubject())) {
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
    if (clan == null) return ResponseEntity.notFound().build();

    // Security Check: Only the leader can delete the clan
    if (!clan.getLeaderId().equals(jwt.getSubject())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the clan leader can delete this clan.");
    }

    // Since we use JPA @ElementCollection, deleting the clan automatically wipes out its member list from the database!
    service.delete(id);
    return ResponseEntity.ok().build();
  }

  // --- Student Actions ---

  @PostMapping("/{id}/apply")
  public ResponseEntity<?> applyToClan(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject();

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
    String userId = jwt.getSubject();
    service.cancelApplication(id, userId);
    return ResponseEntity.ok("Application canceled.");
  }

  @DeleteMapping("/{id}/quit")
  public ResponseEntity<?> quitClan(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject();
    Clan clan = service.findById(id);

    // Business Logic: A leader cannot just quit. They must delete the clan (or we can add a 'transfer leadership' feature later).
    if (clan != null && clan.getLeaderId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Clan leaders cannot quit. You must delete the clan.");
    }

    service.removeMemberByUserId(id, userId);
    return ResponseEntity.ok().build();
  }

  // --- Leader Actions ---

  @PostMapping("/{id}/accept/{applicantId}")
  public ResponseEntity<?> acceptApplicant(@PathVariable String id, @PathVariable String applicantId, @AuthenticationPrincipal Jwt jwt) {
    Clan clan = service.findById(id);
    if (clan == null) return ResponseEntity.notFound().build();

    // Security: Only leader can accept
    if (!clan.getLeaderId().equals(jwt.getSubject())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the clan leader can accept applicants.");
    }

    service.acceptApplicant(id, applicantId);
    return ResponseEntity.ok("Applicant accepted!");
  }

  @PostMapping("/{id}/reject/{applicantId}")
  public ResponseEntity<?> rejectApplicant(@PathVariable String id, @PathVariable String applicantId, @AuthenticationPrincipal Jwt jwt) {
    Clan clan = service.findById(id);
    if (clan == null) return ResponseEntity.notFound().build();

    // Security: Only leader can reject
    if (!clan.getLeaderId().equals(jwt.getSubject())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the clan leader can reject applicants.");
    }

    service.rejectApplicant(id, applicantId);
    return ResponseEntity.ok("Applicant rejected.");
  }
}