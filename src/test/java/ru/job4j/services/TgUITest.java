package ru.job4j.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.job4j.model.Mood;
import ru.job4j.repository.MoodFakeRepository;
import ru.job4j.repository.MoodRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = {TgUI.class, MoodFakeRepository.class})
class TgUITest {
    @Autowired
    private TgUI tgUI;

    @Autowired
    @Qualifier("moodFakeRepository")
    private MoodRepository moodRepository;

    @Test
    public void whenBtnGood() {
        var mood = new Mood("Good", true);
        mood.setId(1L);
        moodRepository.save(mood);

        var markup = tgUI.buildButtons();
        var button = markup.getKeyboard().get(0).get(0);

        assertThat(button.getText()).isEqualTo("Good");
        assertThat(button.getCallbackData()).isEqualTo("1");
    }
}