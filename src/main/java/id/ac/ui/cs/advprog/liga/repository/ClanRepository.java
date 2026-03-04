package id.ac.ui.cs.advprog.liga.repository;

import id.ac.ui.cs.advprog.liga.model.Clan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanRepository extends JpaRepository<Clan, String> {
  // Magic! save(), findAll(), findById(), and deleteById() are now built-in.
}