package id.ac.ui.cs.advprog.liga.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    // Stored tier — only changes at end of season
    private String tier = "Bronze";

    // Accumulated score for the current season (resets each season)
    private int seasonScore = 0;

    // Current buff/debuff multiplier (1.0 = no buff, 1.2 = productivity buff, etc.)
    private double scoreMultiplier = 1.0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clan_members", joinColumns = @JoinColumn(name = "clan_id"))
    private List<ClanMember> members = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "clan_applicants", joinColumns = @JoinColumn(name = "clan_id"))
    @Column(name = "applicant_user_id")
    private List<String> applicantIds = new ArrayList<>();

    public Clan() {
        this.clanId = UUID.randomUUID().toString();
    }

    // Raw sum of member scores (used as input to scoring strategy)
    public int getRawScore() {
        int total = 0;
        for (ClanMember member : members) {
            total += member.getScore();
        }
        return total;
    }

    // Average accuracy across all members
    public double getAverageAccuracy() {
        if (members.isEmpty()) return 0.0;
        double total = 0.0;
        for (ClanMember member : members) {
            total += member.getAccuracy();
        }
        return total / members.size();
    }
}