package id.ac.ui.cs.advprog.liga.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clans")
@Getter
@Setter
public class Clan {

  @Id
  private String clanId;

  private String clanName;
  private String leaderId;
  private String tier = "Bronze";
  private int seasonScore = 0;
  private double scoreMultiplier = 1.0;

  // Applicants are still a simple list of user IDs
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "clan_applicants", joinColumns = @JoinColumn(name = "clan_id"))
  @Column(name = "applicant_user_id")
  private List<String> applicantIds = new ArrayList<>();

  public Clan() {
    this.clanId = UUID.randomUUID().toString();
  }
}