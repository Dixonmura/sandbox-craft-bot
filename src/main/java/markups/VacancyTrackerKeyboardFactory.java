package markups;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import vacancy_tracker.bot.types.*;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.min;
import static vacancy_tracker.bot.CallbackPrefixes.*;
import static vacancy_tracker.bot.types.ReadyAction.COMPLETE;
import static vacancy_tracker.bot.types.ReadyAction.GO_BACK;
import static vacancy_tracker.bot.types.SettingOptions.*;
import static vacancy_tracker.bot.types.StartSchedulerOptions.START_SCHEDULER;
import static vacancy_tracker.bot.types.StartStopBotOption.OUT_IN_ROUTER;
import static vacancy_tracker.bot.types.StartStopBotOption.STOP_BOT;

public class VacancyTrackerKeyboardFactory {

    private static final Logger log = LogManager.getLogger(VacancyTrackerKeyboardFactory.class);

    private final List<String> utcOffsets = List.of(
            "UTC-12:00",
            "UTC-11:00",
            "UTC-10:00",
            "UTC-09:00",
            "UTC-08:00",
            "UTC-07:00",
            "UTC-06:00",
            "UTC-05:00",
            "UTC-04:00",
            "UTC-03:00",
            "UTC-02:00",
            "UTC-01:00",
            "UTC+01:00",
            "UTC+02:00",
            "UTC+03:00",
            "UTC+04:00",
            "UTC+05:00",
            "UTC+06:00",
            "UTC+07:00",
            "UTC+08:00",
            "UTC+09:00",
            "UTC+10:00",
            "UTC+11:00",
            "UTC+12:00",
            "UTC+13:00",
            "UTC+14:00"
    );

    private final List<String> listRegions = List.of(
            "Республика Адыгея : 1",
            "Республика Алтай : 4",
            "Республика Башкортостан : 2",
            "Республика Бурятия : 3",
            "Владимирская область : 33",
            "Волгоградская область : 34",
            "Вологодская область : 35",
            "Воронежская область : 36",
            "Еврейская автономная область : 79",
            "Забайкальский край : 75",
            "Ивановская область : 37",
            "Иркутская область : 38",
            "Кабардино-Балкарская Республика : 7",
            "Калининградская область : 39",
            "Калужская область : 40",
            "Камчатский край : 41",
            "Кемеровская область : 42",
            "Кировская область : 43",
            "Костромская область : 44",
            "Краснодарский край : 23",
            "Красноярский край : 24",
            "Курганская область : 45",
            "Курская область : 46",
            "Ленинградская область : 47",
            "Липецкая область : 48",
            "Магаданская область : 49",
            "Москва : 77",
            "Московская область : 50",
            "Мурманская область : 51",
            "Ненецкий автономный округ : 83",
            "Нижегородская область : 52",
            "Новгородская область : 53",
            "Новосибирская область : 54",
            "Омская область : 55",
            "Оренбургская область : 56",
            "Орловская область : 57",
            "Пензенская область : 58",
            "Пермский край : 59",
            "Псковская область : 60",
            "Приморский край : 25",
            "Республика Дагестан : 5",
            "Республика Ингушетия : 6",
            "Республика Калмыкия : 8",
            "Республика Карелия : 10",
            "Республика Коми : 11",
            "Республика Крым : 82",
            "Республика Марий Эл : 12",
            "Республика Мордовия : 13",
            "Республика Саха (Якутия) : 14",
            "Республика Северная Осетия — Алания : 15",
            "Республика Татарстан : 16",
            "Республика Тыва : 17",
            "Республика Хакасия : 19",
            "Ростовская область : 61",
            "Рязанская область : 62",
            "Самарская область : 63",
            "Санкт-Петербург : 78",
            "Саратовская область : 64",
            "Севастополь : 92",
            "Сахалинская область : 65",
            "Свердловская область : 66",
            "Смоленская область : 67",
            "Ставропольский край : 26",
            "Тамбовская область : 68",
            "Тверская область : 69",
            "Томская область : 70",
            "Тульская область : 71",
            "Тюменская область : 72",
            "Удмуртская Республика : 18",
            "Ульяновская область : 73",
            "Хабаровский край : 27",
            "Ханты-Мансийский автономный округ — Югра : 86",
            "Челябинская область : 74",
            "Чеченская Республика : 20",
            "Чукотский автономный округ : 87",
            "Чувашская Республика : 21",
            "Ямало-Ненецкий автономный округ : 89",
            "Ярославская область : 76",
            "Алтайский край : 22",
            "Амурская область : 28",
            "Архангельская область : 29",
            "Астраханская область : 30",
            "Белгородская область : 31",
            "Брянская область : 32",
            "Карачаево-Черкесская Республика : 9"
    );

