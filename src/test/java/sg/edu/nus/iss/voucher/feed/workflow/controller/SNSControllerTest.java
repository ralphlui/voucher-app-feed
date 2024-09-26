package sg.edu.nus.iss.voucher.feed.workflow.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SNSSubscriptionService;
import sg.edu.nus.iss.voucher.feed.workflow.dto.AuditDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.HTTPVerb;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.AuditService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SNSControllerTest {
	
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SNSSubscriptionService snsReceiverService;

	@MockBean
	private AuditService auditService;

	@InjectMocks
	private SNSController snsController;

	static String userId = "user123";

	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;
	
	private static AuditDTO auditDTO ;

	@BeforeEach
	void setUp() {
		auditDTO = new AuditDTO();
		auditDTO.setUserId(userId);
		auditDTO.setActivityType(activityTypePrefix+"Feed Notification");
		auditDTO.setRequestType(HTTPVerb.POST);
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testReceiveNotification() throws Exception {
		String jsonMessage = "{\n"
				+ "    \"Type\": \"Notification\",\n"
				+ "    \"MessageId\": \"example-message-id\",\n"
				+ "    \"TopicArn\": \"arn:aws:sns:region:account-id:topic-name\",\n"
				+ "    \"Message\": \"{\\\"category\\\": \\\"Food\\\", \\\"campaign\\\": {\\\"campaignId\\\": \\\"123\\\", \\\"description\\\": \\\"Happy Hour\\\"}, \\\"store\\\": {\\\"storeId\\\": \\\"456\\\", \\\"name\\\": \\\"MUJI\\\"}}\",\n"
				+ "    \"Timestamp\": \"2024-09-08T12:34:56.789Z\",\n"
				+ "    \"SignatureVersion\": \"1\",\n"
				+ "    \"Signature\": \"example-signature\",\n"
				+ "    \"SigningCertURL\": \"https://sns-region.amazonaws.com/SimpleNotificationService-1234567890.pem\",\n"
				+ "    \"UnsubscribeURL\": \"https://sns-region.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:region:account-id:topic-name:subscription-id\",\n"
				+ "    \"MessageAttributes\": {\n"
				+ "        \"X-User-Id\": {\n"
				+ "            \"Type\": \"String\",\n"
				+ "            \"Value\": \"user123\"\n"
				+ "        }\n"
				+ "    }\n"
				+ "}";

		when(snsReceiverService.processNotification(anyString(), eq(userId))).thenReturn("Processed successfully");
		 
		when(auditService.createAuditDTO(userId, "Feed Notification", activityTypePrefix,
				"/api/feeds/sns/notification/", HTTPVerb.POST)).thenReturn(auditDTO);

		mockMvc.perform(
				post("/api/feeds/sns/notification").contentType(MediaType.APPLICATION_JSON).content(jsonMessage))
				.andExpect(status().isOk())
				.andExpect(content().string("Notification processed successfully\nProcessed successfully"));
	}

}
