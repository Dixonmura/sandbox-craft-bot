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
                .getResourceAsStream("assets/motivations/motivations.csv")) {

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
        statsLogger = new StatsLogger(writer, Path.of("Telegram_API/logs"));
        csvStatsReader = new CsvStatsReader(Path.of("Telegram_API/logs"), Clock.systemDefaultZone(), reader);
        statsUtils = new StatsUtils();
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

        String text = """
                Метод «Помодоро» — это работа короткими рывками с паузами 🍅
                Один «помидор» = сначала работа, потом короткий отдых — так легче не выгореть и не залипать в телефоне 💪
                
                Как будем работать:
                1️⃣ Ты задаёшь длительность рабочего интервала в минутах
                2️⃣ Я запускаю таймер и напомню, когда пора отдыхать ⏱️
                3️⃣ За каждый завершённый помидор ты копишь прогресс и получаешь звания 🏅
                
                Напиши, на сколько минут поставить первый рабочий интервал (только цифру, число больше 0️⃣).
                """;

        log.info("Первый запуск Pomodoro-бота для пользователя chatId={}", chatId);

        pomodoroManager.addSession(chatId, new PomodoroSession(
                Phase.WORK,
                Duration.ofMinutes(25)));

        return new PomodoroReply(text, null, true);
    }

    /**
     * Обрабатывает ответ пользователя и возвращает сообщение
     */
    public PomodoroReply handleAnswer(Update update) {

        Long chatId = update.getMessage().getChatId();
        StringBuilder builder = new StringBuilder();

        if (pomodoroManager.getSession(chatId).getState().equals(SessionState.SETUP)) {
            return checkUserSetupState(update, chatId);
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return new PomodoroReply("Не понял сообщение \uD83E\uDD14\n" +
                    "Для нужной команды нажмите на соответствующую кнопку на клавиатуре ниже ⬇\uFE0F",
                    null, false);
        } else {
            String textMessage = update.getMessage().getText();
            PomodoroServiceSettings settings = pomodoroManager.getSettings(chatId);

            if (textMessage.equalsIgnoreCase("Старт \uD83D\uDE80")) {
                if (pomodoroManager.getSession(chatId).getState().equals(SessionState.WAITING)) {
                    pomodoroManager.startWorkSession(chatId, settings.workDuration());
                    sender.sendPomodoroReply(chatId, new PomodoroReply(
                            "Отличный настрой! Отсчет пошел! ⏱\uFE0F",
                            pomodoroManager.chooseMotivationForSession(pomodoroManager.getSession(chatId)).pathToPhoto(),
                            false));
                    pomodoroManager.getSession(chatId).setState(SessionState.RUNNING);
                    scheduledPhaseEnd(chatId, settings.workDuration());
                } else if (pomodoroManager.getSession(chatId).getState().equals(SessionState.RUNNING)) {
                    sender.sendPomodoroReply(chatId, new PomodoroReply(
                            "Тссс… сессия уже идёт \uD83E\uDD2B\n" +
                                    "Подождите завершения текущего цикла ⏳",
                            null,
                            false));
                }
            } else if (textMessage.equalsIgnoreCase("Пауза ⏸\uFE0F")) {
                pomodoroManager.cancelFuture(chatId);
                pomodoroManager.getSession(chatId).setState(SessionState.WAITING);
                sender.sendPomodoroReply(chatId, new PomodoroReply("Текущий цикл отменён ⏹\uFE0F", null, false));
            } else if (textMessage.equalsIgnoreCase("Завершить сеанс ✅")) {
                closingMessage(builder, chatId);
                pomodoroManager.cancelFuture(chatId);
                sender.sendPomodoroReply(chatId, new PomodoroReply(builder.toString(), null, true));
                sender.sendFinalStatsQuestion(chatId, "📊 Хотите вывести статистику за последние 30 дней?");
            } else if (textMessage.equalsIgnoreCase("Да 📊")) {
                stats = csvStatsReader.readMonthlyStats(chatId);
                sender.sendPomodoroReply(chatId, new PomodoroReply(statsUtils.getStatsMessage(stats), null, true));
                log.info("Завершена сессия для пользователя chatId={}", chatId);
                pomodoroManager.endSession(chatId);
            } else if (textMessage.equalsIgnoreCase("Нет ❌")) {
                sender.sendPomodoroReply(chatId, new PomodoroReply(statsUtils.getStatsMessage(stats), null, true));
                log.info("Завершена сессия для пользователя chatId={}", chatId);
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

    private void onPhaseFinished(Long chatId) {
        PomodoroSession session = pomodoroManager.getSession(chatId);
        PomodoroServiceSettings settings = pomodoroManager.getSettings(chatId);
        StringBuilder builder = new StringBuilder();

        if (pomodoroManager.isOverLimit(session)) {
            log.info("Завершена сессия для пользователя chatId={}", chatId);
            builder.append("Уважаемый пользователь, сессия превысила лимит времени существования и будет закрыта ⏳\uD83D\uDEAA");
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
            builder.append("Уважаемый пользователь, с момента первого запуска сессии прошло уже более 14 часов ⏰")
                    .append("\nВ скором времени сессия будет закрыта по достижению лимита ⏳");
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
                        "Пора сделать короткий перерыв! \uD83E\uDDD8\u200D♂\uFE0F☕",
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
                        "Пора сделать длинный перерыв! \uD83C\uDF34☕",
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
                        "Перерыв окончен, поехали дальше! \uD83D\uDCAA",
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
            return new PomodoroReply("Кажется, что-то пошло не так \uD83E\uDD14\n" +
                    "Пожалуйста, введите запрошенное значение.", null, true);
        } else {
            textMessage = update.getMessage().getText().trim();
            try {
                value = Integer.parseInt(textMessage);
            } catch (NumberFormatException e) {
                return new PomodoroReply("Нужно ввести целое число, больше 0\uFE0F⃣ \uD83D\uDE42", null, true);
            }
            if (value <= 0) {
                return new PomodoroReply("Число должно быть больше 0, попробуйте ещё раз \uD83D\uDD01", null, true);
            }
        }

        if (state.getStep().equals(SetupStep.WAITING_WORK_DURATION)) {
            state.setWorkDuration(Duration.ofMinutes(value));
            state.setStep(SetupStep.WAITING_SHORT_REST_DURATION);
            return new PomodoroReply("Период рабочего цикла определён ✅\n" +
                    "Теперь отправьте период короткого отдыха в минутах ⏱\uFE0F",
                    null, true);
        } else if (state.getStep().equals(SetupStep.WAITING_SHORT_REST_DURATION)) {
            state.setShortRestDuration(Duration.ofMinutes(value));
            state.setStep(SetupStep.WAITING_LONG_REST_DURATION);
            return new PomodoroReply("Период короткого отдыха определён ✅\n" +
                    "Далее отправьте период длинного отдыха в минутах ⏱\uFE0F",
                    null, true);
        } else if (state.getStep().equals(SetupStep.WAITING_LONG_REST_DURATION)) {
            state.setLongRestDuration(Duration.ofMinutes(value));
            state.setStep(SetupStep.WAITING_COUNT_CYCLES);
            return new PomodoroReply("Период длинного отдыха определён ✅\n" +
                    "Теперь отправьте количество рабочих циклов до длинного отдыха \uD83D\uDD01",
                    null, true);
        } else if (state.getStep().equals(SetupStep.WAITING_COUNT_CYCLES)) {
            state.setSessionsBeforeLongBreak(value);
            state.setStep(SetupStep.READY);

            PomodoroServiceSettings settings = new PomodoroServiceSettings(
                    state.getWorkDuration(),
                    state.getShortRestDuration(),
                    state.getLongRestDuration(),
                    state.getSessionsBeforeLongBreak());
            textAnswer = "Количество циклов работы до длинного отдыха определено, теперь можно начинать! \nОжидаю команду \"Старт \uD83D\uDE80\"!";
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


        builder.append("Сессия завершена. ✅")
                .append("\nСовершено рабочих циклов: ")
                .append(session.getCompleteWorkingCycles())
                .append(" 💼")
                .append("\nВам присваивается звание")
                .append(" > ")
                .append(rank)
                .append("< ")
                .append("\uD83C\uDFC5");
    }

    private void logCurrentPhase(Long chatId, PomodoroServiceSettings settings) {
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
