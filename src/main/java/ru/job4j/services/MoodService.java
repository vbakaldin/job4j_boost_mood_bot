package ru.job4j.services;

import org.springframework.stereotype.Service;
import ru.job4j.content.*;
import ru.job4j.model.*;
import ru.job4j.recommendation.RecommendationEngine;
import ru.job4j.repository.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MoodService {
    private final MoodLogRepository moodLogRepository;
    private final RecommendationEngine recommendationEngine;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final MoodRepository moodRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("dd-MM-yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    public MoodService(MoodLogRepository moodLogRepository,
                       RecommendationEngine recommendationEngine,
                       UserRepository userRepository,
                       AchievementRepository achievementRepository,
                       MoodRepository moodRepository) {
        this.moodLogRepository = moodLogRepository;
        this.recommendationEngine = recommendationEngine;
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.moodRepository = moodRepository;
    }

    public Content chooseMood(User user, Long moodId) {
        var mood = moodRepository.findById(moodId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mood not found by id: " + moodId
                ));
        var moodLog = new MoodLog();
        moodLog.setUser(user);
        moodLog.setMood(mood);
        moodLog.setCreatedAt(Instant.now().getEpochSecond());
        moodLogRepository.save(moodLog);
        return recommendationEngine.recommendFor(user.getChatId(), moodId);
    }

    public Optional<Content> weekMoodLogCommand(long chatId, Long clientId) {
        var content = new Content(chatId);
        var to = Instant.now();
        var from = to.minus(7, ChronoUnit.DAYS);
        var logs = moodLogRepository.findMoodLogs(
                clientId,
                from.getEpochSecond(),
                to.getEpochSecond()
        );
        content.setText(formatMoodLogs(logs, "Mood log for the last week"));
        return Optional.of(content);
    }

    public Optional<Content> monthMoodLogCommand(long chatId, Long clientId) {
        var content = new Content(chatId);
        var to = Instant.now();
        var from = to.atZone(ZoneId.systemDefault())
                .minusMonths(1)
                .toInstant();
        var logs = moodLogRepository.findMoodLogs(
                clientId,
                from.getEpochSecond(),
                to.getEpochSecond()
        );
        content.setText(formatMoodLogs(logs, "Mood log for the last month"));
        return Optional.of(content);
    }

    private String formatMoodLogs(List<MoodLog> logs, String title) {
        if (logs.isEmpty()) {
            return title + ":\nNo mood logs found.";
        }
        var sb = new StringBuilder(title + ":\n");
        logs.forEach(log -> {
            String formattedDate = formatter.format(Instant.ofEpochSecond(log.getCreatedAt()));
            sb.append(formattedDate)
                    .append(": ")
                    .append(log.getMood().getText())
                    .append("\n");
        });
        return sb.toString();
    }

    public Optional<Content> awards(long chatId, Long clientId) {
        var content = new Content(chatId);
        var achievements = achievementRepository.findByUser(clientId);
        content.setText(formatAchievements(achievements));
        return Optional.of(content);
    }

    private String formatAchievements(List<Achievement> achievements) {
        if (achievements.isEmpty()) {
            return "Awards:\nNo awards found.";
        }
        var sb = new StringBuilder("Awards:\n");
        achievements.forEach(achievement -> {
            var award = achievement.getAward();
            var formattedDate = formatter.format(Instant.ofEpochSecond(
                    achievement.getCreateAt()
            ));
            sb.append(formattedDate)
                    .append(": ")
                    .append(award.getTitle())
                    .append(" - ")
                    .append(award.getDescription())
                    .append("\n");
        });
        return sb.toString();
    }
}
