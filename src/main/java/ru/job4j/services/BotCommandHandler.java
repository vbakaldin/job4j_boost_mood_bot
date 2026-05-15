package ru.job4j.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.job4j.content.Content;
import ru.job4j.model.User;
import ru.job4j.repository.UserRepository;

import java.util.Optional;

@Service
public class BotCommandHandler implements ApplicationContextAware {
    private final UserRepository userRepository;
    private final MoodService moodService;
    private final TgUI tgUI;
    private final DailyAdviceService dailyAdviceService;

    public BotCommandHandler(UserRepository userRepository,
                             MoodService moodService,
                             TgUI tgUI,
                             DailyAdviceService dailyAdviceService) {
        this.userRepository = userRepository;
        this.moodService = moodService;
        this.tgUI = tgUI;
        this.dailyAdviceService = dailyAdviceService;
    }

    Optional<Content> commands(Message message) {
        var text = message.getText();
        var chatId = message.getChatId();
        var clientId = message.getFrom().getId();
        return switch (text) {
            case "/start" -> handleStartCommand(chatId, clientId);
            case "/week_mood_log" -> moodService.weekMoodLogCommand(chatId, clientId);
            case "/month_mood_log" -> moodService.monthMoodLogCommand(chatId, clientId);
            case "/award" -> moodService.awards(chatId, clientId);
            case "/daily_advice" -> dailyAdviceService.dailyAdvice(chatId, clientId);
            case "/daily_advice_on" -> dailyAdviceService.enableDailyAdvice(chatId, clientId);
            case "/daily_advice_off" -> dailyAdviceService.disableDailyAdvice(chatId, clientId);
            default -> Optional.empty();
        };
    }

    Optional<Content> handleCallback(CallbackQuery callback) {
        var moodId = Long.valueOf(callback.getData());
        var user = userRepository.findByClientId(callback.getFrom().getId());
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(moodService.chooseMood(user, moodId));
    }

    private Optional<Content> handleStartCommand(long chatId, Long clientId) {
        var user = userRepository.findByClientId(clientId);
        if (user == null) {
            user = new User();
            user.setClientId(clientId);
            user.setChatId(chatId);
            userRepository.save(user);
        }
        var content = new Content(user.getChatId());
        content.setText("Как настроение?");
        content.setMarkup(tgUI.buildButtons());
        return Optional.of(content);
    }
    void receive(Content content) {
        System.out.println(content);
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        System.out.println("ApplicationContext set in ApplicationContextAwareExample");
    }

    public void displayAllBeanNames() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Beans in ApplicationContext:");
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("Bean is going through @PostConstruct init.");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("Bean will be destroyed via @PreDestroy.");
    }
}
