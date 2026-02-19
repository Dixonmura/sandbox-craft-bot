package markups;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import vacancy_tracker.bot.CallbackPrefixes;
import vacancy_tracker.bot.types.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacancyTrackerKeyboardFactoryTest {

    VacancyTrackerKeyboardFactory keyboardFactory;

    @BeforeEach
    void setUp() {
        keyboardFactory = new VacancyTrackerKeyboardFactory();
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для запуска бота")
    void createStartKeyboard_shouldCreateKeyboardWithOnlyOneButton() {
        InlineKeyboardMarkup keyboardMarkup = keyboardFactory.createStartKeyboard();
        assertThat(keyboardMarkup.getKeyboard())
                .isNotEmpty()
                .hasSize(1);

        InlineKeyboardRow row = keyboardMarkup.getKeyboard().getFirst();
        assertThat(row)
                .isNotEmpty()
                .hasSize(1);

        assertThat(row.getFirst().getText())
                .isEqualTo(StartBotOption.START_BOT.getTitle());

        assertThat(row.getFirst().getCallbackData())
                .isEqualTo(StartBotOption.START_BOT.getTitle());
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для выбора региона")
    void createRegionKeyboard_shouldCreateKeyboardWithFirstFiveValues_whenKeyboardPageIsZero() {
        InlineKeyboardMarkup keyboardMarkup = keyboardFactory.createRegionKeyboard(0);

        List<InlineKeyboardRow> rows = keyboardMarkup.getKeyboard();
        assertThat(rows)
                .isNotEmpty()
                .hasSize(6);

        InlineKeyboardRow firstRow = rows.getFirst();
        assertThat(firstRow)
                .isNotEmpty()
                .hasSize(1);

        assertThat(firstRow.getFirst().getText())
                .isEqualTo("Республика Адыгея (1)");

        assertThat(firstRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.REGION_CODE_PREFIX + "1");

        InlineKeyboardRow lastRow = rows.getLast();
        assertThat(lastRow)
                .isNotEmpty()
                .hasSize(1);

        assertThat(lastRow.getFirst().getText())
                .isEqualTo(NavigationAction.FURTHER.getTitle());

        assertThat(lastRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.REGION_PAGE_PREFIX + "1");
    }

    @Test
    @DisplayName("Проверка корректного смещения страниц клавиатуры для выбора региона")
    void createRegionKeyboard_shouldCreateKeyboardWithTwoNavigateButton_whenKeyboardPageNotZero() {
        InlineKeyboardMarkup keyboardMarkup = keyboardFactory.createRegionKeyboard(3);

        List<InlineKeyboardRow> rows = keyboardMarkup.getKeyboard();
        assertThat(rows)
                .isNotEmpty()
                .hasSize(6);

        InlineKeyboardRow firstRow = rows.getFirst();
        assertThat(firstRow)
                .isNotEmpty()
                .hasSize(1);

        assertThat(firstRow.getFirst().getText())
                .isEqualTo("Камчатский край (41)");

        assertThat(firstRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.REGION_CODE_PREFIX + "41");

        InlineKeyboardRow lastRow = rows.getLast();
        assertThat(lastRow)
                .isNotEmpty()
                .hasSize(2);

        assertThat(lastRow.getFirst().getText())
                .isEqualTo(NavigationAction.RETURN.getTitle());

        assertThat(lastRow.getLast().getText())
                .isEqualTo(NavigationAction.FURTHER.getTitle());

        assertThat(lastRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.REGION_PAGE_PREFIX + "2");

        assertThat(lastRow.getLast().getCallbackData())
                .isEqualTo(CallbackPrefixes.REGION_PAGE_PREFIX + "4");
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры настроек")
    void createSettingKeyboard_shouldCreateSettingKeyboardWithCorrectlyData() {
        InlineKeyboardMarkup settingKeyboard = keyboardFactory.createSettingKeyboard();
        assertThat(settingKeyboard.getKeyboard())
                .isNotEmpty()
                .hasSize(6);

        InlineKeyboardRow firstRow = settingKeyboard.getKeyboard().getFirst();

        assertThat(firstRow.getFirst().getText())
                .isNotNull()
                .isEqualTo(SettingOptions.REGION.getTitle());

        assertThat(firstRow.getFirst().getCallbackData())
                .isNotNull()
                .isEqualTo(CallbackPrefixes.SETTING_PREFIX + SettingOptions.REGION);

        InlineKeyboardRow lastRow = settingKeyboard.getKeyboard().getLast();

        assertThat(lastRow.getFirst().getText())
                .isNotNull()
                .isEqualTo(ReadyAction.COMPLETE.getTitle());

        assertThat(lastRow.getFirst().getCallbackData())
                .isNotNull()
                .isEqualTo(CallbackPrefixes.SETTING_PREFIX + ReadyAction.COMPLETE);
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для установки фильтра опыта пользователя")
    void createExperienceKeyboard_shouldCreateExperienceKeyboardWithCorrectlyData() {
        InlineKeyboardMarkup expKeyboard = keyboardFactory.createExperienceKeyboard();
        assertThat(expKeyboard.getKeyboard())
                .isNotNull()
                .hasSize(6);

        InlineKeyboardRow firstRow = expKeyboard.getKeyboard().getFirst();

        assertThat(firstRow.getFirst().getText())
                .isEqualTo("от 1 года");

        assertThat(firstRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.EXPERIENCE_PREFIX + "1");

        InlineKeyboardRow lastRow = expKeyboard.getKeyboard().getLast();

        assertThat(lastRow.getFirst().getText())
                .isEqualTo(ExperienceOption.WITHOUT_EXPERIENCE.getTitle());

        assertThat(lastRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.EXPERIENCE_PREFIX + ExperienceOption.WITHOUT_EXPERIENCE);
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для установки фильтра опыта пользователя")
    void createSalaryKeyboard_shouldCreateSalaryKeyboardWithCorrectlyData() {
        InlineKeyboardMarkup salaryKeyboard = keyboardFactory.createSalaryKeyboard();
        assertThat(salaryKeyboard.getKeyboard())
                .isNotNull()
                .hasSize(6);

        InlineKeyboardRow firstRow = salaryKeyboard.getKeyboard().getFirst();

        assertThat(firstRow.getFirst().getText())
                .isEqualTo("от 25000");

        assertThat(firstRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.SALARY_PREFIX + "25000");

        InlineKeyboardRow lastRow = salaryKeyboard.getKeyboard().getLast();

        assertThat(lastRow.getFirst().getText())
                .isEqualTo(SalaryOption.NOT_TAKE_SALARY.getTitle());

        assertThat(lastRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.SALARY_PREFIX + SalaryOption.NOT_TAKE_SALARY);
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для установки ключевого слова для поиска")
    void createKeyWordKeyboard_shouldCreateKeyWordKeyboardWithCorrectlyData() {
        InlineKeyboardMarkup KeyWordKeyboard = keyboardFactory.createKeyWordKeyboard();
        assertThat(KeyWordKeyboard.getKeyboard())
                .isNotNull()
                .hasSize(6);

        InlineKeyboardRow firstRow = KeyWordKeyboard.getKeyboard().getFirst();

        assertThat(firstRow.getFirst().getText())
                .isEqualTo("Backend Developer");

        assertThat(firstRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.KEY_WORD_PREFIX + "Backend Developer");

        InlineKeyboardRow lastRow = KeyWordKeyboard.getKeyboard().getLast();

        assertThat(lastRow.getFirst().getText())
                .isEqualTo(KeyWordOption.LEAVE_IT_EMPTY.getTitle());

        assertThat(lastRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.KEY_WORD_PREFIX + KeyWordOption.LEAVE_IT_EMPTY);
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для выбора времени сдвига по UTC")
    void createUtcOffsetsKeyboard_shouldCreateUtcOffsetsKeyboardWithFirstFiveValues_whenKeyboardPageIsOne() {
        InlineKeyboardMarkup keyboardMarkup = keyboardFactory.createUtcOffsetsKeyboard(1);

        List<InlineKeyboardRow> rows = keyboardMarkup.getKeyboard();
        assertThat(rows)
                .isNotEmpty()
                .hasSize(6);

        InlineKeyboardRow firstRow = rows.getFirst();
        assertThat(firstRow)
                .isNotEmpty()
                .hasSize(1);

        assertThat(firstRow.getFirst().getText())
                .isEqualTo("UTC-07:00");

        assertThat(firstRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.UTC_OFFSET_PREFIX + "-0700");

        InlineKeyboardRow lastRow = rows.getLast();
        assertThat(lastRow)
                .isNotEmpty()
                .hasSize(2);

        assertThat(lastRow.getFirst().getText())
                .isEqualTo(NavigationAction.RETURN.getTitle());

        assertThat(lastRow.getLast().getText())
                .isEqualTo(NavigationAction.FURTHER.getTitle());

        assertThat(lastRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.UTC_OFFSET_PAGE_PREFIX + "0");

        assertThat(lastRow.getLast().getCallbackData())
                .isEqualTo(CallbackPrefixes.UTC_OFFSET_PAGE_PREFIX + "2");

        InlineKeyboardMarkup firstPageKeyboardMarkup = keyboardFactory.createUtcOffsetsKeyboard(0);

        List<InlineKeyboardRow> rowsFirstPage = firstPageKeyboardMarkup.getKeyboard();
        assertThat(rows)
                .isNotEmpty()
                .hasSize(6);

        InlineKeyboardRow lastRowFirstPage = rowsFirstPage.getLast();
        assertThat(lastRowFirstPage)
                .isNotEmpty()
                .hasSize(1);
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для выбора времени нотификации")
    void createNotifyTimeKeyboard_shouldCreateKeyboardWithFirstFiveValues_whenKeyboardPageIsOne() {
        InlineKeyboardMarkup keyboardMarkup = keyboardFactory.createNotifyTimeKeyboard(1);

        List<InlineKeyboardRow> rows = keyboardMarkup.getKeyboard();
        assertThat(rows)
                .isNotEmpty()
                .hasSize(6);

        InlineKeyboardRow firstRow = rows.getFirst();
        assertThat(firstRow)
                .isNotEmpty()
                .hasSize(1);

        assertThat(firstRow.getFirst().getText())
                .isEqualTo("14:00");

        assertThat(firstRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.NOTIFY_TIME_PREFIX + "1400");

        InlineKeyboardRow lastRow = rows.getLast();
        assertThat(lastRow)
                .isNotEmpty()
                .hasSize(2);

        assertThat(lastRow.getFirst().getText())
                .isEqualTo(NavigationAction.RETURN.getTitle());

        assertThat(lastRow.getLast().getText())
                .isEqualTo(NavigationAction.FURTHER.getTitle());

        assertThat(lastRow.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.NOTIFY_PAGE_PREFIX + "0");

        assertThat(lastRow.getLast().getCallbackData())
                .isEqualTo(CallbackPrefixes.NOTIFY_PAGE_PREFIX + "2");

        InlineKeyboardMarkup lastPageKeyboardMarkup = keyboardFactory.createNotifyTimeKeyboard(2);

        List<InlineKeyboardRow> rowsLastPage = lastPageKeyboardMarkup.getKeyboard();
        assertThat(rowsLastPage)
                .isNotEmpty()
                .hasSize(4);
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для простых ответов Да и Нет")
    void createYesOrNoKeyboard_shouldCreateYesOrNoKeyboardWithCorrectlyData() {
        InlineKeyboardMarkup yesOrNoKeyboard = keyboardFactory.createYesOrNoKeyboard();
        assertThat(yesOrNoKeyboard.getKeyboard())
                .isNotNull()
                .hasSize(1);

        InlineKeyboardRow row = yesOrNoKeyboard.getKeyboard().getFirst();

        assertThat(row.getFirst().getText())
                .isEqualTo(ReadyAction.YES.getTitle());

        assertThat(row.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.YES_OR_NO_PREFIX + ReadyAction.YES);

        assertThat(row.getLast().getText())
                .isEqualTo(ReadyAction.NO.getTitle());

        assertThat(row.getLast().getCallbackData())
                .isEqualTo(CallbackPrefixes.YES_OR_NO_PREFIX + ReadyAction.NO);
    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для запуска планировщика")
    void createReadyKeyboard_shouldCreateReadyKeyboardWithCorrectlyData() {
        InlineKeyboardMarkup readyKeyboard = keyboardFactory.createReadyKeyboard();
        assertThat(readyKeyboard.getKeyboard())
                .isNotNull()
                .hasSize(1);

        InlineKeyboardRow row = readyKeyboard.getKeyboard().getFirst();

        assertThat(row.getFirst().getText())
                .isEqualTo(StartStopOptions.START.getTitle());

        assertThat(row.getFirst().getCallbackData())
                .isEqualTo(CallbackPrefixes.READY_PREFIX + StartStopOptions.START);

    }

    @Test
    @DisplayName("Проверка корректного создания клавиатуры для остановки планировщика")
    void createStopKeyboard_shouldCreateStopKeyboardWithCorrectlyData() {
        InlineKeyboardMarkup stopKeyboard = keyboardFactory.createStopKeyboard();
        assertThat(stopKeyboard.getKeyboard())
                .isNotNull()
                .hasSize(1);

        InlineKeyboardRow row = stopKeyboard.getKeyboard().getFirst();

        assertThat(row.getFirst().getText())
                .isEqualTo("Остановить планировщик");

        assertThat(row.getFirst().getCallbackData())
                .isEqualTo("STOP:STOP");

    }

    @Test
    @DisplayName("Проверка выброса исключения, когда номер страницы меньше нуля или больше размера списка")
    void createRegionKeyboard_shouldThrowsIllegalArgumentException_whenInputDataInvalid() {
        assertThatThrownBy(() ->
                keyboardFactory.createRegionKeyboard(-3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Номер страницы не может быть меньше нуля");

        assertThatThrownBy(() ->
                keyboardFactory.createRegionKeyboard(31))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Номер страницы превышает размер списка");

        assertThatThrownBy(() ->
                keyboardFactory.createNotifyTimeKeyboard(-3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Номер страницы не может быть меньше нуля");

        assertThatThrownBy(() ->
                keyboardFactory.createNotifyTimeKeyboard(31))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Номер страницы превышает размер списка");
    }
}