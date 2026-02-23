package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для контактов работодателя из API TrudVsem.
 *
 * <p>Представляет один канал связи (email, телефон, сайт) с указанием типа и значения.</p>
 */
public record TrudVsemContact(
        @JsonProperty("contact_type")
        String contactType,
        @JsonProperty("contact_value")
        String contactValue
) {}
