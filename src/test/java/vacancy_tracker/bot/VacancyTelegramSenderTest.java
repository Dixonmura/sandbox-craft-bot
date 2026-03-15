package vacancy_tracker.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import vacancy_tracker.core.Vacancy;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyTelegramSenderTest {

    @Mock
    private TelegramClient telegramClient;

    private VacancyTelegramSender sender;
    private final Long CHAT_ID = 123L;

    @BeforeEach
    void setUp() {
        sender = new VacancyTelegramSender(telegramClient);
    }

    private Vacancy createVacancy(String company, Integer salaryFrom, Integer salaryTo,
                                  Integer experience, String url) {
        return new Vacancy(
                "id1",
                "Java Developer",
                company,
                salaryFrom,
                salaryTo,
                77,
                experience,
                url
        );
    }

    @Test
    @DisplayName("При пустом списке отправляет сообщение об отсутствии вакансий")
    void sendVacancies_shouldSendNoVacanciesMessage_whenListIsEmpty() throws TelegramApiException {
        sender.sendVacancies(CHAT_ID, List.of());

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(sentMessage.getText()).contains("❌ Новых вакансий не найдено");
        assertThat(sentMessage.getReplyMarkup()).isNull();
    }

    @Test
    @DisplayName("При одной вакансии отправляет страницу с одной записью")
    void sendVacancies_shouldSendSingleVacancy_whenListHasOneElement() throws TelegramApiException {
        Vacancy vacancy = createVacancy("ООО Тест", 100000, 150000, 3, "https://test.ru");
        sender.sendVacancies(CHAT_ID, List.of(vacancy));

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(sentMessage.getText())
                .contains("✅ Вакансии (стр. 1/1)")
                .contains("ООО Тест")
                .contains("100000-150000 руб.")
                .contains("Опыт: 3.");
    }

    @Test
    @DisplayName("При 15 вакансиях создаёт первую страницу с 10 записями")
    void sendVacancies_shouldCreateFirstPageWithTenVacancies_whenTotalIsFifteen() throws TelegramApiException {
        List<Vacancy> vacancies = List.of(
                createVacancy("Компания 1", 100, 200, 1, "url1"),
                createVacancy("Компания 2", 200, 300, 2, "url2"),
                createVacancy("Компания 3", 300, 400, 3, "url3"),
                createVacancy("Компания 4", 400, 500, 4, "url4"),
                createVacancy("Компания 5", 500, 600, 5, "url5"),
                createVacancy("Компания 6", 600, 700, 6, "url6"),
                createVacancy("Компания 7", 700, 800, 7, "url7"),
                createVacancy("Компания 8", 800, 900, 8, "url8"),
                createVacancy("Компания 9", 900, 1000, 9, "url9"),
                createVacancy("Компания 10", 1000, 1100, 10, "url10"),
                createVacancy("Компания 11", 1100, 1200, 11, "url11"),
                createVacancy("Компания 12", 1200, 1300, 12, "url12"),
                createVacancy("Компания 13", 1300, 1400, 13, "url13"),
                createVacancy("Компания 14", 1400, 1500, 14, "url14"),
                createVacancy("Компания 15", 1500, 1600, 15, "url15")
        );

        sender.sendVacancies(CHAT_ID, vacancies);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText())
                .contains("✅ Вакансии (стр. 1/2)")
                .contains("Компания 1")
                .contains("Компания 10")
                .doesNotContain("Компания 11");

        assertThat(sentMessage.getReplyMarkup()).isInstanceOf(InlineKeyboardMarkup.class);
        InlineKeyboardMarkup keyboard = (InlineKeyboardMarkup) sentMessage.getReplyMarkup();
        List<InlineKeyboardButton> buttons = keyboard.getKeyboard().getFirst();

        assertThat(buttons).hasSize(2);
        assertThat(buttons.get(1).getText()).isEqualTo("Следующие 10 ➡️");
    }

    @Test
    @DisplayName("Переход на следующую страницу")
    void nextPage_shouldShowSecondPage() throws TelegramApiException {
        List<Vacancy> vacancies = List.of(
                createVacancy("Компания 1", 100, 200, 1, "url1"),
                createVacancy("Компания 2", 200, 300, 2, "url2"),
                createVacancy("Компания 3", 300, 400, 3, "url3"),
                createVacancy("Компания 4", 400, 500, 4, "url4"),
                createVacancy("Компания 5", 500, 600, 5, "url5"),
                createVacancy("Компания 6", 600, 700, 6, "url6"),
                createVacancy("Компания 7", 700, 800, 7, "url7"),
                createVacancy("Компания 8", 800, 900, 8, "url8"),
                createVacancy("Компания 9", 900, 1000, 9, "url9"),
                createVacancy("Компания 10", 1000, 1100, 10, "url10"),
                createVacancy("Компания 11", 1100, 1200, 11, "url11"),
                createVacancy("Компания 12", 1200, 1300, 12, "url12")
        );

        sender.sendVacancies(CHAT_ID, vacancies);
        verify(telegramClient, times(1)).execute(any(SendMessage.class));

        sender.nextPage(CHAT_ID);

        ArgumentCaptor<SendMessage> secondMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient, times(2)).execute(secondMessageCaptor.capture());

        SendMessage secondPage = secondMessageCaptor.getAllValues().get(1);
        assertThat(secondPage.getText())
                .contains("✅ Вакансии (стр. 2/2)")
                .contains("Компания 11")
                .contains("Компания 12");
    }

    @Test
    @DisplayName("Переход на предыдущую страницу")
    void prevPage_shouldReturnToFirstPage() throws TelegramApiException {
        List<Vacancy> vacancies = List.of(
                createVacancy("Компания 1", 100, 200, 1, "url1"),
                createVacancy("Компания 2", 200, 300, 2, "url2"),
                createVacancy("Компания 3", 300, 400, 3, "url3"),
                createVacancy("Компания 4", 400, 500, 4, "url4"),
                createVacancy("Компания 5", 500, 600, 5, "url5"),
                createVacancy("Компания 6", 600, 700, 6, "url6"),
                createVacancy("Компания 7", 700, 800, 7, "url7"),
                createVacancy("Компания 8", 800, 900, 8, "url8"),
                createVacancy("Компания 9", 900, 1000, 9, "url9"),
                createVacancy("Компания 10", 1000, 1100, 10, "url10"),
                createVacancy("Компания 11", 1100, 1200, 11, "url11")
        );

        sender.sendVacancies(CHAT_ID, vacancies);
        sender.nextPage(CHAT_ID);
        sender.prevPage(CHAT_ID);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient, times(3)).execute(messageCaptor.capture());

        SendMessage firstPage = messageCaptor.getAllValues().get(0);
        SendMessage afterPrev = messageCaptor.getAllValues().get(2);

        assertThat(afterPrev.getText()).isEqualTo(firstPage.getText());
    }

    @Test
    @DisplayName("Переход на следующую страницу без активной сессии игнорируется")
    void nextPage_shouldDoNothing_whenNoActiveSession() throws TelegramApiException {
        sender.nextPage(CHAT_ID);
        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Переход на следующую страницу на последней странице игнорируется")
    void nextPage_shouldDoNothing_whenOnLastPage() throws TelegramApiException {
        List<Vacancy> vacancies = List.of(createVacancy("Компания", 100, 200, 1, "url"));
        sender.sendVacancies(CHAT_ID, vacancies);

        sender.nextPage(CHAT_ID);

        verify(telegramClient, times(1)).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Переход на предыдущую страницу на первой странице игнорируется")
    void prevPage_shouldDoNothing_whenOnFirstPage() throws TelegramApiException {
        List<Vacancy> vacancies = List.of(createVacancy("Компания", 100, 200, 1, "url"));
        sender.sendVacancies(CHAT_ID, vacancies);

        sender.prevPage(CHAT_ID);

        verify(telegramClient, times(1)).execute(any(SendMessage.class));
    }
}