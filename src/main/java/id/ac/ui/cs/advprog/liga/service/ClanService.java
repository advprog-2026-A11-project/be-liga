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
  void addMember(String clanId, int score);

  void editMember(String clanId, int memberIndex, int newScore);

  void deleteMember(String clanId, int memberIndex);
}