    private final List<String> minExperience = List.of(
            "от 1 года",
            "от 2 лет",
            "от 3 лет",
            "от 4 лет",
            "от 5 лет");

    private final List<String> minSalary = List.of(
            "от 25000",
            "от 50000",
            "от 100000",
            "от 150000",
            "от 200000");

    private final List<String> keyWords = List.of(
            "Backend Developer",
            "Frontend Developer",
            "Full Stack Developer",
            "Mobile Developer",
            "DevOps Engineer");

    private final List<String> notifyTimelist = List.of(
            "9:00",
            "10:00",
            "11:00",
            "12:00",
            "13:00",
            "14:00",
            "15:00",
            "16:00",
            "17:00",
            "18:00",
            "19:00",
            "20:00",
            "21:00");

    public InlineKeyboardMarkup createStartKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        var startButton = InlineKeyboardButton.builder();
        startButton.text(StartStopBotOption.START_BOT.getTitle())
                .callbackData(StartStopBotOption.START_BOT.getTitle());

        inlineKeyboard.keyboardRow(new InlineKeyboardRow(startButton.build()));
        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createStartAndStopKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        var startButton = InlineKeyboardButton.builder();
        startButton.text(StartStopBotOption.START_BOT.getTitle())
                .callbackData(StartStopBotOption.START_BOT.getTitle());

        var stopButtonBuilder = InlineKeyboardButton.builder();
        stopButtonBuilder.text(STOP_BOT.getTitle());
        stopButtonBuilder.callbackData(STOP_BOT.getTitle());

