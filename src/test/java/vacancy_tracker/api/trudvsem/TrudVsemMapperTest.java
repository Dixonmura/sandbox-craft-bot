package vacancy_tracker.api.trudvsem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.core.Vacancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrudVsemMapperTest {

    private TrudVsemMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TrudVsemMapper();
    }

    private TrudVsemResponse.VacancyDto createFullDto() {
        TrudVsemResponse.VacancyDto dto = new TrudVsemResponse.VacancyDto();
        dto.setId("12345");
        dto.setJobName("Java Developer");

        TrudVsemResponse.Company company = new TrudVsemResponse.Company();
        company.setName("ООО Тест");
        dto.setCompany(company);

        dto.setSalaryMin(100000);
        dto.setSalaryMax(150000);

        TrudVsemResponse.Region region = new TrudVsemResponse.Region();
        region.setRegionCode("7700000000000");
        region.setName("Москва");
        dto.setRegion(region);

        TrudVsemResponse.Requirement requirement = new TrudVsemResponse.Requirement();
        requirement.setExperience(3);
        dto.setRequirement(requirement);

        dto.setVacancyUrl("https://test.ru/12345");

        return dto;
    }

    @Test
    @DisplayName("Маппинг полного DTO")
    void mapToDomain_shouldMapAllFields_whenDtoIsFull() {
        TrudVsemResponse.VacancyDto dto = createFullDto();

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.externalId()).isEqualTo("12345");
        assertThat(vacancy.title()).isEqualTo("Java Developer");
        assertThat(vacancy.company()).isEqualTo("ООО Тест");
        assertThat(vacancy.salaryFrom()).isEqualTo(100000);
        assertThat(vacancy.salaryTo()).isEqualTo(150000);
        assertThat(vacancy.regionCode()).isEqualTo(77);
        assertThat(vacancy.experience()).isEqualTo(3);
        assertThat(vacancy.url()).isEqualTo("https://test.ru/12345");
    }

    @Test
    @DisplayName("Маппинг с null полями подставляет значения по умолчанию")
    void mapToDomain_shouldUseDefaultValues_whenFieldsAreNull() {
        TrudVsemResponse.VacancyDto dto = new TrudVsemResponse.VacancyDto();
        dto.setId(null);
        dto.setJobName(null);
        dto.setCompany(null);
        dto.setSalaryMin(null);
        dto.setSalaryMax(null);
        dto.setRegion(null);
        dto.setRequirement(null);
        dto.setVacancyUrl(null);

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.externalId()).isEqualTo("id");
        assertThat(vacancy.title()).isEqualTo("Наименование вакансии отсутствует");
        assertThat(vacancy.company()).isEqualTo("Компания не указана");
        assertThat(vacancy.salaryFrom()).isEqualTo(0);
        assertThat(vacancy.salaryTo()).isEqualTo(0);
        assertThat(vacancy.regionCode()).isEqualTo(0);
        assertThat(vacancy.experience()).isEqualTo(0);
        assertThat(vacancy.url()).isEqualTo("Ссылка на вакансию отсутствует");
    }

    @Test
    @DisplayName("Маппинг с пустыми строками")
    void mapToDomain_shouldUseDefaultValues_whenStringsAreBlank() {
        TrudVsemResponse.VacancyDto dto = new TrudVsemResponse.VacancyDto();
        dto.setId("   ");
        dto.setJobName("");

        TrudVsemResponse.Company company = new TrudVsemResponse.Company();
        company.setName("   ");
        dto.setCompany(company);

        dto.setVacancyUrl("");

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.externalId()).isEqualTo("id");
        assertThat(vacancy.title()).isEqualTo("Наименование вакансии отсутствует");
        assertThat(vacancy.company()).isEqualTo("Компания не указана");
        assertThat(vacancy.url()).isEqualTo("Ссылка на вакансию отсутствует");
    }

    @Test
    @DisplayName("Парсинг кода региона из полной строки")
    void parseRegion_shouldExtractFirstTwoDigits() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.getRegion().setRegionCode("7800000000000");

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.regionCode()).isEqualTo(78);
    }

    @Test
    @DisplayName("Парсинг кода региона из короткой строки")
    void parseRegion_shouldHandleShortCode() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.getRegion().setRegionCode("77");

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.regionCode()).isEqualTo(77);
    }

    @Test
    @DisplayName("Парсинг кода региона из строки с лидирующими нулями")
    void parseRegion_shouldHandleLeadingZeros() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.getRegion().setRegionCode("0500000000000");

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.regionCode()).isEqualTo(5);
    }

    @Test
    @DisplayName("Парсинг кода региона возвращает 0 при ошибке")
    void parseRegion_shouldReturnZero_whenParsingFails() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.getRegion().setRegionCode("invalid");

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.regionCode()).isEqualTo(0);
    }

    @Test
    @DisplayName("Опыт работы возвращает 0 при null")
    void experience_shouldReturnZero_whenRequirementIsNull() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.setRequirement(null);

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.experience()).isEqualTo(0);
    }

    @Test
    @DisplayName("Опыт работы возвращает 0 при null значении")
    void experience_shouldReturnZero_whenExperienceIsNull() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.getRequirement().setExperience(null);

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.experience()).isEqualTo(0);
    }

    @Test
    @DisplayName("Название компании возвращает значение по умолчанию при null объекте")
    void companyName_shouldReturnDefault_whenCompanyIsNull() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.setCompany(null);

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.company()).isEqualTo("Компания не указана");
    }

    @Test
    @DisplayName("Название компании возвращает значение по умолчанию при null имени")
    void companyName_shouldReturnDefault_whenNameIsNull() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.getCompany().setName(null);

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.company()).isEqualTo("Компания не указана");
    }

    @Test
    @DisplayName("Зарплата возвращает 0 при null значениях")
    void salary_shouldReturnZero_whenValuesAreNull() {
        TrudVsemResponse.VacancyDto dto = createFullDto();
        dto.setSalaryMin(null);
        dto.setSalaryMax(null);

        Vacancy vacancy = mapper.mapToDomain(dto);

        assertThat(vacancy.salaryFrom()).isEqualTo(0);
        assertThat(vacancy.salaryTo()).isEqualTo(0);
    }

    @Test
    @DisplayName("Бросает исключение при null DTO")
    void mapToDomain_shouldThrowException_whenDtoIsNull() {
        assertThatThrownBy(() -> mapper.mapToDomain(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VacancyDto не может быть null");
    }
}