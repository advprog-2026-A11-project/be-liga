package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.client.AchievementClient;
import id.ac.ui.cs.advprog.liga.client.BacaanClient;
import id.ac.ui.cs.advprog.liga.dto.ScoreUpdateRequest;
import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.model.Season;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import id.ac.ui.cs.advprog.liga.repository.SeasonRepository;
import id.ac.ui.cs.advprog.liga.strategy.ScoringStrategy;
import id.ac.ui.cs.advprog.liga.strategy.ScoringStrategyFactory;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeagueServiceImpl implements LeagueService {

    // How many clans promote/degrade per tier per season end
    private static final int PROMOTION_SLOTS = 2;
    private static final int DEGRADATION_SLOTS = 2;

    private static final List<String> TIER_ORDER =
            List.of("Bronze", "Silver", "Gold", "Platinum", "Diamond");

    private final ClanRepository clanRepository;
    private final SeasonRepository seasonRepository;
    private final ScoringStrategyFactory scoringStrategyFactory;
    private final AchievementClient achievementClient;
    private final BacaanClient bacaanClient;

    public LeagueServiceImpl(
            ClanRepository clanRepository,
            SeasonRepository seasonRepository,
            ScoringStrategyFactory scoringStrategyFactory,
            AchievementClient achievementClient,
            BacaanClient bacaanClient) {
        this.clanRepository = clanRepository;
        this.seasonRepository = seasonRepository;
        this.scoringStrategyFactory = scoringStrategyFactory;
        this.achievementClient = achievementClient;
        this.bacaanClient = bacaanClient;
    }

    @Override
    public void handleScoreUpdate(ScoreUpdateRequest request) {
        String userId = request.getUserId();

        Clan userClan = clanRepository.findAll().stream()
                .filter(clan -> clan.getMembers().stream()
                        .anyMatch(m -> m.getUserId().equals(userId)))
                .findFirst()
                .orElse(null);

        // Student isn't in any clan — nothing to do
        if (userClan == null) return;

        List<ClanMember> members = userClan.getMembers();
        for (int i = 0; i < members.size(); i++) {
            ClanMember member = members.get(i);
            if (member.getUserId().equals(userId)) {
                int newScore = member.getScore() + request.getScore();

                // Only update accuracy if this update comes from a quiz (accuracy > 0)
                // Missions don't affect accuracy
                double newAccuracy;
                if (request.getAccuracy() > 0.0) {
                    // Running average: weight existing average against the new reading
                    // We don't store quiz count so use a simple smoothing approach:
                    // new average = (old * 0.8) + (new * 0.2)
                    // This is an approximation but avoids needing a separate quiz count field
                    newAccuracy = (member.getAccuracy() * 0.8) + (request.getAccuracy() * 0.2);
                } else {
                    newAccuracy = member.getAccuracy();
                }

                members.set(i, new ClanMember(userId, newScore, newAccuracy));
                break;
            }
        }

        recalculateClanScore(userClan);
        clanRepository.save(userClan);
    }

    @Override
    public Season endSeason() {
        // 1. Close the active season
        Season currentSeason = seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active season found."));

        currentSeason.setActive(false);
        currentSeason.setEndDate(LocalDateTime.now());
        seasonRepository.save(currentSeason);

        // 2. Process promotions and degradations for each tier
        List<Clan> allClans = clanRepository.findAll();

        for (String tier : TIER_ORDER) {
            List<Clan> clansInTier = allClans.stream()
                    .filter(c -> c.getTier().equals(tier))
                    .sorted(Comparator.comparingInt(Clan::getSeasonScore).reversed())
                    .toList();

            int tierIndex = TIER_ORDER.indexOf(tier);

            // Promote top clans (if not already at Diamond)
            if (tierIndex < TIER_ORDER.size() - 1) {
                String nextTier = TIER_ORDER.get(tierIndex + 1);
                int promotionCount = Math.min(PROMOTION_SLOTS, clansInTier.size());
                for (int i = 0; i < promotionCount; i++) {
                    Clan clan = clansInTier.get(i);
                    String previousTier = clan.getTier();
                    clan.setTier(nextTier);

                    // Notify achievement service if promoted to Diamond
                    if (nextTier.equals("Diamond")) {
                        achievementClient.notifyClanPromoted(clan.getClanId(), nextTier);
                    }
                }
            }

            // Degrade bottom clans (if not already at Bronze)
            if (tierIndex > 0) {
                String previousTier = TIER_ORDER.get(tierIndex - 1);
                int degradationCount = Math.min(DEGRADATION_SLOTS, clansInTier.size());
                // Bottom clans are at the end of the sorted list
                for (int i = clansInTier.size() - 1;
                        i >= clansInTier.size() - degradationCount; i--) {
                    clansInTier.get(i).setTier(previousTier);
                }
            }
        }

        // 3. Reset all clans for the new season
        for (Clan clan : allClans) {
            clan.setSeasonScore(0);
            clan.setScoreMultiplier(1.0);

            // Reset each member's season score to 0.
            // Accuracy is NOT reset — it stays as the lifetime average.
            List<ClanMember> resetMembers = clan.getMembers().stream()
                    .map(m -> new ClanMember(m.getUserId(), 0, m.getAccuracy()))
                    .toList();

            clan.getMembers().clear();
            clan.getMembers().addAll(resetMembers);
        }

        clanRepository.saveAll(allClans);

        // 4. Start a new season
        Season newSeason = new Season();
        newSeason.setSeasonNumber(currentSeason.getSeasonNumber() + 1);
        return seasonRepository.save(newSeason);
    }

    @Override
    public Season getCurrentSeason() {
        return seasonRepository.findByActiveTrue().orElse(null);
    }

    // Recalculates seasonScore using the clan's tier strategy + buffs/debuffs
    private void recalculateClanScore(Clan clan) {
        // Step 1: Apply the tier's scoring strategy
        ScoringStrategy strategy = scoringStrategyFactory.getStrategy(clan.getTier());
        int baseScore = strategy.computeScore(clan);

        // Step 2: Compute buff/debuff multiplier
        double multiplier = 1.0;

        // Productivity Buff: if >=50% of members completed today's daily mission
        long membersWithMission = clan.getMembers().stream()
                .filter(m -> achievementClient.getMissionScore(m.getUserId()) > 0)
                .count();

        double missionCompletionRate = clan.getMembers().isEmpty() ? 0.0
                : (double) membersWithMission / clan.getMembers().size();

        if (missionCompletionRate >= 0.5) {
            multiplier *= 1.2; // Productivity Buff
        }

        // Low Accuracy Debuff: if average accuracy < 50%
        if (clan.getAverageAccuracy() < 0.5) {
            multiplier *= 0.8; // Low Accuracy Penalty
        }

        clan.setScoreMultiplier(multiplier);
        clan.setSeasonScore((int) Math.round(baseScore * multiplier));
    }
}