package vacancy_tracker.api.trudvsem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vacancy_tracker.core.Vacancy;

import java.util.function.Supplier;

/**
 * Маппер для преобразования DTO вакансии из API TrudVsem в доменную модель {@link Vacancy}.
 * <p>
 * Обрабатывает возможные null-значения и отсутствующие поля,
 * подставляя значения по умолчанию и логируя предупреждения.
 */
public class TrudVsemMapper {

    private static final Logger log = LogManager.getLogger(TrudVsemMapper.class);

    /**
     * Преобразует DTO вакансии в доменную модель.
     *
     * @param vacancyDto DTO из API TrudVsem
     * @return доменная модель вакансии
     * @throws IllegalArgumentException если vacancyDto равен null
     */
    public Vacancy mapToDomain(TrudVsemResponse.VacancyDto vacancyDto) {

        if (vacancyDto == null) {
            log.error("VacancyDto is null");
            throw new IllegalArgumentException("VacancyDto не может быть null");
        }

        String id = safeGet(vacancyDto::getId, "id");
        String vacancyName = safeGet(vacancyDto::getJobName, "Наименование вакансии отсутствует");
        String companyName = safeGetCompanyName(vacancyDto.getCompany());
        Integer salaryMin = safeGetInt(vacancyDto::getSalaryMin, 0);
        Integer salaryMax = safeGetInt(vacancyDto::getSalaryMax, 0);
        Integer regionCode = safeGetRegionCode(vacancyDto.getRegion());
        Integer experience = safeGetExperience(vacancyDto.getRequirement());
        String vacancyUrl = safeGet(vacancyDto::getVacancyUrl, "Ссылка на вакансию отсутствует");

        return new Vacancy(
                id,
                vacancyName,
                companyName,
                salaryMin,
                salaryMax,
                regionCode,
                experience,
                vacancyUrl
        );
    }

    /**
     * Извлекает код региона из строки вида "7700000000000" (первые 2 цифры).
     *
     * @param regionCode строка с кодом региона
     * @return число (первые 2 цифры) или 0 при ошибке
     */
    private Integer parseRegion(String regionCode) {
        try {
            String code = regionCode.substring(0, 2);
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            log.warn("Отсутствует код региона. regionCode={}", regionCode);
            return 0;
        }
    }

    /**
     * Безопасно извлекает название компании.
     *
     * @param company объект компании или null
     * @return название компании или значение по умолчанию
     */
    private String safeGetCompanyName(TrudVsemResponse.Company company) {
        if (company == null) {
            log.warn("Company object is null");
            return "Компания не указана";
        }
        String name = company.getName();
        return name != null && !name.isBlank() ? name : "Компания не указана";
    }

    /**
     * Безопасно извлекает требуемый опыт работы.
     *
     * @param requirement объект требований или null
     * @return количество лет опыта или 0
     */
    private Integer safeGetExperience(TrudVsemResponse.Requirement requirement) {
        if (requirement == null) {
            log.warn("Requirement object is null");
            return 0;
        }
        Integer exp = requirement.getExperience();
        return exp != null ? exp : 0;
    }

    /**
     * Безопасно извлекает и парсит код региона.
     *
     * @param region объект региона или null
     * @return код региона или 0
     */
    private Integer safeGetRegionCode(TrudVsemResponse.Region region) {
        if (region == null) {
            log.warn("Region object is null");
            return 0;
        }
        return parseRegion(region.getRegionCode());
    }

    /**
     * Безопасно получает строковое значение из Supplier.
     *
     * @param getter       поставщик значения
     * @param defaultValue значение по умолчанию
     * @return полученное значение или defaultValue
     */
    private String safeGet(Supplier<String> getter, String defaultValue) {
        try {
            String value = getter.get();
            return value != null && !value.isBlank() ? value : defaultValue;
        } catch (NullPointerException e) {
            log.warn("Field access failed: {}", e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Безопасно получает целочисленное значение из Supplier.
     *
     * @param getter       поставщик значения
     * @param defaultValue значение по умолчанию
     * @return полученное значение или defaultValue
     */
    private Integer safeGetInt(Supplier<Integer> getter, Integer defaultValue) {
        try {
            Integer value = getter.get();
            return value != null ? value : defaultValue;
        } catch (NullPointerException e) {
            log.warn("Int field access failed: {}", e.getMessage());
            return defaultValue;
        }
    }
}