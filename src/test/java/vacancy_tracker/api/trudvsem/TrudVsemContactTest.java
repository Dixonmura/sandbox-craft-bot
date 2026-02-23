package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrudVsemContactTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Корректный email контакт")
    void shouldDeserializeValidEmailContact() throws JsonProcessingException {
        String json = """
                {"contact_type": "email", "contact_value": "hr@yandex.ru"}""";
        TrudVsemContact contact = mapper.readValue(json, TrudVsemContact.class);
        assertThat(contact.contactType()).isEqualTo("email");
        assertThat(contact.contactValue()).isEqualTo("hr@yandex.ru");
    }

    @Test
    @DisplayName("Корректный телефон контакт")
    void shouldDeserializeValidPhoneContact() throws JsonProcessingException {
        String json = """
                {"contact_type": "phone", "contact_value": "+74951234567"}""";
        TrudVsemContact contact = mapper.readValue(json, TrudVsemContact.class);
        assertThat(contact.contactType()).isEqualTo("phone");
        assertThat(contact.contactValue()).isEqualTo("+74951234567");
    }

    @Test
    @DisplayName("Пустые значения")
    void shouldDeserializeEmptyValues() throws JsonProcessingException {
        String json = """
                {"contact_type": "", "contact_value": ""}""";
        TrudVsemContact contact = mapper.readValue(json, TrudVsemContact.class);
        assertThat(contact.contactType()).isEmpty();
        assertThat(contact.contactValue()).isEmpty();
    }

    @Test
    @DisplayName("Null значения")
    void shouldDeserializeNullValues() throws JsonProcessingException {
        String json = """
                {"contact_type": null, "contact_value": null}""";
        TrudVsemContact contact = mapper.readValue(json, TrudVsemContact.class);
        assertThat(contact.contactType()).isNull();
        assertThat(contact.contactValue()).isNull();
    }

    @Test
    @DisplayName("Некорректный JSON")
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = "{contact_type: email}";
        assertThrows(JsonProcessingException.class,
                () -> mapper.readValue(invalidJson, TrudVsemContact.class));
    }
}