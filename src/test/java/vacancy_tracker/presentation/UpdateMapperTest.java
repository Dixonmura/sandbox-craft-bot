package vacancy_tracker.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateMapperTest {

    Update update;
    Chat chat;
    Message message;
    UpdateMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UpdateMapper();
        chat = new Chat(15L, "");
        String textMessage = "Тест mapper";
        message = new Message();
        message.setChat(chat);
        message.setText(textMessage);
        update = new Update();
        update.setMessage(message);
    }

    @Test
    @DisplayName("Проверка возвращения корректного экземпляра IncomingUpdateDto")
    void getUpdateDto_shouldReturnIncomingUpdateDto_whenDataIsValid() {
        IncomingUpdateDto updateDto = mapper.getUpdateDto(update);
        assertThat(updateDto).extracting(IncomingUpdateDto::userId, IncomingUpdateDto::text)
                .containsExactly(chat.getId(), message.getText());
    }

    @Test
    @DisplayName("Проверка возвращения корректного экземпляра IncomingUpdateDto, когда текст отсутствует")
    void getUpdateDto_shouldReturnIncomingUpdateDto_whenUpdateWithoutText() {
        Message messageWithoutText = new Message();
        messageWithoutText.setChat(chat);
        update.setMessage(messageWithoutText);
        IncomingUpdateDto updateDto = mapper.getUpdateDto(update);
        assertThat(updateDto).extracting(IncomingUpdateDto::userId, IncomingUpdateDto::text)
                .containsExactly(chat.getId(), null);
    }

    @Test
    @DisplayName("Проверка выброса исключения, когда update null")
    void constructor_shouldThrowsIllegalArgumentException_whenUpdateIsNull() {
        assertThatThrownBy(() ->
                new UpdateMapper().getUpdateDto(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("update не может быть null");
    }
}