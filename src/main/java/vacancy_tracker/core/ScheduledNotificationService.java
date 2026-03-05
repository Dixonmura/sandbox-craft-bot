package vacancy_tracker.core;

import vacancy_tracker.bot.VacancySender;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Реализация {@link NotificationService} на основе {@link ScheduledExecutorService}.
 * <p>
 * Для каждого пользователя хранит список запланированных задач и позволяет
 * запускать их по расписанию и отменять при необходимости.
 */
public class ScheduledNotificationService implements NotificationService {

    private final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(4);

    final Map<Long, List<ScheduledFuture<?>>> tasksByUser = new ConcurrentHashMap<>();
    private UserService userService;
    private final VacancySearchService vacancySearchService;
    private final VacancySender vacancySender;

    public ScheduledNotificationService(UserService userService, VacancySearchService vacancySearchService, VacancySender vacancySender) {
        this.userService = userService;
        this.vacancySearchService = vacancySearchService;
        this.vacancySender = vacancySender;
    }

    @Override
    public void scheduleNotifications(User user) {

        if (user == null) {
            throw new IllegalArgumentException("user не может быть null");
        }

        Long userId = user.getUserId();
        ZoneOffset offset = user.getUtcOffset();
        LocalTime notifyTime = (user.getSettings() != null) ? user.getSettings().getNotificationTime() : null;

        if (notifyTime == null) {
            throw new IllegalStateException("Нельзя запланировать уведомления без notificationTime");
        }

        long initialDelayMillis = computeInitialDelayMillis(Instant.now(), offset, notifyTime);
        long periodMillis = Duration.ofDays(1).toMillis();

        Runnable task = createNotificationTask(userId);

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(
                task,
                initialDelayMillis,
                periodMillis,
                TimeUnit.MILLISECONDS
        );

        tasksByUser.computeIfAbsent(userId, id -> new ArrayList<>()).add(future);
    }

    @Override
    public void cancelNotifications(Long userId) {
        List<ScheduledFuture<?>> futures = tasksByUser.remove(userId);
        if (futures != null) {
            for (ScheduledFuture<?> f : futures) {
                f.cancel(true);
            }
        }
    }

    /**
     * Вычисляет задержку до ближайшего запуска ежедневной задачи.
     * <p>
     * Расчёт выполняется в три шага:
     * <ol>
     *   <li>Определяется текущая дата и время в часовом поясе пользователя.</li>
     *   <li>Строится целевое локальное время уведомления на текущий день.</li>
     *   <li>Если целевое время уже прошло, используется то же время на следующий день.</li>
     * </ol>
     * Возвращаемое значение — разница между текущим моментом {@code now}
     * и ближайшим целевым временем в миллисекундах.
     *
     * @param now         текущий момент времени в UTC
     * @param offset      смещение часового пояса пользователя
     * @param notifyTime  локальное время нотификации в часовом поясе пользователя
     * @return задержка до ближайшего запуска задачи в миллисекундах
     */
    long computeInitialDelayMillis(Instant now, ZoneOffset offset, LocalTime notifyTime) {
        ZonedDateTime nowUserZone = now.atZone(offset);
        LocalDate today = nowUserZone.toLocalDate();

        ZonedDateTime target = ZonedDateTime.of(today, notifyTime, offset);
        if (!target.toInstant().isAfter(now)) {
            target = target.plusDays(1);
        }

        return Duration.between(now, target.toInstant()).toMillis();
    }

    /**
     * Создаёт задачу, которая будет выполняться по расписанию для указанного пользователя.
     * <p>
     * Внутри задачи позже можно вызвать сервисы поиска вакансий и отправки сообщений
     * в Telegram.
     *
     * @param userId идентификатор пользователя, для которого создаётся задача
     * @return {@link Runnable}, представляющий planned-задачу
     */
    private Runnable createNotificationTask(Long userId) {
        return () -> {
            try {
                User user = userService.getOrCreateUser(userId);
                if (user != null && user.isSettingsReady()) {
                    List<Vacancy> vacancies = vacancySearchService.findVacancies(user.getSettings());
                    if (!vacancies.isEmpty()) {
                        vacancySender.sendVacancies(userId, vacancies);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }
}
