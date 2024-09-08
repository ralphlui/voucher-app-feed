package sg.edu.nus.iss.voucher.feed.workflow.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SNSSubscriptionService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SNSControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private SNSSubscriptionService snsReceiverService;

    @InjectMocks
    private SNSController snsController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(snsController).build();
    }

    @Test
    public void testReceiveNotification_Confirmation() throws Exception {
        JSONObject json = new JSONObject();
        json.put("Type", "SubscriptionConfirmation");
        json.put("Message", "Test message");
        json.put("MessageId", "12345");

        String message = json.toJSONString();
        
        doNothing().when(snsReceiverService).confirmSubscription(message);

        mockMvc.perform(post("/api/feeds/sns/notification")
                .content(message)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription confirmed"));
    }

    @Test
    public void testReceiveNotification_Notification() throws Exception {
        JSONObject json = new JSONObject();
        json.put("Type", "Notification");
        json.put("Message", "Test message");
        json.put("MessageId", "12345");

        String message = json.toJSONString();
        when(snsReceiverService.processNotification(message)).thenReturn("Success");

        mockMvc.perform(post("/api/feeds/sns/notification")
                .content(message)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification processed successfully\nSuccess"));
    }

    @Test
    public void testReceiveNotification_InvalidType() throws Exception {
        JSONObject json = new JSONObject();
        json.put("Type", "InvalidType");
        json.put("Message", "Test message");
        json.put("MessageId", "12345");

        String message = json.toJSONString();

        mockMvc.perform(post("/api/feeds/sns/notification")
                .content(message)
                .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid SNS notification type"));
    }

}

