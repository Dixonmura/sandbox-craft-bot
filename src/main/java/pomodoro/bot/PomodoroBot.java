package pomodoro.bot;

import bot.utils.CsvResourceReader;
import bot.utils.CsvStatsReader;
import bot.utils.StatsUtils;
import bot.utils.StatsWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;
import pomodoro.core.*;
import pomodoro.service.PomodoroManager;
import pomodoro.service.StatsLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Основной класс бота-Pomodoro.
 * Управляет сессиями пользователей, командами начала и завершения
 * временных циклов, выводом и сохранением статистики.
 */
public class PomodoroBot {

    private static final Logger log = LogManager.getLogger(PomodoroBot.class);
    private final ScheduledExecutorService scheduled;
    private final Map<Long, UserSetupState> stateUsers;
    private final PomodoroSender sender;
    private final StatsLogger statsLogger;
    private final CsvResourceReader reader;
    private final CsvStatsReader csvStatsReader;
    private Map<Phase, List<MotivationPhoto>> motivationPhotos = new HashMap<>();
    private PomodoroManager pomodoroManager = null;
    private PomodoroStats stats;
    private final StatsUtils statsUtils;

    public PomodoroBot(PomodoroSender sender) {
        reader = new CsvResourceReader();
        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(PomodoroPaths.RESOURCES_DIR)) {

            List<MotivationPhoto> photos = reader.read(is, ',', row -> new MotivationPhoto(row[0], row[1]));
            List<MotivationPhoto> photosForWork = new ArrayList<>();
            List<MotivationPhoto> photosForRest = new ArrayList<>();

            photos.forEach(photo -> {

                if (photo.motivationTitle().startsWith("work")) {
                    photosForWork.add(photo);
                } else {
                    photosForRest.add(photo);
                }
            });
            motivationPhotos.put(Phase.WORK, photosForWork);
            motivationPhotos.put(Phase.SHORT_BREAK, photosForRest);
            motivationPhotos.put(Phase.LONG_BREAK, photosForRest);
            this.motivationPhotos = Collections.unmodifiableMap(motivationPhotos);
        } catch (IOException e) {
            log.error("Не удалось прочитать файл movies.csv", e);
            throw new UncheckedIOException(e);
        }
        this.sender = sender;
        stateUsers = new HashMap<>();
        pomodoroManager = new PomodoroManager(motivationPhotos);
        scheduled = Executors.newScheduledThreadPool(4);
        StatsWriter writer = new StatsWriter();
        statsLogger = new StatsLogger(writer, Path.of(PomodoroPaths.LOGS_DIR));
        csvStatsReader = new CsvStatsReader(Path.of(PomodoroPaths.LOGS_DIR), Clock.systemDefaultZone(), reader);
        statsUtils = new StatsUtils();
    }

    /**
     * Конструктор для удобного тестирования
     */
    PomodoroBot(PomodoroSender sender,
                PomodoroManager manager,
                Map<Phase, List<MotivationPhoto>> motivationPhotos,
                StatsLogger statsLogger,
                CsvStatsReader csvStatsReader,
                StatsUtils statsUtils,
                ScheduledExecutorService scheduled) {
        this.sender = sender;
        this.pomodoroManager = manager;
        this.motivationPhotos = motivationPhotos;
        this.statsLogger = statsLogger;
        this.csvStatsReader = csvStatsReader;
        this.statsUtils = statsUtils;
        this.scheduled = scheduled;
        this.reader = null;
        this.stateUsers = null;
    }

    /**
     * Первый запуск бота
     *
     * @param update обновление Telegram
     * @return приветственное сообщение и просит ввести длительность рабочего цикла
     */
    public PomodoroReply startPomodoro(Update update) {
        Long chatId = update.getMessage().getChatId();
        UserSetupState state = new UserSetupState();
        state.setStep(SetupStep.WAITING_WORK_DURATION);
        stateUsers.put(chatId, state);
        var from = update.getMessage().getFrom();
        String firstName = from != null ? from.getFirstName() : "unknown";
        String userName = from != null ? from.getUserName() : "unknown";

        log.info("Первый запуск Pomodoro-бота для пользователя chatId={}, firstName={}, userName={}", chatId, firstName, userName);

        pomodoroManager.addSession(chatId, new PomodoroSession(
                Phase.WORK,
                Duration.ofMinutes(25)));

        return new PomodoroReply(PomodoroMessages.WELCOME_MESSAGE, null, true);
    }

    /**
     * Обрабатывает ответ пользователя и возвращает сообщение
     */
    public PomodoroReply handleAnswer(Update update) {

        Long chatId = update.getMessage().getChatId();
        StringBuilder builder = new StringBuilder();
        var from = update.getMessage().getFrom();
        String firstName = from != null ? from.getFirstName() : "unknown";
        String userName = from != null ? from.getUserName() : "unknown";

        if (pomodoroManager.getSession(chatId).getState().equals(SessionState.SETUP)) {
            return checkUserSetupState(update, chatId);
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return new PomodoroReply(PomodoroMessages.DONT_UNDERSTAND_MESSAGE,
                    null, false);
        } else {
            String textMessage = update.getMessage().getText();
            PomodoroServiceSettings settings = pomodoroManager.getSettings(chatId);

            if (textMessage.equalsIgnoreCase(PomodoroMessages.START_MESSAGE)) {
                if (pomodoroManager.getSession(chatId).getState().equals(SessionState.WAITING)) {
                    pomodoroManager.startWorkSession(chatId, settings.workDuration());
                    sender.sendPomodoroReply(chatId, new PomodoroReply(
                            PomodoroMessages.MOTIVATION_REPLY,
                            pomodoroManager.chooseMotivationForSession(pomodoroManager.getSession(chatId)).pathToPhoto(),
                            false));
                    pomodoroManager.getSession(chatId).setState(SessionState.RUNNING);
                    scheduledPhaseEnd(chatId, settings.workDuration());
                } else if (pomodoroManager.getSession(chatId).getState().equals(SessionState.RUNNING)) {
                    sender.sendPomodoroReply(chatId, new PomodoroReply(
                            PomodoroMessages.DOUBLE_CALL,
                            null,
                            false));
                }
            } else if (textMessage.equalsIgnoreCase(PomodoroMessages.PAUSE_MESSAGE)) {
                pomodoroManager.cancelFuture(chatId);
                pomodoroManager.getSession(chatId).setState(SessionState.WAITING);
                sender.sendPomodoroReply(chatId, new PomodoroReply(PomodoroMessages.CANSEL_CURRENT_CYCLE, null, false));
            } else if (textMessage.equalsIgnoreCase(PomodoroMessages.END_SEANCE_MESSAGE)) {
                closingMessage(builder, chatId);
                pomodoroManager.cancelFuture(chatId);
                sender.sendPomodoroReply(chatId, new PomodoroReply(builder.toString(), null, true));
                sender.sendFinalStatsQuestion(chatId, PomodoroMessages.QUESTION_STATS_MESSAGE);
            } else if (textMessage.equalsIgnoreCase(PomodoroMessages.YES_ANSWER_MESSAGE)) {
                stats = csvStatsReader.readMonthlyStats(chatId);
                if (stats.getWorkSessions() == 0) {
                    sender.sendPomodoroReply(chatId, new PomodoroReply(PomodoroMessages.MESSAGE_WITHOUT_STATS, null, true));
                    log.info("Завершена сессия для пользователя chatId={}, firstName={}, userName={}", chatId, firstName, userName);
                    pomodoroManager.endSession(chatId);
                } else {
                    sender.sendPomodoroReply(chatId, new PomodoroReply(statsUtils.getStatsMessage(stats), null, true));
                    log.info("Завершена сессия для пользователя chatId={}, firstName={}, userName={}", chatId, firstName, userName);
                    pomodoroManager.endSession(chatId);
                }
            } else if (textMessage.equalsIgnoreCase(PomodoroMessages.NO_ANSWER_MESSAGE)) {
                sender.sendPomodoroReply(chatId, new PomodoroReply(PomodoroMessages.END_MESSAGE_WITHOUT_STATS, null, true));
                log.info("Завершена сессия для пользователя chatId={}, firstName={}, userName={}", chatId, firstName, userName);
                pomodoroManager.endSession(chatId);
            }

            return new PomodoroReply("", null, false);
        }
    }

    public void scheduledPhaseEnd(Long chatId, Duration duration) {
        ScheduledFuture<?> future = scheduled.schedule(
                () -> onPhaseFinished(chatId),
                duration.toMinutes(),
                TimeUnit.MINUTES
        );
        pomodoroManager.saveFuture(chatId, future);
    }

    void onPhaseFinished(Long chatId) {
        PomodoroSession session = pomodoroManager.getSession(chatId);
        PomodoroServiceSettings settings = pomodoroManager.getSettings(chatId);
        StringBuilder builder = new StringBuilder();

        if (pomodoroManager.isOverLimit(session)) {
            log.info("Завершена сессия для пользователя chatId={}", chatId);
            builder.append(PomodoroMessages.LIMIT_IS_UP_MESSAGE);
            closingMessage(builder, chatId);
            pomodoroManager.cancelFuture(chatId);

            Phase currentPhase = pomodoroManager.getSession(chatId).getCurrentPhase();
            Duration currentDuration = switch (currentPhase) {
                case WORK -> settings.workDuration();
                case SHORT_BREAK -> settings.shortRestDuration();
                case LONG_BREAK -> settings.longRestDuration();
            };
            statsLogger.logPhase(chatId, currentPhase, currentDuration, Instant.now());
            session.completeCurrentPhase();

            sender.sendPomodoroReply(chatId, new PomodoroReply(
                    builder.toString(),
                    pomodoroManager.chooseMotivationForSession(session).pathToPhoto(),
                    true));
            pomodoroManager.endSession(chatId);
        } else if (!session.isWarnedAboutLimit() &&
                pomodoroManager.isCloseToLimit(session, Duration.ofHours(2))) {
            builder.append(PomodoroMessages.WARNED_LIMIT_MESSAGE);
            session.setWantedAboutLimit(true);
            sender.sendPomodoroReply(chatId, new PomodoroReply(builder.toString(), null, false));
        }

        Phase nextPhase = pomodoroManager.getNextPhase(session, chatId);
        switch (nextPhase) {
            case SHORT_BREAK -> {
                if (session.isCurrentPhaseFinished()) {
                    session.completeCurrentPhase();
                    logCurrentPhase(chatId, settings);
                }
                session.setCurrentPhase(Phase.SHORT_BREAK);
                session.startCurrentPhase(settings.shortRestDuration());
                sender.sendPomodoroReply(chatId, new PomodoroReply(
                        PomodoroMessages.SHORT_REST_MESSAGE,
                        pomodoroManager.chooseMotivationForSession(session).pathToPhoto(),
                        false
                ));
                scheduledPhaseEnd(chatId, settings.shortRestDuration());
            }
            case LONG_BREAK -> {
                if (session.isCurrentPhaseFinished()) {
                    session.completeCurrentPhase();
                    logCurrentPhase(chatId, settings);
                }
                session.setCurrentPhase(Phase.LONG_BREAK);
                session.startCurrentPhase(settings.longRestDuration());
                sender.sendPomodoroReply(chatId, new PomodoroReply(
                        PomodoroMessages.LONG_REST_MESSAGE,
                        pomodoroManager.chooseMotivationForSession(session).pathToPhoto(),
                        false
                ));
                scheduledPhaseEnd(chatId, settings.longRestDuration());
            }
            case WORK -> {
                if (session.isCurrentPhaseFinished()) {
                    session.completeCurrentPhase();
                    logCurrentPhase(chatId, settings);
                }
                session.setCurrentPhase(Phase.WORK);
                session.startCurrentPhase(settings.workDuration());
                sender.sendPomodoroReply(chatId, new PomodoroReply(
                        PomodoroMessages.END_REST_MESSAGE,
                        pomodoroManager.chooseMotivationForSession(session).pathToPhoto(),
                        false
                ));
                scheduledPhaseEnd(chatId, settings.workDuration());
            }
        }
    }

    private PomodoroReply checkUserSetupState(Update update, Long chatId) {
        UserSetupState state = stateUsers.get(chatId);
        int value;
        String textMessage;
        String textAnswer = "";

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return new PomodoroReply(PomodoroMessages.WRONG_VALUE_MESSAGE, null, true);
        } else {
            textMessage = update.getMessage().getText().trim();
            try {
                value = Integer.parseInt(textMessage);
            } catch (NumberFormatException e) {
                return new PomodoroReply(PomodoroMessages.WRONG_VALUE_NOT_INTEGER_MESSAGE, null, true);
            }
            if (value <= 0) {
                return new PomodoroReply(PomodoroMessages.WRONG_VALUE_NOT_POSITIVE_MESSAGE, null, true);
            }
        }

        if (state.getStep().equals(SetupStep.WAITING_WORK_DURATION)) {
            state.setWorkDuration(Duration.ofMinutes(value));
            state.setStep(SetupStep.WAITING_SHORT_REST_DURATION);
            return new PomodoroReply(PomodoroMessages.CREATE_WORK_MESSAGE,
                    null, true);
        } else if (state.getStep().equals(SetupStep.WAITING_SHORT_REST_DURATION)) {
            state.setShortRestDuration(Duration.ofMinutes(value));
            state.setStep(SetupStep.WAITING_LONG_REST_DURATION);
            return new PomodoroReply(PomodoroMessages.CREATE_SHORT_REST_MESSAGE,
                    null, true);
        } else if (state.getStep().equals(SetupStep.WAITING_LONG_REST_DURATION)) {
            state.setLongRestDuration(Duration.ofMinutes(value));
            state.setStep(SetupStep.WAITING_COUNT_CYCLES);
            return new PomodoroReply(PomodoroMessages.CREATE_LONG_REST_MESSAGE,
                    null, true);
        } else if (state.getStep().equals(SetupStep.WAITING_COUNT_CYCLES)) {
            state.setSessionsBeforeLongBreak(value);
            state.setStep(SetupStep.READY);

            PomodoroServiceSettings settings = new PomodoroServiceSettings(
                    state.getWorkDuration(),
                    state.getShortRestDuration(),
                    state.getLongRestDuration(),
                    state.getSessionsBeforeLongBreak());
            textAnswer = PomodoroMessages.ALL_PERIODS_CREATE_MESSAGE;
            pomodoroManager.setSettings(chatId, settings);
            pomodoroManager.addSession(
                    chatId,
                    new PomodoroSession(Phase.WORK, pomodoroManager.getSettings(chatId).workDuration()));
            pomodoroManager.getSession(chatId).setState(SessionState.WAITING);
        }
        return new PomodoroReply(textAnswer, null, false);
    }

    /**
     * Формирует завершающее сообщение пользователю перед закрытием сессии
     */
    public void closingMessage(StringBuilder builder, Long chatId) {
        PomodoroSession session = pomodoroManager.getSession(chatId);
        String rank = pomodoroManager.calculateRank(session);
        String rankMessage = String.format(
                PomodoroMessages.CLOSING_MESSAGE_TEMPLATE,
                pomodoroManager.getSession(chatId).getCompleteWorkingCycles(),
                rank);
        builder.append(rankMessage);
    }

    void logCurrentPhase(Long chatId, PomodoroServiceSettings settings) {
        Phase currentPhase = pomodoroManager.getSession(chatId).getCurrentPhase();
        Duration currentDuration = switch (currentPhase) {
            case WORK -> settings.workDuration();
            case SHORT_BREAK -> settings.shortRestDuration();
            case LONG_BREAK -> settings.longRestDuration();
        };
        statsLogger.logPhase(chatId, currentPhase, currentDuration, Instant.now());
    }

    public boolean hasSession(Long chatId) {
        return pomodoroManager.hasActiveSession(chatId);
    }
}
