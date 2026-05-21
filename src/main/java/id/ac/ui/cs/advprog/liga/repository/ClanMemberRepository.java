package id.ac.ui.cs.advprog.liga.repository;

import id.ac.ui.cs.advprog.liga.model.ClanMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanMemberRepository extends JpaRepository<ClanMember, String> {

  // Find all members currently in a specific clan
  List<ClanMember> findByClanId(String clanId);

  // Find the clan member record for a specific user
  Optional<ClanMember> findByUserId(String userId);

  // Check if a student is registered in the system at all
  boolean existsByUserId(String userId);
}