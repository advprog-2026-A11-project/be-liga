package id.ac.ui.cs.advprog.liga.controller;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.service.ClanService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
  public ResponseEntity<Clan> createClan(@RequestBody Clan clan) {
    service.create(clan);
    return ResponseEntity.ok(clan);
  }

  @GetMapping("/detail/{id}")
  public ResponseEntity<Clan> detailClan(@PathVariable String id) {
    Clan clan = service.findById(id);
    return clan != null ? ResponseEntity.ok(clan) : ResponseEntity.notFound().build();
  }

  @PutMapping("/edit")
  public ResponseEntity<Clan> editClan(@RequestBody Clan updatedClan) {
    // 1. Find the existing clan in the database
    Clan existingClan = service.findById(updatedClan.getClanId());

    if (existingClan != null) {
      // 2. Only update the name
      existingClan.setClanName(updatedClan.getClanName());

      // 3. Save the object that still contains the original members
      service.update(existingClan);
      return ResponseEntity.ok(existingClan);
    }

    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> deleteClan(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.ok().build();
  }

  // --- Member Management ---

  @PostMapping("/{id}/add-member")
  public ResponseEntity<Void> addMember(@PathVariable String id, @RequestBody int score) {
    service.addMember(id, score);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{id}/edit-member/{index}")
  public ResponseEntity<Void> editMember(@PathVariable String id,
                                         @PathVariable int index,
                                         @RequestBody int score) {
    service.editMember(id, index, score);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}/delete-member/{index}")
  public ResponseEntity<Void> deleteMember(@PathVariable String id, @PathVariable int index) {
    service.deleteMember(id, index);
    return ResponseEntity.ok().build();
  }
}