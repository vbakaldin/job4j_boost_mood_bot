package ru.job4j.services;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.config.FakeTelegramCondition;
import ru.job4j.content.Content;
import ru.job4j.content.SentContent;

@Service
@Conditional(FakeTelegramCondition.class)
public class FakeTelegramBotService extends TelegramLongPollingBot implements SentContent {
    @Override
    public String getBotUsername() {
        return "fake";
    }

    @Override
    public String getBotToken() {
        return "fake";
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Fake bot received update: " + update);
    }

    @Override
    public void sent(Content content) {
        System.out.println("Fake sent content to chat " + content.getChatId());
        System.out.println("Text: " + content.getText());
        System.out.println("Audio: " + content.getAudio());
        System.out.println("Photo: " + content.getPhoto());
        System.out.println("Markup: " + content.getMarkup());
    }
}