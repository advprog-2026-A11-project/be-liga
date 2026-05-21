package id.ac.ui.cs.advprog.liga.service;

import id.ac.ui.cs.advprog.liga.client.AchievementClient;
import id.ac.ui.cs.advprog.liga.dto.ScoreUpdateRequest;
import id.ac.ui.cs.advprog.liga.model.Clan;
import id.ac.ui.cs.advprog.liga.model.ClanMember;
import id.ac.ui.cs.advprog.liga.model.Season;
import id.ac.ui.cs.advprog.liga.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.liga.repository.ClanRepository;
import id.ac.ui.cs.advprog.liga.repository.SeasonRepository;
import id.ac.ui.cs.advprog.liga.strategy.ScoringStrategy;
import id.ac.ui.cs.advprog.liga.strategy.ScoringStrategyFactory;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeagueServiceImpl implements LeagueService {

    private static final int PROMOTION_SLOTS = 2;
    private static final int DEGRADATION_SLOTS = 2;
    private static final List<String> TIER_ORDER =
            List.of("Bronze", "Silver", "Gold", "Platinum", "Diamond");

    private final ClanRepository clanRepository;
    private final ClanMemberRepository clanMemberRepository;
    private final SeasonRepository seasonRepository;
    private final ScoringStrategyFactory scoringStrategyFactory;
    private final AchievementClient achievementClient;

    public LeagueServiceImpl(
            ClanRepository clanRepository,
            ClanMemberRepository clanMemberRepository,
            SeasonRepository seasonRepository,
            ScoringStrategyFactory scoringStrategyFactory,
            AchievementClient achievementClient) {
        this.clanRepository = clanRepository;
        this.clanMemberRepository = clanMemberRepository;
        this.seasonRepository = seasonRepository;
        this.scoringStrategyFactory = scoringStrategyFactory;
        this.achievementClient = achievementClient;
    }

    @Override
    public void handleScoreUpdate(ScoreUpdateRequest request) {
        String userId = request.getUserId();

        // Register the student in our system if they're not already known
        ClanMember member = clanMemberRepository.findByUserId(userId)
                .orElseGet(() -> {
                    ClanMember newMember = new ClanMember(userId);
                    return clanMemberRepository.save(newMember);
                });

        // Update quiz or mission score
        if (request.isAQuiz()) {
            member.setQuizScore(member.getQuizScore() + request.getScore());
            member.setTotalQuizzes(member.getTotalQuizzes() + 1);
            member.setTotalAccuracy(member.getTotalAccuracy() + request.getAccuracy());
        } else {
            member.setMissionScore(member.getMissionScore() + request.getScore());
        }

        clanMemberRepository.save(member);

        // If this student is in a clan, recalculate that clan's season score
        if (member.getClanId() != null) {
            clanRepository.findById(member.getClanId())
                    .ifPresent(this::recalculateClanScore);
        }
    }

    @Override
    public Season endSeason() {
        Season currentSeason = seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active season found."));

        currentSeason.setActive(false);
        currentSeason.setEndDate(LocalDateTime.now());
        seasonRepository.save(currentSeason);

        // Process promotions and degradations
        List<Clan> allClans = clanRepository.findAll();

        for (String tier : TIER_ORDER) {
            List<Clan> clansInTier = allClans.stream()
                    .filter(c -> c.getTier().equals(tier))
                    .sorted(Comparator.comparingInt(Clan::getSeasonScore).reversed())
                    .toList();

            int tierIndex = TIER_ORDER.indexOf(tier);

            if (tierIndex < TIER_ORDER.size() - 1) {
                String nextTier = TIER_ORDER.get(tierIndex + 1);
                int promotionCount = Math.min(PROMOTION_SLOTS, clansInTier.size());
                for (int i = 0; i < promotionCount; i++) {
                    Clan clan = clansInTier.get(i);
                    clan.setTier(nextTier);
                    if (nextTier.equals("Diamond")) {
                        achievementClient.notifyClanPromoted(clan.getClanId(), nextTier);
                    }
                }
            }

            if (tierIndex > 0) {
                String previousTier = TIER_ORDER.get(tierIndex - 1);
                int degradationCount = Math.min(DEGRADATION_SLOTS, clansInTier.size());
                for (int i = clansInTier.size() - 1;
                        i >= clansInTier.size() - degradationCount; i--) {
                    clansInTier.get(i).setTier(previousTier);
                }
            }
        }

        // Reset all clan season scores and multipliers
        for (Clan clan : allClans) {
            clan.setSeasonScore(0);
            clan.setScoreMultiplier(1.0);
        }
        clanRepository.saveAll(allClans);

        // Reset all known students' season scores (quiz + mission).
        // Accuracy (totalQuizzes + totalAccuracy) is NEVER reset.
        List<ClanMember> allMembers = clanMemberRepository.findAll();
        for (ClanMember member : allMembers) {
            member.setQuizScore(0);
            member.setMissionScore(0);
        }
        clanMemberRepository.saveAll(allMembers);

        // Start new season
        Season newSeason = new Season();
        newSeason.setSeasonNumber(currentSeason.getSeasonNumber() + 1);
        return seasonRepository.save(newSeason);
    }

    @Override
    public Season getCurrentSeason() {
        return seasonRepository.findByActiveTrue().orElse(null);
    }

    // Recomputes a clan's seasonScore using its tier's strategy + buffs/debuffs
    private void recalculateClanScore(Clan clan) {
        List<ClanMember> members = clanMemberRepository.findByClanId(clan.getClanId());

        ScoringStrategy strategy = scoringStrategyFactory.getStrategy(clan.getTier());
        int baseScore = strategy.computeScore(members);

        double multiplier = 1.0;

        // Productivity Buff: >= 50% of members completed a daily mission today
        long membersWithMission = members.stream()
                .filter(m -> achievementClient.getMissionScore(m.getUserId()) > 0)
                .count();

        if (!members.isEmpty() &&
                (double) membersWithMission / members.size() >= 0.5) {
            multiplier *= 1.2;
        }

        // Low Accuracy Debuff: clan average accuracy < 50%
        double avgAccuracy = members.stream()
                .mapToDouble(ClanMember::getAccuracy)
                .average()
                .orElse(0.0);

        if (avgAccuracy < 0.5) {
            multiplier *= 0.8;
        }

        clan.setScoreMultiplier(multiplier);
        clan.setSeasonScore((int) Math.round(baseScore * multiplier));
        clanRepository.save(clan);
    }
}