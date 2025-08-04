package com.veggieshop.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailService, "fromAddress", "test@veggieshop.com");
        // Optionally, set the fromAddress directly if you want to test @Value logic:
        // ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@veggieshop.com");
    }

    @Test
    void sendPasswordReset_ShouldSendProperEmail() {
        String to = "user@example.com";
        String name = "John Doe";
        String resetLink = "https://reset.link/token-123";

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendPasswordReset(to, name, resetLink);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();

        assertThat(sent.getTo()).containsExactly(to);
        assertThat(sent.getSubject()).containsIgnoringCase("Password Reset");
        assertThat(sent.getText())
                .contains(name)
                .contains(resetLink)
                .contains("ignore this email");
        assertThat(sent.getFrom()).isNotEmpty();
    }

    @Test
    void sendPasswordReset_ShouldWorkWithEmptyName() {
        String to = "test@x.com";
        String name = "";
        String resetLink = "https://reset/reset";

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendPasswordReset(to, name, resetLink);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();

        assertThat(sent.getTo()).containsExactly(to);
        assertThat(sent.getText()).contains(resetLink);
    }

    @Test
    void sendPasswordReset_ShouldThrowIfMailSenderFails() {
        String to = "fail@mail.com";
        String name = "Fail";
        String resetLink = "link";

        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendPasswordReset(to, name, resetLink))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Mail error");
    }
}
