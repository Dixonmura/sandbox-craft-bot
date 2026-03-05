package vacancy_tracker.bot;

import markups.VacancyKeyboardKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacancy_tracker.core.UserService;
import vacancy_tracker.core.VacancySessionState;
import vacancy_tracker.presentation.UpdateMapper;
import vacancy_tracker.presentation.UserCommandParser;
import vacancy_tracker.presentation.VacancyCommandDispatcher;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;
import vacancy_tracker.presentation.dto.UserCommandDto;

import static vacancy_tracker.bot.VacancyMessages.START_MESSAGE;
import static vacancy_tracker.bot.VacancyMessages.START_WHEN_ACTIVE_MESSAGE;

/**
 * Telegram-бот для настройки поиска вакансий и получения ежедневных уведомлений.
 * Принимает обновления Telegram, преобразует их в доменные команды
 * и делегирует обработку {@link VacancyCommandDispatcher}.
 */
public class VacancyBot {

    private static final Logger log = LogManager.getLogger(VacancyBot.class);
    private final UserService userService;
    private final UpdateMapper updateMapper;
    private final UserCommandParser commandParser;
    private final VacancyCommandDispatcher dispatcher;

    public VacancyBot(UserService userService,
                      UpdateMapper updateMapper,
                      UserCommandParser commandParser,
                      VacancyCommandDispatcher dispatcher) {
        this.userService = userService;
        this.updateMapper = updateMapper;
        this.commandParser = commandParser;
        this.dispatcher = dispatcher;
    }

    public VacancyReply startVacancyBot(Update update) {

        Long chatId = update.getMessage().getChatId();
        var from = update.getMessage().getFrom();
        String firstName = from != null ? from.getFirstName() : "unknown";
        String userName = from != null ? from.getUserName() : "unknown";

        log.info(
                "Первый запуск VacancyTracker-бота для пользователя chatId={}, firstName={}, userName={}",
                chatId,
                firstName,
                userName);

        if (userService.getStateSessionOrDefault(chatId) == VacancySessionState.ACTIVE) {
            return new VacancyReply(update.getMessage().getChatId(), START_WHEN_ACTIVE_MESSAGE, VacancyKeyboardKey.START_AND_STOP_KEYBOARD);
        } else {
            return new VacancyReply(update.getMessage().getChatId(), START_MESSAGE, VacancyKeyboardKey.START_KEYBOARD);
        }
    }

    public VacancyReply handleAnswer(Update update) {
        IncomingUpdateDto incoming = updateMapper.getUpdateDto(update);
        Long userId = incoming.userId();
        var state = userService.getSettingState(userId);
        UserCommandDto command = commandParser.parse(incoming, state);

        return dispatcher.commandDispatch(command);
    }

    public boolean isConfiguring(Long chatId) {
        return userService.getStateSessionOrDefault(chatId) == VacancySessionState.CONFIGURING;
    }

    public boolean isActive(Long chatId) {
        return userService.getStateSessionOrDefault(chatId) == VacancySessionState.ACTIVE;
    }

    public boolean isInactive(Long chatId) {
        return userService.getStateSessionOrDefault(chatId) == VacancySessionState.INACTIVE;
    }
}
