package vacancy_tracker.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vacancy_tracker.bot.VacancySender;
import vacancy_tracker.data.repository.UserRepository;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Реализация {@link NotificationService} на основе {@link ScheduledExecutorService}.
 * <p>
 * Для каждого пользователя хранит список запланированных задач и позволяет
 * запускать их по расписанию и отменять при необходимости.
 * <p>
 * Задачи выполняются ежедневно в указанное пользователем время
 * (с учётом его часового пояса).
 */
public class ScheduledNotificationService implements NotificationService {

    private static final Logger log = LogManager.getLogger(ScheduledNotificationService.class);

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    final Map<Long, List<ScheduledFuture<?>>> tasksByUser = new ConcurrentHashMap<>();

    private final UserService userService;
    private final UserRepository repository;
    private final VacancySearchService vacancySearchService;
    private final VacancySender vacancySender;

    public ScheduledNotificationService(
            UserService userService,
            UserRepository repository,
            VacancySearchService vacancySearchService,
            VacancySender vacancySender) {
        this.userService = userService;
        this.repository = repository;
        this.vacancySearchService = vacancySearchService;
        this.vacancySender = vacancySender;
    }

    /**
     * Планирует ежедневные уведомления для пользователя.
     *
     * @param user пользователь, для которого нужно запланировать уведомления
     * @throws IllegalArgumentException если user равен null
     * @throws IllegalStateException    если у пользователя не задано время уведомления
     */
    @Override
    public void scheduleNotifications(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user не может быть null");
        }

        Long userId = user.getUserId();
        ZoneOffset offset = user.getUtcOffset();
        LocalTime notifyTime = (user.getSettings() != null)
                ? user.getSettings().getNotificationTime()
                : null;

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
        log.info("Запланированы уведомления для пользователя {}, первая отправка через {} мс",
                userId, initialDelayMillis);
    }

    /**
     * Отменяет все запланированные задачи для пользователя.
     *
     * @param userId идентификатор пользователя
     */
    @Override
    public void cancelNotifications(Long userId) {
        List<ScheduledFuture<?>> futures = tasksByUser.remove(userId);
        if (futures != null) {
            for (ScheduledFuture<?> f : futures) {
                f.cancel(true);
            }
            log.info("Отменены уведомления для пользователя {}", userId);
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
     * @param now        текущий момент времени в UTC
     * @param offset     смещение часового пояса пользователя
     * @param notifyTime локальное время нотификации в часовом поясе пользователя
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
     * Задача:
     * <ul>
     *   <li>Получает пользователя из сервиса</li>
     *   <li>Ищет новые вакансии по его настройкам</li>
     *   <li>Отправляет их через {@link VacancySender}</li>
     *   <li>Обновляет время последнего запроса и сохраняет пользователя</li>
     * </ul>
     *
     * @param userId идентификатор пользователя
     * @return {@link Runnable}, представляющий запланированную задачу
     */
    private Runnable createNotificationTask(Long userId) {
        return () -> {
            try {
                User user = userService.getOrCreateUser(userId);
                if (user != null && user.isSettingsReady()) {
                    List<Vacancy> vacancies = vacancySearchService.findVacancies(user.getSettings());

                    if (!vacancies.isEmpty()) {
                        vacancySender.sendVacancies(userId, vacancies);
                        user.updateLastRequestTime(Instant.now());
                        repository.saveUser(user);
                        log.info("Отправлено {} вакансий пользователю {}, время обновлено",
                                vacancies.size(), userId);
                    } else {
                        log.debug("Новых вакансий для пользователя {} не найдено", userId);
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка при уведомлении пользователя {}", userId, e);
            }
        };
    }
}