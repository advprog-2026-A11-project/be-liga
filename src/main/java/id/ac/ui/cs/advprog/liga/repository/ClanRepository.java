package id.ac.ui.cs.advprog.liga.repository;

import java.util.ArrayList;
import java.util.List;
import id.ac.ui.cs.advprog.liga.model.Clan;
import org.springframework.stereotype.Repository;

@Repository
public class ClanRepository {
  private List<Clan> clanData = new ArrayList<>();

  public Clan create(Clan clan) {
    clanData.add(clan);
    return clan;
  }

  public List<Clan> findAll() {
    return clanData;
  }

  public Clan findById(String id) {
    return clanData.stream()
            .filter(c -> c.getClanId().equals(id))
            .findFirst()
            .orElse(null);
  }

  public void update(Clan updatedClan) {
    for (int i = 0; i < clanData.size(); i++) {
      if (clanData.get(i).getClanId().equals(updatedClan.getClanId())) {
        clanData.set(i, updatedClan);
      }
    }
  }

  public void delete(String id) {
    clanData.removeIf(clan -> clan.getClanId().equals(id));
  }
}