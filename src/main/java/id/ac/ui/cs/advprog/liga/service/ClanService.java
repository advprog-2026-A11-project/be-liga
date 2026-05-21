package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.model.Clan;
import java.util.List;

public interface ClanService {
  Clan create(Clan clan);

  List<Clan> findAll();

  Clan findById(String id);

  void update(Clan clan);

  void delete(String id);

  // Member operations
  void addMember(String clanId, String userId);

  void removeMemberByUserId(String clanId, String userId);

  // Applicant operations
  void applyToClan(String clanId, String userId);

  void acceptApplicant(String clanId, String applicantId);

  void rejectApplicant(String clanId, String applicantId);

  void cancelApplication(String clanId, String userId);

  // Business logic checks
  boolean isUserInAnyClan(String userId);

  boolean hasPendingApplication(String userId);
}