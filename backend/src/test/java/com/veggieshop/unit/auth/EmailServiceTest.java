package com.veggieshop.unit.auth;

import com.veggieshop.auth.EmailServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        // Inject the fromAddress via reflection (for @Value)
        TestUtils.setField(emailService, "fromAddress", "no-reply@veggieshop.com");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void sendPasswordReset_shouldSendCorrectEmail() {
        // Arrange
        String toEmail = "testuser@example.com";
        String name = "Test User";
        String resetLink = "https://test.com/reset/abcdef";

        // Capture the sent message
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendPasswordReset(toEmail, name, resetLink);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();

        assertThat(sent.getTo()).containsExactly(toEmail);
        assertThat(sent.getFrom()).isEqualTo("no-reply@veggieshop.com");
        assertThat(sent.getSubject()).isEqualTo("Password Reset Request");
        assertThat(sent.getText())
                .contains(name)
                .contains(resetLink)
                .contains("VeggieShop")
                .contains("reset your VeggieShop password");
    }

    // ========== TestUtils helper ==========

    static class TestUtils {
        static void setField(Object obj, String fieldName, Object value) {
            try {
                java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(obj, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
