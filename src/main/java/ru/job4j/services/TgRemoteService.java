package ru.job4j.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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
    private final UserRepository userRepository;
    private final TgUI tgUI;

    public TgRemoteService(@Value("${telegram.bot.name}") String botName,
                           @Value("${telegram.bot.token}") String botToken,
                           UserRepository userRepository,
                           TgUI tgUI) {
        this.botName = botName;
        this.botToken = botToken;
        this.userRepository = userRepository;
        this.tgUI = tgUI;
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

    InlineKeyboardButton createBtn(String name, String data) {
        var inline = new InlineKeyboardButton();
        inline.setText(name);
        inline.setCallbackData(data);
        return inline;
    }

    public SendMessage sendButtons(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Как настроение сегодня?");
        message.setReplyMarkup(tgUI.buildButtons());
        return message;
    }

    @Override
    public void onUpdateReceived(Update update) {
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            String messageText = update.getMessage().getText();
//            long chatId = update.getMessage().getChatId();
//            SendMessage message = new SendMessage();
//            message.setChatId(chatId);
//            message.setText("Вы написали: " + messageText);
//            try {
//                execute(message);
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//            }
//        }

//        if (update.hasCallbackQuery()) {
//            var data = update.getCallbackQuery().getData();
//            var chatId = update.getCallbackQuery().getMessage().getChatId();
//            send(new SendMessage(String.valueOf(chatId), MOOD_RESP.get(data)));
//        }
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            long chatId = update.getMessage().getChatId();
//            send(sendButtons(chatId));
//        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            if ("/start".equals(message.getText())) {
                long chatId = message.getChatId();
                long clientId = message.getFrom().getId();
                if (userRepository.findByClientId(clientId) == null) {
                    var user = new User();
                    user.setClientId(clientId);
                    user.setChatId(chatId);
                    userRepository.save(user);
                }
                send(sendButtons(chatId));  // Метод для отправки кнопок пользователю
            }
        }
   }
}