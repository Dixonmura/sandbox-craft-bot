package vacancy_tracker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vacancy_tracker.bot.VacancySender;
import vacancy_tracker.data.repository.UserRepository;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledNotificationServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRepository repository;
    @Mock
    private VacancySearchService vacancySearchService;
    @Mock
    private VacancySender vacancySender;

    private ScheduledNotificationService notificationService;
    private final Long USER_ID = 123L;

    @BeforeEach
    void setUp() {
        notificationService = new ScheduledNotificationService(
                userService, repository, vacancySearchService, vacancySender
        );
    }

    private User createTestUserWithNotifyTime(LocalTime notifyTime) {
        User user = new User(USER_ID);
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        UserSettings settings = new UserSettings(77, 3, 100000, "Java", notifyTime);
        user.updateSettings(settings);
        return user;
    }

    @Test
    @DisplayName("scheduleNotifications планирует задачу при валидных данных")
    void scheduleNotifications_shouldScheduleTask_whenDataIsValid() {
        LocalTime notifyTime = LocalTime.parse("09:00");
        User user = createTestUserWithNotifyTime(notifyTime);

        notificationService.scheduleNotifications(user);

        assertThat(notificationService.tasksByUser).containsKey(USER_ID);
        assertThat(notificationService.tasksByUser.get(USER_ID)).hasSize(1);
    }

    @Test
    @DisplayName("scheduleNotifications бросает исключение при null пользователе")
    void scheduleNotifications_shouldThrowException_whenUserIsNull() {
        assertThatThrownBy(() -> notificationService.scheduleNotifications(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user не может быть null");
    }

    @Test
    @DisplayName("scheduleNotifications бросает исключение при отсутствии времени уведомления")
    void scheduleNotifications_shouldThrowException_whenNotifyTimeIsNull() {
        User user = new User(USER_ID);
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        // Без настроек и без notificationTime

        assertThatThrownBy(() -> notificationService.scheduleNotifications(user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя запланировать уведомления без notificationTime");
    }

    @Test
    @DisplayName("cancelNotifications отменяет задачи пользователя")
    void cancelNotifications_shouldCancelUserTasks() {
        // Сначала планируем задачу
        LocalTime notifyTime = LocalTime.parse("09:00");
        User user = createTestUserWithNotifyTime(notifyTime);
        notificationService.scheduleNotifications(user);

        assertThat(notificationService.tasksByUser).containsKey(USER_ID);

        // Отменяем
        notificationService.cancelNotifications(USER_ID);

        assertThat(notificationService.tasksByUser).doesNotContainKey(USER_ID);
    }

    @Test
    @DisplayName("cancelNotifications ничего не делает для несуществующего пользователя")
    void cancelNotifications_shouldDoNothing_whenUserHasNoTasks() {
        notificationService.cancelNotifications(999L);
        // Не должно быть исключений
    }

    @Test
    @DisplayName("computeInitialDelayMillis вычисляет задержку для времени в будущем")
    void computeInitialDelayMillis_shouldCalculateDelay_whenTargetIsInFuture() {
        Instant now = Instant.parse("2024-01-01T08:00:00Z");
        ZoneOffset offset = ZoneOffset.ofHours(3);
        LocalTime notifyTime = LocalTime.parse("12:00");

        long delay = notificationService.computeInitialDelayMillis(now, offset, notifyTime);

        // Время пользователя: 2024-01-01T11:00:00+03:00
        // Цель: 2024-01-01T12:00:00+03:00 = 2024-01-01T09:00:00Z
        // Разница: 1 час = 3600000 мс
        assertThat(delay).isEqualTo(3600000);
    }

    @Test
    @DisplayName("computeInitialDelayMillis добавляет день, если время уже прошло")
    void computeInitialDelayMillis_shouldAddDay_whenTargetIsInPast() {
        Instant now = Instant.parse("2024-01-01T10:00:00Z");
        ZoneOffset offset = ZoneOffset.ofHours(3);
        LocalTime notifyTime = LocalTime.parse("09:00");

        long delay = notificationService.computeInitialDelayMillis(now, offset, notifyTime);

        // Время пользователя: 2024-01-01T13:00:00+03:00
        // Цель на сегодня: 2024-01-01T09:00:00+03:00 = 2024-01-01T06:00:00Z (уже прошло)
        // Берём завтра: 2024-01-02T09:00:00+03:00 = 2024-01-02T06:00:00Z
        // Разница: 20 часов = 72000000 мс
        assertThat(delay).isEqualTo(72000000);
    }

    @Test
    @DisplayName("createNotificationTask при запуске ищет вакансии и отправляет их")
    void notificationTask_shouldSearchAndSendVacancies() throws Exception {
        // Создаём пользователя
        User user = createTestUserWithNotifyTime(LocalTime.parse("09:00"));
        when(userService.getOrCreateUser(USER_ID)).thenReturn(user);

        // Мокаем поиск вакансий
        List<Vacancy> vacancies = List.of(mock(Vacancy.class), mock(Vacancy.class));
        when(vacancySearchService.findVacancies(user.getSettings())).thenReturn(vacancies);

        // Получаем задачу через рефлексию или создаём тестовый executor
        Runnable task = createNotificationTaskViaReflection(USER_ID);
        task.run();

        // Проверяем, что вакансии отправились
        verify(vacancySender).sendVacancies(USER_ID, vacancies);
        // Проверяем, что время обновилось
        verify(repository).saveUser(user);
        assertThat(user.getSettings().getLastRequestTime()).isNotNull();
    }

    @Test
    @DisplayName("createNotificationTask не отправляет вакансии, если их нет")
    void notificationTask_shouldNotSend_whenNoVacancies() throws Exception {
        User user = createTestUserWithNotifyTime(LocalTime.parse("09:00"));
        when(userService.getOrCreateUser(USER_ID)).thenReturn(user);
        when(vacancySearchService.findVacancies(user.getSettings())).thenReturn(List.of());

        Runnable task = createNotificationTaskViaReflection(USER_ID);
        task.run();

        verify(vacancySender, never()).sendVacancies(anyLong(), anyList());
        verify(repository, never()).saveUser(any());
    }

    @Test
    @DisplayName("createNotificationTask не выполняется, если пользователь не готов")
    void notificationTask_shouldNotRun_whenUserNotReady() throws Exception {
        User user = new User(USER_ID); // без настроек
        when(userService.getOrCreateUser(USER_ID)).thenReturn(user);

        Runnable task = createNotificationTaskViaReflection(USER_ID);
        task.run();

        verify(vacancySearchService, never()).findVacancies(any());
        verify(vacancySender, never()).sendVacancies(anyLong(), anyList());
    }

    // Вспомогательный метод для доступа к private Runnable через рефлексию
    private Runnable createNotificationTaskViaReflection(Long userId) throws Exception {
        var method = ScheduledNotificationService.class.getDeclaredMethod("createNotificationTask", Long.class);
        method.setAccessible(true);
        return (Runnable) method.invoke(notificationService, userId);
    }
}