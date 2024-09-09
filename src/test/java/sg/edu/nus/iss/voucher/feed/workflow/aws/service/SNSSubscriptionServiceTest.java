package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import sg.edu.nus.iss.voucher.feed.workflow.entity.TargetUser;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.*;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;


public class SNSSubscriptionServiceTest {

    @InjectMocks
    private SNSSubscriptionService snsSubscriptionService;

    @Mock
    private JSONReader jsonReader;

    @Mock
    private FeedDAO feedDAO;

    @Mock
    private EmailStrategy emailStrategy;

    @Mock
    private NotificationStrategy notificationStrategy;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConfirmSubscription() {
        JSONObject json = new JSONObject();
        json.put("SubscribeURL", "http://example.com/confirm");

        when(restTemplate.getForObject(anyString(), any())).thenReturn("Success");

        snsSubscriptionService.confirmSubscription(json.toJSONString());
        
    }

    @Test
    public void testProcessNotification() throws Exception {
        String message = "{\"Message\": \"Test message\"}";
        MessagePayload feedMsg = new MessagePayload();
        feedMsg.setCategory("Food");
        feedMsg.setCampaignId("1");
        feedMsg.setCampaignDescription("Campaign Description");
        feedMsg.setStoreId("StoreId");
        feedMsg.setStoreName("StoreName");

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
        when(feedDAO.saveFeed(any())).thenReturn(feed);
        when(notificationStrategy.sendNotification(any())).thenReturn(true);
        when(emailStrategy.sendNotification(any())).thenReturn(true);

        String result = snsSubscriptionService.processNotification(message);

        assertEquals("Processed user:\n11:true\n", result);
    }


}
