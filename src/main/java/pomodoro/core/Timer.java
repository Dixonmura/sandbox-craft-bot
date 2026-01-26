package pomodoro.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;

/**
 * Класс представляющий и реализующий
 * поведение таймера.
 */
public class Timer {

    private static final Logger log = LogManager.getLogger(Timer.class);
    private Phase timerPhase;
    private Instant startTime;
    private final Duration duration;

    /**
     * Конструктор таймера
     * @param duration инициализация длительности таймера
     */
    public Timer(Phase phase, Duration duration) {
        this.duration = duration;
        this.timerPhase = phase;
    }

    /**
     * Начало отсчета времени, инициализация startTimer
     */
    public void startTimer() {
        startTime = Instant.now();
    }

    /**
     * Проверка остатка времени после объявления старта
     *
     * @return оставшееся время до завершения таймера
     * @throws IllegalStateException если метод вызван, когда startTimer null
     */
    public Duration remaining() {
        if (startTime == null) {
            log.error("Вызван метод remaining, когда startTime={}", startTime);
            throw new IllegalStateException("невозможно рассчитать оставшееся время до вызова команды startTimer()");
        }
        Duration elapsed = Duration.between(startTime, Instant.now());
        return duration.minus(elapsed);
    }

    public Phase getPhaseTimer() {
        return timerPhase;
    }

    public void setPhaseTimer(Phase timerPhase) {
        this.timerPhase = timerPhase;
    }

    /**
     * Метод проверяет завершение отсчета таймера
     * @return true если пройденное время совпадает или больше заданного промежутка
     */
    public boolean isFinished() {
        return remaining().isNegative() || remaining().isZero();
    }
}
