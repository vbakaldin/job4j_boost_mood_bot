package ru.job4j.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.job4j.content.Content;
import ru.job4j.model.User;
import ru.job4j.repository.UserRepository;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TgRemoteService extends TelegramLongPollingBot {
    private static final Map<String, String> MOOD_RESP = new HashMap<>();

    static {
        MOOD_RESP.put("lost_sock", "Носки — это коварные создания. Но не волнуйся, второй обязательно найдётся!");
        MOOD_RESP.put("cucumber", "Огурец тоже дело серьёзное! Главное, не мариноваться слишком долго.");
        MOOD_RESP.put("dance_ready", "Супер! Танцуй, как будто никто не смотрит. Или, наоборот, как будто все смотрят!");
        MOOD_RESP.put("need_coffee", "Кофе уже в пути! Осталось только подождать... И ещё немного подождать...");
        MOOD_RESP.put("sleepy", "Пора на боковую! Даже супергерои отдыхают, ты не исключение.");
    }

    private final String botName;
    private final String botToken;
    private final BotCommandHandler botCommandHandler;

    public TgRemoteService(@Value("${telegram.bot.name}") String botName,
                           @Value("${telegram.bot.token}") String botToken,
                           BotCommandHandler botCommandHandler) {
        this.botName = botName;
        this.botToken = botToken;
        this.botCommandHandler = botCommandHandler;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void send(Content content) {
        var message = new SendMessage();
        message.setChatId(content.getChatId());
        message.setText(content.getText());
        message.setReplyMarkup(content.getMarkup());
        send(message);
    }

//    InlineKeyboardButton createBtn(String name, String data) {
//        var inline = new InlineKeyboardButton();
//        inline.setText(name);
//        inline.setCallbackData(data);
//        return inline;
//    }

//    public SendMessage sendButtons(long chatId) {
//        SendMessage message = new SendMessage();
//        message.setChatId(chatId);
//        message.setText("Как настроение сегодня?");
//        message.setReplyMarkup(tgUI.buildButtons());
//        return message;
//    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            botCommandHandler.commands(update.getMessage()).ifPresent(this::send);
        }
        if (update.hasCallbackQuery()) {
            botCommandHandler.handleCallback(update.getCallbackQuery()).ifPresent(this::send);
        }
   }
}