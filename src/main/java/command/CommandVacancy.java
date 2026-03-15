package command;

import bot.utils.ReplyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import vacancy_tracker.bot.VacancyBot;
import vacancy_tracker.bot.VacancyReply;

public class CommandVacancy implements Command {

    private static final Logger log = LogManager.getLogger(CommandVacancy.class);


    private final TelegramClient telegramClient;
    private final VacancyBot vacancyBot;

    public CommandVacancy(TelegramClient telegramClient, VacancyBot vacancyBot) {
        this.telegramClient = telegramClient;
        this.vacancyBot = vacancyBot;
    }

    @Override
    public void execute(Update update) {
        if (update == null || !update.hasMessage()) {
            log.error("CommandVacancy.execute вызван с некорректным update");
            return;
        }

        Long chatId = update.getMessage().getChatId();
        log.info("Запуск VacancyBot для chatId={}", chatId);

        VacancyReply reply = vacancyBot.startVacancyBot(update);

        SendMessage sendMessage = ReplyUtils.sendMessageVacancy(reply);

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке стартового сообщения VacancyBot для chatId={}", chatId, e);
        }
    }
}
