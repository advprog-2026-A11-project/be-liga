package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClanServiceImpl implements ClanService {
    @Autowired
    private ClanRepository clanRepository;

    @Override public Clan create(Clan clan) { return clanRepository.create(clan); }
    @Override public List<Clan> findAll() { return clanRepository.findAll(); }
    @Override public Clan findById(String id) { return clanRepository.findById(id); }
    @Override public void update(Clan clan) { clanRepository.update(clan); }
    @Override public void delete(String id) { clanRepository.delete(id); }

    @Override
    public void addMember(String clanId, int score) {
        Clan clan = findById(clanId);
        if (clan != null) clan.getMemberScores().add(score);
    }

    @Override
    public void editMember(String clanId, int index, int score) {
        Clan clan = findById(clanId);
        if (clan != null && index < clan.getMemberScores().size()) {
            clan.getMemberScores().set(index, score);
        }
    }

    @Override
    public void deleteMember(String clanId, int index) {
        Clan clan = findById(clanId);
        if (clan != null && index < clan.getMemberScores().size()) {
            clan.getMemberScores().remove(index);
        }
    }
}