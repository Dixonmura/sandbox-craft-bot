package pomodoro.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pomodoro.core.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Управляет PomodoroSession:
 * Контролирует фазы сессии, выдаёт мотивирующее фото с сообщением в соответствующей фазе
 */
public class PomodoroManager {

    private static final Logger log = LogManager.getLogger(PomodoroManager.class);
    private static final Duration MAX_SESSION_DURATION = Duration.ofHours(16);
    private final Map<Long, ScheduledFuture<?>> scheduledTasks;
    private final Map<Phase, List<MotivationPhoto>> motivationPhotos;
    private final Map<Long, PomodoroSession> sessions;
    private final Map<Long, PomodoroServiceSettings> settings;

    public PomodoroManager(Map<Phase, List<MotivationPhoto>> motivationPhotos) {
        if (motivationPhotos == null || motivationPhotos.isEmpty()) {
            log.error("Попытка создать PomodoroManager, когда motivationPhoto пустой или null");
            throw new IllegalArgumentException("motivationPhotos не может быть пустым или null");
        }

        this.motivationPhotos = motivationPhotos;
        this.settings = new ConcurrentHashMap<>();
        this.scheduledTasks = new ConcurrentHashMap<>();
        this.sessions = new ConcurrentHashMap<>();
    }

    /**
     * Запускает сессию для фазы работы
     *
     * @param chatId идентификатор пользователя
     */
    public void startWorkSession(Long chatId, Duration phaseDuration) {
        PomodoroSession session = sessions.get(chatId);
        session.setCurrentPhase(Phase.WORK);
        session.startCurrentPhase(phaseDuration);
    }

    /**
     * Запускает сессию для фазы короткого отдыха
     *
     * @param chatId идентификатор пользователя
     */
    public void startShortRestSession(Long chatId, Duration phaseDuration) {
        PomodoroSession session = sessions.get(chatId);
        session.setCurrentPhase(Phase.SHORT_BREAK);
        session.startCurrentPhase(phaseDuration);
    }

    /**
     * Запускает сессию для фазы длинного отдыха
     *
     * @param chatId идентификатор пользователя
     */
    public void startLongRestSession(Long chatId, Duration phaseDuration) {
        PomodoroSession session = sessions.get(chatId);
        session.setCurrentPhase(Phase.LONG_BREAK);
        session.startCurrentPhase(phaseDuration);
    }

    /**
     * Выбирает список мотивационную фотографию согласно текущей фазы сессии
     *
     * @param session текущая сессия
     * @return случайное мотивирующее фото из списка для текущей фазы
     */
    public MotivationPhoto chooseMotivationForSession(PomodoroSession session) {
        Phase phase = session.getCurrentPhase();
        List<MotivationPhoto> listMotivations = motivationPhotos.get(phase);
        if (listMotivations == null || listMotivations.isEmpty()) {
            log.error("вызван метод chooseMotivationForSession, когда список мотивационных фото null или пустой");
            throw new IllegalStateException("Нет мотивационных фото для фазы " + phase);
        }
        int index = ThreadLocalRandom.current().nextInt(listMotivations.size());
        return listMotivations.get(index);
    }

    /**
     * Проверяет, когда пользователь должен сделать длинный перерыв
     *
     * @param session текущая сессия
     * @return результат проверки опираясь на количество пройденных рабочих циклов
     */
    public boolean shouldStartLongBreak(PomodoroSession session, Long chatId) {
        int cycles = session.getCompleteWorkingCycles();
        int cyclesBeforeLongBreak = settings.get(chatId).sessionsBeforeLongBreak();
        return cycles > 0 && cycles % cyclesBeforeLongBreak == 0;
    }

    /**
     * Определяет следующую фазу по текущей фазе и количеству пройденных рабочих циклов
     *
     * @param session текущая сессия
     * @return следующую фазу исходя из секущей фазы сессии
     */
    public Phase getNextPhase(PomodoroSession session, Long chatId) {
        Phase currentPhase = session.getCurrentPhase();
        return switch (currentPhase) {
            case WORK -> (shouldStartLongBreak(session, chatId)) ? Phase.LONG_BREAK : Phase.SHORT_BREAK;
            case LONG_BREAK, SHORT_BREAK -> Phase.WORK;
        };
    }

    /**
     * Использует количество пройденных рабочих циклов сессии
     *
     * @param session текущая сессия
     * @return ранг, соответствующий количеству пройденных рабочих циклов
     */
    public String calculateRank(PomodoroSession session) {
        int cycles = session.getCompleteWorkingCycles();
        return PomodoroRank.fromCycles(cycles).title();
    }

    /**
     * Проверяет прошло ли максимальное время существования сессии
     * сравнивая прошедший период времени с максимально установленным
     */
    public boolean isOverLimit(PomodoroSession session) {
        return Duration.between(session.getStartTime(), Instant.now())
                .compareTo(MAX_SESSION_DURATION) >= 0;
    }

    /**
     * Опираясь на фиксированный лимит времени жизни сессии
     * по достижении указанного промежутка времени, заблаговременно
     * предупреждает пользователя о скором закрытии сессии по лимиту времени
     */
    public boolean isCloseToLimit(PomodoroSession session, Duration warnBefore) {
        Duration elapsed = Duration.between(session.getStartTime(), Instant.now());
        return elapsed.compareTo(MAX_SESSION_DURATION.minus(warnBefore)) >= 0;
    }

    /**
     * Удаляет существующую сессию по chatId
     */
    public void endSession(Long chatId) {
        if (sessions.get(chatId) != null) {
            sessions.remove(chatId);
        } else {
            log.warn("Попытка удаления несуществующей сессии по chatId={}", chatId);
        }
    }

    /**
     * Отменяет действующую задачу в планировщике
     */
    public void cancelFuture(Long chatId) {
        ScheduledFuture<?> future = scheduledTasks.remove(chatId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * Добавляет сессию в список сессий
     */
    public void addSession(Long chatId, PomodoroSession session) {
        session.setState(SessionState.SETUP);
        sessions.put(chatId, session);
    }

    public boolean hasActiveSession(Long chatId) {
        PomodoroSession session = sessions.get(chatId);
        return session != null && !session.isFinished();
    }

    public void saveFuture(Long chatId, ScheduledFuture<?> future) {
        scheduledTasks.put(chatId, future);
    }

    public PomodoroServiceSettings getSettings(Long chatId) {
        return settings.get(chatId);
    }

    public PomodoroSession getSession(Long chatId) {
        if (sessions.get(chatId) == null) {
            throw new IllegalStateException("Невозможно получить сессию до её добавления в список");
        }
        return sessions.get(chatId);
    }

    public void setSettings(Long chatId, PomodoroServiceSettings pomodoroServiceSettings) {
        settings.put(chatId, pomodoroServiceSettings);
    }
}