        inlineKeyboard.keyboardRow(new InlineKeyboardRow(startButton.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(stopButtonBuilder.build()));
        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createUtcOffsetsKeyboard(int page) {

        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть меньше нуля");
        }

        int PAGE_SIZE = 5;
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        int fromIndex = page * PAGE_SIZE;
        int toIndex = min(fromIndex + PAGE_SIZE, utcOffsets.size());

        log.debug("Create UTC keyboard: page={}, fromIndex={}, toIndex={}, total={}",
                page, fromIndex, toIndex, utcOffsets.size());

        if (fromIndex > utcOffsets.size()) {
            log.warn("UTC keyboard page out of range: page={}, totalPages≈{}",
                    page, (utcOffsets.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            throw new IllegalArgumentException("Номер страницы превышает размер списка");
        }

        boolean hasPrev = page > 0;
        boolean hasNext = toIndex < utcOffsets.size();

        var pageNotifyTime = utcOffsets.subList(fromIndex, toIndex);

        for (String notifyTime : pageNotifyTime) {
            var buttonBuilder = InlineKeyboardButton.builder();
            buttonBuilder.text(notifyTime);
            var digits = notifyTime.replaceAll("[^0-9:+-]", "");
            var withoutColon = digits.replace(":", "");
            buttonBuilder.callbackData(UTC_OFFSET_PREFIX + withoutColon);
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonBuilder.build()));
        }

        List<InlineKeyboardButton> buttonsInNavigateRow = new ArrayList<>();

        if (hasPrev && hasNext) {
            var buttonBuilderBack = InlineKeyboardButton.builder()
                    .text(NavigationAction.RETURN.getTitle())
                    .callbackData(UTC_OFFSET_PAGE_PREFIX + (page - 1));
            buttonsInNavigateRow.add(buttonBuilderBack.build());
            var buttonBuilderNext = InlineKeyboardButton.builder()
                    .text(NavigationAction.FURTHER.getTitle())
                    .callbackData(UTC_OFFSET_PAGE_PREFIX + (page + 1));
            buttonsInNavigateRow.add(buttonBuilderNext.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        } else if (!hasPrev) {
            var buttonBuilderNext = InlineKeyboardButton.builder()
                    .text(NavigationAction.FURTHER.getTitle())
                    .callbackData(UTC_OFFSET_PAGE_PREFIX + (page + 1));
            buttonsInNavigateRow.add(buttonBuilderNext.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        } else {
            var buttonBuilderBack = InlineKeyboardButton.builder()
                    .text(NavigationAction.RETURN.getTitle())
                    .callbackData(UTC_OFFSET_PAGE_PREFIX + (page - 1));
            buttonsInNavigateRow.add(buttonBuilderBack.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        }

        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createSettingKeyboard() {
        log.debug("Creating SETTING_KEYBOARD");

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        var utcButton = InlineKeyboardButton.builder();
        utcButton.text(UTC_OPTION.getTitle());
        utcButton.callbackData(SETTING_PREFIX + UTC_OPTION.getTitle());

        var regionButton = InlineKeyboardButton.builder();
        regionButton.text(REGION.getTitle());
        regionButton.callbackData(SETTING_PREFIX + REGION.getTitle());

        var minExperience = InlineKeyboardButton.builder();
        minExperience.text(MIN_EXPERIENCE.getTitle());
        minExperience.callbackData(SETTING_PREFIX + MIN_EXPERIENCE.getTitle());

        var minSalary = InlineKeyboardButton.builder();
        minSalary.text(MIN_SALARY.getTitle());
        minSalary.callbackData(SETTING_PREFIX + MIN_SALARY.getTitle());

        var wordForSearch = InlineKeyboardButton.builder();
        wordForSearch.text(WORD_FOR_SEARCH.getTitle());
        wordForSearch.callbackData(SETTING_PREFIX + WORD_FOR_SEARCH.getTitle());

        var settingNotify = InlineKeyboardButton.builder();
        settingNotify.text(SETTINGS_NOTIFICATION.getTitle());
        settingNotify.callbackData(SETTING_PREFIX + SETTINGS_NOTIFICATION.getTitle());

        var completeButton = InlineKeyboardButton.builder();
        completeButton.text(START_SCHEDULER.getTitle());
        completeButton.callbackData(SETTING_PREFIX + START_SCHEDULER.getTitle());

        var homeButton = InlineKeyboardButton.builder();
        homeButton.text(OUT_IN_ROUTER.getTitle());
        homeButton.callbackData(OUT_IN_ROUTER.getTitle());

        inlineKeyboard.keyboardRow(new InlineKeyboardRow(utcButton.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(regionButton.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(minExperience.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(minSalary.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(wordForSearch.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(settingNotify.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(completeButton.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(homeButton.build()));

        InlineKeyboardMarkup markup = inlineKeyboard.build();
        log.debug("SETTING_KEYBOARD created: rows={}", markup.getKeyboard().size());
        return markup;
    }

    public InlineKeyboardMarkup createRegionKeyboard(int page) {

        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть меньше нуля");
        }

        int PAGE_SIZE = 5;
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        int fromIndex = page * PAGE_SIZE;
        int toIndex = min(fromIndex + PAGE_SIZE, listRegions.size());

        log.debug("Create region keyboard: page={}, fromIndex={}, toIndex={}, total={}",
                page, fromIndex, toIndex, listRegions.size());

        if (fromIndex > listRegions.size()) {
            log.warn("Region keyboard page out of range: page={}, totalPages≈{}",
                    page, (listRegions.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            throw new IllegalArgumentException("Номер страницы превышает размер списка");
        }

        boolean hasPrev = page > 0;
        boolean hasNext = toIndex < listRegions.size();

        var pageRegions = listRegions.subList(fromIndex, toIndex);

        for (String row : pageRegions) {
            StringBuilder title = new StringBuilder();
            String[] regionTitleWithCode = row.split(":");
            var buttonBuilder = InlineKeyboardButton.builder();
            title.append(regionTitleWithCode[0].trim())
                    .append(" (")
                    .append(regionTitleWithCode[1].trim())
                    .append(")");
            buttonBuilder.text(title.toString());
            buttonBuilder.callbackData(REGION_CODE_PREFIX + regionTitleWithCode[1].trim());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonBuilder.build()));
        }

        List<InlineKeyboardButton> buttonsInNavigateRow = new ArrayList<>();

        if (hasPrev && hasNext) {
            var buttonBuilderBack = InlineKeyboardButton.builder()
                    .text(NavigationAction.RETURN.getTitle())
                    .callbackData(REGION_PAGE_PREFIX + (page - 1));
            buttonsInNavigateRow.add(buttonBuilderBack.build());
            var buttonBuilderNext = InlineKeyboardButton.builder()
                    .text(NavigationAction.FURTHER.getTitle())
                    .callbackData(REGION_PAGE_PREFIX + (page + 1));
            buttonsInNavigateRow.add(buttonBuilderNext.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        } else if (!hasPrev) {
            var buttonBuilderNext = InlineKeyboardButton.builder()
                    .text(NavigationAction.FURTHER.getTitle())
                    .callbackData(REGION_PAGE_PREFIX + (page + 1));
            buttonsInNavigateRow.add(buttonBuilderNext.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        } else {
            var buttonBuilderBack = InlineKeyboardButton.builder()
                    .text(NavigationAction.RETURN.getTitle())
                    .callbackData(REGION_PAGE_PREFIX + (page - 1));
            buttonsInNavigateRow.add(buttonBuilderBack.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        }

        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createExperienceKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        for (String exp : minExperience) {
            String[] expNum = exp.split("\\s");
            var rowButtonBuilder = InlineKeyboardButton.builder();
            rowButtonBuilder.text(exp);
            rowButtonBuilder.callbackData(EXPERIENCE_PREFIX + expNum[1]);
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(rowButtonBuilder.build()));
        }

        var rowCompleteButtonBuilder = InlineKeyboardButton.builder();
        rowCompleteButtonBuilder.text(ExperienceOption.WITHOUT_EXPERIENCE.getTitle());
        rowCompleteButtonBuilder.callbackData(EXPERIENCE_PREFIX + 0);
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(rowCompleteButtonBuilder.build()));

        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createSalaryKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        for (String salary : minSalary) {
            String[] salarySum = salary.split("\\s");
            var rowButtonBuilder = InlineKeyboardButton.builder();
            rowButtonBuilder.text(salary);
            rowButtonBuilder.callbackData(SALARY_PREFIX + salarySum[1]);
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(rowButtonBuilder.build()));
        }

        var rowCompleteButtonBuilder = InlineKeyboardButton.builder();
        rowCompleteButtonBuilder.text(SalaryOption.NOT_TAKE_SALARY.getTitle());
        rowCompleteButtonBuilder.callbackData(SALARY_PREFIX + 0);
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(rowCompleteButtonBuilder.build()));

        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createKeyWordKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        for (String keyWord : keyWords) {
            var rowButtonBuilder = InlineKeyboardButton.builder();
            rowButtonBuilder.text(keyWord);
            rowButtonBuilder.callbackData(KEY_WORD_PREFIX + keyWord);
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(rowButtonBuilder.build()));
        }

        var rowCompleteButtonBuilder = InlineKeyboardButton.builder();
        rowCompleteButtonBuilder.text(KeyWordOption.LEAVE_IT_EMPTY.getTitle());
        rowCompleteButtonBuilder.callbackData(KEY_WORD_PREFIX + KeyWordOption.LEAVE_IT_EMPTY);
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(rowCompleteButtonBuilder.build()));

        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createNotifyTimeKeyboard(int page) {

        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть меньше нуля");
        }

        int PAGE_SIZE = 5;
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        int fromIndex = page * PAGE_SIZE;
        int toIndex = min(fromIndex + PAGE_SIZE, notifyTimelist.size());

        log.debug("Create notifyTime keyboard: page={}, fromIndex={}, toIndex={}, total={}",
                page, fromIndex, toIndex, notifyTimelist.size());

        if (fromIndex > notifyTimelist.size()) {
            log.warn("NotifyTime keyboard page out of range: page={}, totalPages≈{}",
                    page, (notifyTimelist.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            throw new IllegalArgumentException("Номер страницы превышает размер списка");
        }

        boolean hasPrev = page > 0;
        boolean hasNext = toIndex < notifyTimelist.size();

        var pageNotifyTime = notifyTimelist.subList(fromIndex, toIndex);

        for (String notifyTime : pageNotifyTime) {
            var buttonBuilder = InlineKeyboardButton.builder();
            buttonBuilder.text(notifyTime);
            buttonBuilder.callbackData(NOTIFY_TIME_PREFIX + notifyTime);
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonBuilder.build()));
        }

        List<InlineKeyboardButton> buttonsInNavigateRow = new ArrayList<>();

        if (hasPrev && hasNext) {
            var buttonBuilderBack = InlineKeyboardButton.builder()
                    .text(NavigationAction.RETURN.getTitle())
                    .callbackData(NOTIFY_PAGE_PREFIX + (page - 1));
            buttonsInNavigateRow.add(buttonBuilderBack.build());
            var buttonBuilderNext = InlineKeyboardButton.builder()
                    .text(NavigationAction.FURTHER.getTitle())
                    .callbackData(NOTIFY_PAGE_PREFIX + (page + 1));
            buttonsInNavigateRow.add(buttonBuilderNext.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        } else if (!hasPrev) {
            var buttonBuilderNext = InlineKeyboardButton.builder()
                    .text(NavigationAction.FURTHER.getTitle())
                    .callbackData(NOTIFY_PAGE_PREFIX + (page + 1));
            buttonsInNavigateRow.add(buttonBuilderNext.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        } else {
            var buttonBuilderBack = InlineKeyboardButton.builder()
                    .text(NavigationAction.RETURN.getTitle())
                    .callbackData(NOTIFY_PAGE_PREFIX + (page - 1));
            buttonsInNavigateRow.add(buttonBuilderBack.build());
            inlineKeyboard.keyboardRow(new InlineKeyboardRow(buttonsInNavigateRow));
        }

        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createYesOrNoKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        var yesButtonBuilder = InlineKeyboardButton.builder();
        yesButtonBuilder.text(ReadyAction.YES.getTitle());
        yesButtonBuilder.callbackData(YES_OR_NO_PREFIX + ReadyAction.YES.getTitle());

        var noButtonBuilder = InlineKeyboardButton.builder();
        noButtonBuilder.text(ReadyAction.NO.getTitle());
        noButtonBuilder.callbackData(YES_OR_NO_PREFIX + ReadyAction.NO.getTitle());

        List<InlineKeyboardButton> rowButtons = new ArrayList<>();
        rowButtons.add(yesButtonBuilder.build());
        rowButtons.add(noButtonBuilder.build());


        inlineKeyboard.keyboardRow(new InlineKeyboardRow(rowButtons));

        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createReadyKeyboard() {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboard = InlineKeyboardMarkup.builder();

        var readyButtonBuilder = InlineKeyboardButton.builder();
        readyButtonBuilder.text(COMPLETE.getTitle());
        readyButtonBuilder.callbackData(READY_PREFIX + COMPLETE.getTitle());

        var returnButtonBuilder = InlineKeyboardButton.builder();
        returnButtonBuilder.text(GO_BACK.getTitle());
        returnButtonBuilder.callbackData(READY_PREFIX + GO_BACK.getTitle());

        inlineKeyboard.keyboardRow(new InlineKeyboardRow(readyButtonBuilder.build()));
        inlineKeyboard.keyboardRow(new InlineKeyboardRow(returnButtonBuilder.build()));

        return inlineKeyboard.build();
    }

    public InlineKeyboardMarkup createBotStopKeyboard() {
        var stopButtonBuilder = InlineKeyboardButton.builder();
        stopButtonBuilder.text(STOP_BOT.getTitle());
        stopButtonBuilder.callbackData(STOP_BOT.getTitle());

        var homeButtonBuilder = InlineKeyboardButton.builder();
        homeButtonBuilder.text(OUT_IN_ROUTER.getTitle());
        homeButtonBuilder.callbackData(OUT_IN_ROUTER.getTitle());


        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(stopButtonBuilder.build()))
                .keyboardRow(new InlineKeyboardRow(homeButtonBuilder.build()))
                .build();
    }
}
