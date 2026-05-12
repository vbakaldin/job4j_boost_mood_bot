package ru.job4j.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.content.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.job4j.content.Content;
import ru.job4j.content.SentContent;
import ru.job4j.content.SentContentException;
import ru.job4j.config.RealTelegramCondition;

@Service
@Conditional(RealTelegramCondition.class)
public class TelegramBotService extends TelegramLongPollingBot implements SentContent {
    private final BotCommandHandler handler;
    private final String botName;

    public TelegramBotService(@Value("${telegram.bot.name}") String botName,
                              @Value("${telegram.bot.token}") String botToken,
                              BotCommandHandler handler) {
        super(botToken);
        this.handler = handler;
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handler.handleCallback(update.getCallbackQuery())
                    .ifPresent(this::sent);
        } else if (update.hasMessage() && update.getMessage().getText() != null) {
            handler.commands(update.getMessage())
                    .ifPresent(this::sent);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void sent(Content content) {
        try {
            if (content.getAudio() != null) {
                var audio = new SendAudio();
                audio.setChatId(content.getChatId());
                audio.setAudio(content.getAudio());
                if (content.getText() != null) {
                    audio.setCaption(content.getText());
                }
                execute(audio);
                return;
            }
            if (content.getText() != null && content.getMarkup() != null) {
                var message = new SendMessage();
                message.setChatId(content.getChatId());
                message.setText(content.getText());
                message.setReplyMarkup(content.getMarkup());
                execute(message);
                return;
            }
            if (content.getText() != null) {
                var message = new SendMessage();
                message.setChatId(content.getChatId());
                message.setText(content.getText());
                execute(message);
                return;
            }
            if (content.getPhoto() != null) {
                var photo = new SendPhoto();
                photo.setChatId(content.getChatId());
                photo.setPhoto(content.getPhoto());
                if (content.getText() != null) {
                    photo.setCaption(content.getText());
                }
                execute(photo);
            }
        } catch (TelegramApiException e) {
            throw new SentContentException("Could not send content", e);
        }
    }
}