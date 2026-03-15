package vacancy_tracker.bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import vacancy_tracker.core.UserService;
import vacancy_tracker.core.UserSettingState;
import vacancy_tracker.core.VacancySessionState;
import vacancy_tracker.presentation.UpdateMapper;
import vacancy_tracker.presentation.UserCommandParser;
import vacancy_tracker.presentation.VacancyCommandDispatcher;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;
import vacancy_tracker.presentation.dto.UserCommandDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static vacancy_tracker.bot.VacancyMessages.START_MESSAGE;
import static markups.VacancyKeyboardKey.START_KEYBOARD;

@ExtendWith(MockitoExtension.class)
class VacancyBotTest {

    private static final Long CHAT_ID = 33L;

    @Mock
    UserService userService;
    @Mock
    UpdateMapper updateMapper;
    @Mock
    UserCommandParser commandParser;
    @Mock
    VacancyCommandDispatcher dispatcher;

    @InjectMocks
    VacancyBot vacancyBot;

    @Test
    @DisplayName("startVacancyBot должен вернуть стартовое сообщение")
    void startVacancyBot_shouldSetConfiguringState_andReturnStartReply() {
        Update update = createUpdateWithText(CHAT_ID, "любая команда");

        VacancyReply reply = vacancyBot.startVacancyBot(update);

        assertThat(reply.userId()).isEqualTo(CHAT_ID);
        assertThat(reply.text()).isEqualTo(START_MESSAGE);
        assertThat(reply.keyboardKey()).isEqualTo(START_KEYBOARD);
    }

    @Test
    @DisplayName("handleAnswer должен маппить Update в команды и делегировать их диспетчеру")
    void handleAnswer_shouldMapUpdateAndDelegateToDispatcher() {
        Update update = createUpdateWithText(CHAT_ID, "ответ");
        IncomingUpdateDto incomingDto = new IncomingUpdateDto(CHAT_ID, "ответ");
        UserCommandDto commandDto = new UserCommandDto(CHAT_ID, CommandType.START, "ответ");
        VacancyReply expectedReply = new VacancyReply(CHAT_ID, "Текст", START_KEYBOARD);

        when(updateMapper.getUpdateDto(update)).thenReturn(incomingDto);
        when(userService.getSettingState(CHAT_ID)).thenReturn(UserSettingState.CLEAN);
        when(commandParser.parse(incomingDto, UserSettingState.CLEAN)).thenReturn(commandDto);
        when(dispatcher.commandDispatch(commandDto)).thenReturn(expectedReply);

        VacancyReply actual = vacancyBot.handleAnswer(update);

        assertThat(actual).isSameAs(expectedReply);
        verify(updateMapper).getUpdateDto(update);
        verify(userService).getSettingState(CHAT_ID);
        verify(commandParser).parse(incomingDto, UserSettingState.CLEAN);
        verify(dispatcher).commandDispatch(commandDto);
    }

    @Test
    @DisplayName("isConfiguring возвращает true только при состоянии CONFIGURING")
    void isConfiguring_shouldReturnTrueOnlyWhenConfiguring() {
        when(userService.getStateSessionOrDefault(CHAT_ID))
                .thenReturn(VacancySessionState.CONFIGURING);

        assertThat(vacancyBot.isConfiguring(CHAT_ID)).isTrue();
    }

    @Test
    @DisplayName("isActive возвращает true только при состоянии ACTIVE")
    void isActive_shouldReturnTrueOnlyWhenActive() {
        when(userService.getStateSessionOrDefault(CHAT_ID))
                .thenReturn(VacancySessionState.ACTIVE);

        assertThat(vacancyBot.isActive(CHAT_ID)).isTrue();
    }

    @Test
    @DisplayName("isInactive возвращает true только при состоянии INACTIVE")
    void isInactive_shouldReturnTrueOnlyWhenInactive() {
        when(userService.getStateSessionOrDefault(CHAT_ID))
                .thenReturn(VacancySessionState.INACTIVE);

        assertThat(vacancyBot.isInactive(CHAT_ID)).isTrue();
    }

    private Update createUpdateWithText(Long chatId, String text) {
        Chat chat = new Chat(chatId, "");
        chat.setId(chatId);

        Message message = new Message();
        message.setChat(chat);
        message.setText(text);

        Update update = new Update();
        update.setMessage(message);
        return update;
    }
}
