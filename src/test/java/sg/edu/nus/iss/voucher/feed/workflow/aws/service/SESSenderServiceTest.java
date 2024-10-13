package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

@SpringBootTest
@ActiveProfiles("test")
public class SESSenderServiceTest {

	@Mock
	private AmazonSimpleEmailService sesClient;

	@InjectMocks
	private SESSenderService sesEmailService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSendEmail() throws Exception {

		String from = "test@gmail.com";
		Collection<String> recipientsTo = Arrays.asList("user1@gmail.com", "user2@gmail.com");
		String subject = "Greeting";
		String body = "Hello";

		SendEmailResult mockResult = new SendEmailResult();
		when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResult);

		boolean result = sesEmailService.sendEmail(from, recipientsTo, subject, body);

		assertTrue(result);

		verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
	}

}
