package vacancy_tracker.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import vacancy_tracker.core.Vacancy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Отправляет вакансии в Telegram с поддержкой постраничного просмотра.
 * Хранит состояние страниц для каждого пользователя в ConcurrentHashMap.
 */
public class VacancyTelegramSender implements VacancySender {

    private final TelegramClient telegramClient;
    private final ConcurrentHashMap<Long, VacancyPage> pages = new ConcurrentHashMap<>();

    public VacancyTelegramSender(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Отправляет список вакансий пользователю. Если вакансий больше 10,
     * автоматически создаёт первую страницу и клавиатуру для навигации.
     *
     * @param chatId    ID чата пользователя
     * @param vacancies список вакансий для отправки
     */
    @Override
    public void sendVacancies(Long chatId, List<Vacancy> vacancies) {
        if (vacancies.isEmpty()) {
            sendMessage(chatId, "❌ Новых вакансий не найдено");
            return;
        }

        VacancyPage page = new VacancyPage(chatId, vacancies);
        pages.put(chatId, page);
        sendPage(chatId, page);
    }

    /**
     * Отправляет конкретную страницу вакансий.
     *
     * @param chatId ID чата пользователя
     * @param page   объект страницы с вакансиями
     */
    public void sendPage(Long chatId, VacancyPage page) {
        String message = formatPage(page);
        sendMessageWithKeyboard(chatId, message, createKeyboard(page));
    }

    /**
     * Переход на следующую страницу вакансий.
     *
     * @param chatId ID чата пользователя
     */
    public void nextPage(Long chatId) {
        VacancyPage current = pages.get(chatId);
        if (current != null && current.hasNext()) {
            VacancyPage next = current.nextPage();
            pages.put(chatId, next);
            sendPage(chatId, next);
        }
    }

    /**
     * Переход на предыдущую страницу вакансий.
     *
     * @param chatId ID чата пользователя
     */
    public void prevPage(Long chatId) {
        VacancyPage current = pages.get(chatId);
        if (current != null && current.hasPrev()) {
            VacancyPage prev = current.prevPage();
            pages.put(chatId, prev);
            sendPage(chatId, prev);
        }
    }

    /**
     * Форматирует страницу с вакансиями в читаемый текст.
     */
    private String formatPage(VacancyPage page) {
        List<Vacancy> vacancies = page.getCurrentVacancies();

        String header = String.format("✅ Вакансии (стр. %d/%d):\n\n",
                page.getCurrentPage() + 1, page.getTotalPages());

        String body = vacancies.stream()
                .map(this::formatSingleVacancy)
                .collect(Collectors.joining("\n\n"));

        return header + body;
    }

    /**
     * Форматирует одну вакансию.
     */
    private String formatSingleVacancy(Vacancy v) {
        return String.format(
                "Компания: \"%s\"\n" +
                        "Зарплата: %s\n" +
                        "Опыт: %s.\n" +
                        "Подробное описание вакансии %s",
                v.company(),
                formatSalary(v.salaryFrom(), v.salaryTo()),
                v.experience(),
                v.url()
        );
    }

    /**
     * Форматирует зарплату в зависимости от наличия значений.
     */
    private String formatSalary(Integer from, Integer to) {
        if (from == null && to == null) return "не указана";
        if (from == null) return "до " + to + " руб.";
        if (to == null || from.equals(to)) return from + " руб.";
        return from + "-" + to + " руб.";
    }

    /**
     * Создаёт клавиатуру для навигации по страницам.
     */
    private InlineKeyboardMarkup createKeyboard(VacancyPage page) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (page.hasPrev()) {
            row.add(InlineKeyboardButton.builder()
                    .text("⬅️ Предыдущие 10")
                    .callbackData("vacancy_prev")
                    .build());
        }

        row.add(InlineKeyboardButton.builder()
                .text((page.getCurrentPage() + 1) + "/" + page.getTotalPages())
                .callbackData("vacancy_page_info")
                .build());

        if (page.hasNext()) {
            row.add(InlineKeyboardButton.builder()
                    .text("Следующие 10 ➡️")
                    .callbackData("vacancy_next")
                    .build());
        }

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(row))
                .build();
    }

    private void sendMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(keyboard)
                    .build());
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
        }
    }

    private void sendMessage(Long chatId, String text) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
        }
    }
}