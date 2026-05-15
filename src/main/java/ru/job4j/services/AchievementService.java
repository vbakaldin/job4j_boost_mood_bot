package ru.job4j.services;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.content.Content;
import ru.job4j.content.SentContent;
import ru.job4j.model.Achievement;
import ru.job4j.model.Award;
import ru.job4j.model.MoodLog;
import ru.job4j.model.UserEvent;
import ru.job4j.repository.AchievementRepository;
import ru.job4j.repository.AwardRepository;
import ru.job4j.repository.MoodLogRepository;

import java.time.Instant;

@Service
public class AchievementService implements ApplicationListener<UserEvent> {
    private final MoodLogRepository moodLogRepository;
    private final AwardRepository awardRepository;
    private final AchievementRepository achievementRepository;
    private final SentContent sentContent;

    public AchievementService(MoodLogRepository moodLogRepository,
                              AwardRepository awardRepository,
                              AchievementRepository achievementRepository,
                              SentContent sentContent) {
        this.moodLogRepository = moodLogRepository;
        this.awardRepository = awardRepository;
        this.achievementRepository = achievementRepository;
        this.sentContent = sentContent;
    }

    @Transactional
    @Override
    public void onApplicationEvent(UserEvent event) {
        var user = event.getUser();
        var goodMoodDays = moodLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(MoodLog::getMood)
                .filter(mood -> mood != null && mood.isGood())
                .count();
        var userAchievements = achievementRepository.findByUser(user.getClientId());
        for (Award award : awardRepository.findAll()) {
            var hasAward = false;
            for (Achievement userAchievement : userAchievements) {
                if (userAchievement.getAward().equals(award)) {
                    hasAward = true;
                    break;
                }
            }
            if (goodMoodDays >= award.getDays() && !hasAward) {
                var achievement = new Achievement();
                achievement.setUser(user);
                achievement.setAward(award);
                achievement.setCreateAt(Instant.now().getEpochSecond());
                achievementRepository.save(achievement);
                var content = new Content(user.getChatId());
                content.setText("Вы получили награду: " + award.getTitle());
                sentContent.sent(content);
            }
        }
    }
}
