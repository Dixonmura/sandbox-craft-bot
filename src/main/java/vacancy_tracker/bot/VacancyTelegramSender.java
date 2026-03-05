package vacancy_tracker.bot;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import vacancy_tracker.core.Vacancy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Отправляет список вакансий в Telegram с пагинацией.
 */
public class VacancyTelegramSender implements VacancySender {

    private final TelegramClient telegramClient;

    public VacancyTelegramSender(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void sendVacancies(Long chatId, List<Vacancy> vacancies) {
        if (vacancies.isEmpty()) {
            try {
                telegramClient.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("❌ Новых вакансий не найдено")
                        .build());
            } catch (TelegramApiException e) {
                System.out.println(e.getMessage());
            }
            return;
        }

        String message = String.format(
                "✅ Новых вакансий: %d\n\n%s",
                vacancies.size(),
                vacancies.stream()
                        .limit(3)  // Первые 3 для preview
                        .map(v -> String.format("💼 %s\n💰 %s-%s руб.\n🔗 %s",
                                v.getTitle(), v.getSalaryFrom(), v.getSalaryTo(), v.getUrl()))
                        .collect(Collectors.joining("\n\n"))
        );

        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(message)
                    .build());
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}
