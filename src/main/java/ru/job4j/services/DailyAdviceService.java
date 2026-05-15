package ru.job4j.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import ru.job4j.content.Content;
import ru.job4j.content.SentContent;
import ru.job4j.model.MoodLog;
import ru.job4j.model.User;
import ru.job4j.repository.MoodLogRepository;
import ru.job4j.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class DailyAdviceService {
    private static final Random RND = new Random();
    private final UserRepository userRepository;
    private final MoodLogRepository moodLogRepository;
    private final ObjectProvider<SentContent> sentContent;

    private final List<String> goodAdvices = List.of(
            "Отличный день, чтобы сделать один маленький шаг к большой цели.",
            "Поделитесь хорошим настроением с кем-нибудь рядом.",
            "Запишите одну идею, которую хочется попробовать сегодня."
    );

    private final List<String> badAdvices = List.of(
            "Сделайте короткую паузу и спокойно подышите пару минут.",
            "Не требуйте от себя слишком многого. Один простой шаг уже хорошо.",
            "Попробуйте выпить воды, пройтись или немного отдохнуть."
    );

    public DailyAdviceService(UserRepository userRepository,
                              MoodLogRepository moodLogRepository,
                              ObjectProvider<SentContent> sentContent) {
        this.userRepository = userRepository;
        this.moodLogRepository = moodLogRepository;
        this.sentContent = sentContent;
    }

    public Optional<Content> dailyAdvice(long chatId, Long clientId) {
        var user = userRepository.findByClientId(clientId);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(createAdvice(user));
    }

    public Optional<Content> enableDailyAdvice(long chatId, Long clientId) {
        var user = userRepository.findByClientId(clientId);
        if (user == null) {
            return Optional.empty();
        }
        user.setDailyAdviceEnabled(true);
        userRepository.save(user);
        var content = new Content(chatId);
        content.setText("Совет дня включен.");
        return Optional.of(content);
    }

    public Optional<Content> disableDailyAdvice(long chatId, Long clientId) {
        var user = userRepository.findByClientId(clientId);
        if (user == null) {
            return Optional.empty();
        }
        user.setDailyAdviceEnabled(false);
        userRepository.save(user);
        var content = new Content(chatId);
        content.setText("Совет дня отключен.");
        return Optional.of(content);
    }

    @Scheduled(cron = "${daily.advice.cron:0 0 9 * * *}")
    public void sendDailyAdvice() {
        var today = LocalDate.now().toEpochDay();
        for (var user : userRepository.findAll()) {
            if (Boolean.FALSE.equals(user.getDailyAdviceEnabled())) {
                continue;
            }
            if (user.getLastDailyAdviceDay() != null
                    && user.getLastDailyAdviceDay() == today) {
                continue;
            }
            sentContent.getObject().sent(createAdvice(user));
            user.setLastDailyAdviceDay(today);
            userRepository.save(user);
        }
    }

    private Content createAdvice(User user) {
        var content = new Content(user.getChatId());
        content.setText("Совет дня: " + findAdvice(user));
        return content;
    }

    private String findAdvice(User user) {
        var goodMood = moodLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(MoodLog::getMood)
                .filter(mood -> mood != null)
                .findFirst()
                .map(mood -> mood.isGood())
                .orElse(true);
        var advices = goodMood ? goodAdvices : badAdvices;
        return advices.get(RND.nextInt(advices.size()));
    }
}