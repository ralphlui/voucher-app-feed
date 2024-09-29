package sg.edu.nus.iss.voucher.feed.workflow.rabbitmq.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.LiveFeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import sg.edu.nus.iss.voucher.feed.workflow.entity.TargetUser;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.EmailStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.NotificationStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationSubscriberServiceTest {

    @Mock
    private JSONReader jsonReader;

    @Mock
    private FeedDAO feedDAO;

    @Mock
    private EmailStrategy emailStrategy;

    @Mock
    private NotificationStrategy notificationStrategy;

    @InjectMocks
    private NotificationSubscriberService notificationSubscriberService;

    private MessagePayload feedMsg;
    private TargetUser targetUser;
    private LiveFeedDTO liveFeedDTO;
    
    
    private Feed feed ;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        feedMsg = new MessagePayload();
        feedMsg.setCampaignId("123");
        feedMsg.setCategory("Clothes");
        feedMsg.setCampaignDescription("Campaign Test");
        feedMsg.setStoreId("1");
        feedMsg.setStoreName("MUJI");

        targetUser = new TargetUser();
        targetUser.setUserId("user123");
        targetUser.setEmail("test@example.com");
        targetUser.setUsername("TestUser");

        liveFeedDTO = new LiveFeedDTO();
        liveFeedDTO.setCampaignDescription("Campaign Test");
        liveFeedDTO.setStoreName("MUJI");
        
        
        feed = new Feed();
        feed.setFeedId("11");
        feed.setCampaignId("123");
        feed.setCategory("Clothes");
        feed.setCampaignDescription("Campaign Test");
        feed.setStoreId("1");
        feed.setStoreName("MUJI");
    }

    
    
    @Test
    public void testReceiveMessage() {
        String message = "{\"campaignId\": \"123\", \"category\": \"Clothes\"}";
        notificationSubscriberService.receiveMessage(message);

        verify(jsonReader).readFeedMessage(message);
    }

    @Test
    public void testProcessNotification() {
        when(jsonReader.readFeedMessage(anyString())).thenReturn(feedMsg);
        when(jsonReader.getUsersByPreferences(anyString())).thenReturn(new ArrayList<>(List.of(targetUser)));
        when(feedDAO.checkFeedExistsByUserAndCampaign(anyString(), anyString())).thenReturn(false);
        when(feedDAO.saveFeed(any(Feed.class))).thenReturn(feed);

        when(notificationStrategy.sendNotification(any(LiveFeedDTO.class))).thenReturn(true);
        when(emailStrategy.sendNotification(any(LiveFeedDTO.class))).thenReturn(true);

        String response = notificationSubscriberService.processNotification("{\"campaignId\": \"123\", \"category\": \"Clothes\"}");

        assertEquals("Processed user:user123:true", response);

        verify(feedDAO).saveFeed(any(Feed.class));
        verify(notificationStrategy).sendNotification(any(LiveFeedDTO.class));
        verify(emailStrategy).sendNotification(any(LiveFeedDTO.class));
    }

    @Test
    public void testProcessTargetUser_Exists() {
        when(feedDAO.checkFeedExistsByUserAndCampaign(anyString(), anyString())).thenReturn(true);

        boolean result = notificationSubscriberService.processTargetUser(targetUser, feedMsg);
        assertTrue(result);

        verify(feedDAO).checkFeedExistsByUserAndCampaign(targetUser.getUserId(), feedMsg.getCampaignId());
        verify(feedDAO, never()).saveFeed(any());
        verify(notificationStrategy, never()).sendNotification(any());
        verify(emailStrategy, never()).sendNotification(any());
    }
}

