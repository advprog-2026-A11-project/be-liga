package id.ac.ui.cs.advprog.liga.repository;

import id.ac.ui.cs.advprog.liga.model.Season;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonRepository extends JpaRepository<Season, String> {

  // Finds the currently running season
  Optional<Season> findByActiveTrue();
}