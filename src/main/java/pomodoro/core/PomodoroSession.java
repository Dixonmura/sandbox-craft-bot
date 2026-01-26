package pomodoro.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;

public class PomodoroSession {

    private static final Logger log = LogManager.getLogger(PomodoroSession.class);
    private final Instant startTime;
    private Timer timer;
    private Phase currentPhase;
    private SessionState state;
    private int completeWorkingCycles;
    private boolean warnedAboutLimit;
    private boolean finished;

    public PomodoroSession(Phase phase, Duration phaseDuration) {
        if (phaseDuration == null || !phaseDuration.isPositive()) {
            log.error("Попытка создать PomodoroSession с некорректным Duration {}", phaseDuration);
            throw new IllegalArgumentException("значение Duration должно быть позитивным");
        }
        finished = false;
        startTime = Instant.now();
        warnedAboutLimit = false;
        this.currentPhase = phase;
        timer = new Timer(currentPhase, phaseDuration);
    }

    /**
     * Проверяет не завершена ли сессия до старта таймера
     * Запускает таймер (текущую фазу)
     */
    public void startCurrentPhase(Duration duration) {
        ensureNotFinished();
        timer = new Timer(currentPhase, duration);
        timer.startTimer();
    }

    /**
     * Проверяет завершение текущей фазы (остаток заданного интервала)
     *
     * @return true если заданный интервал пройден
     */
    public boolean isCurrentPhaseFinished() {
        return timer.isFinished();
    }

    /**
     * Завершает текущую фазу без выбора следующей.
     * Возвращает true, если фаза была WORK и рабочий цикл засчитан.
     *
     * @throws IllegalStateException если завершение фазы вызвано, когда таймер ещё не завершил работу
     */
    public synchronized boolean completeCurrentPhase() {
        ensureNotFinished();
        if (!timer.isFinished()) {
            log.error("Завершение фазы вызвано, когда таймер ещё не завершил работу");
            throw new IllegalStateException("Фаза ещё не завершена");
        }
        boolean workCompleted = currentPhase == Phase.WORK;
        if (workCompleted) {
            completeWorkingCycles++;
        }
        return workCompleted;
    }

    /**
     * Проверяет не завершена ли сессия до начала работы
     *
     * @throws IllegalStateException если до старта сессия завершена
     */
    private void ensureNotFinished() {
        if (finished) {
            log.error("Сессия завершена до начала работы таймера");
            throw new IllegalStateException("Сессия уже завершена");
        }
    }

    public Duration getReminingTime() {
        return timer.remaining();
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public int getCompleteWorkingCycles() {
        return completeWorkingCycles;
    }

    public boolean isFinished() {
        return finished;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public boolean isWarnedAboutLimit() {
        return warnedAboutLimit;
    }

    public void setCurrentPhase(Phase phase) {
        currentPhase = phase;
    }

    public synchronized void setWantedAboutLimit(boolean wantedAboutLimit) {
        this.warnedAboutLimit = wantedAboutLimit;
    }

    public synchronized void setFinished(boolean finishedFlag) {
        finished = finishedFlag;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }
}
