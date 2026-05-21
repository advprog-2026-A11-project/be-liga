package id.ac.ui.cs.advprog.liga.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seasons")
@Getter
@Setter
public class Season {

  @Id
  private String id;

  private int seasonNumber;

  private LocalDateTime startDate;

  // Null while season is still active
  private LocalDateTime endDate;

  private boolean active;

  public Season() {
    this.id = UUID.randomUUID().toString();
    this.startDate = LocalDateTime.now();
    this.active = true;
  }
}