package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import sg.edu.nus.iss.voucher.feed.workflow.entity.TargetUser;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.EmailStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.NotificationStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest 
@ActiveProfiles("test")
public class SNSSubscriptionServiceTest {

    @Mock
    private JSONReader jsonReader;

    @Mock
    private FeedDAO feedDAO;

    @Mock
    private EmailStrategy emailStrategy;

    @Mock
    private NotificationStrategy notificationStrategy;

    @InjectMocks
    private SNSSubscriptionService snsSubscriptionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessNotification() throws Exception {
     
        String snsMessage = "{\n"
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
        		+ "            \"Value\": \"41e0c9f2-81a4-4b44-b084-90fc0c4cd35b\"\n"
        		+ "        }\n"
        		+ "    }\n"
        		+ "}";
        MessagePayload feedMsg = new MessagePayload();
        feedMsg.setCategory("news");
        feedMsg.setCampaignId("123");
        feedMsg.setCampaignDescription("Test Campaign");
        feedMsg.setStoreId("456");
        feedMsg.setStoreName("Test Store");

        ArrayList<TargetUser> users = new ArrayList<>();
        TargetUser user = new TargetUser();
        user.setUserId("11");
		user.setEmail("eleven.11@gmail.com");
		user.setUsername("User 11");
		users.add(user);
        
        Feed feed = new Feed();
		feed.setCategory(feedMsg.getCategory());
		feed.setUserId(user.getUserId());
		feed.setUserName(user.getUsername());
		feed.setEmail(user.getEmail());
		feed.setCampaignId(feedMsg.getCampaignId());
		feed.setCampaignDescription(feedMsg.getCampaignDescription());
		feed.setStoreId(feedMsg.getStoreId());
		feed.setStoreName(feedMsg.getStoreName());

      

        when(jsonReader.readFeedMessage(anyString())).thenReturn(feedMsg);
        when(jsonReader.getUsersByPreferences(anyString())).thenReturn(users);
        when(feedDAO.checkFeedExistsByUserAndCampaign(anyString(), anyString())).thenReturn(true);
        when(feedDAO.saveFeed(any(Feed.class))).thenReturn(feed); 
        when(notificationStrategy.sendNotification(any(FeedDTO.class))).thenReturn(true);
        when(emailStrategy.sendNotification(any(FeedDTO.class))).thenReturn(true);

     
        String result = snsSubscriptionService.processNotification(snsMessage);

      
        assertTrue(result.contains("Processed user:"));
        
    }

}

