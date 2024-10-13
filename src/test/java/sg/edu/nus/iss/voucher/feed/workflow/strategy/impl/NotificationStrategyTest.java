package sg.edu.nus.iss.voucher.feed.workflow.strategy.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.websocket.handler.NotificationWebSocketHandler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class NotificationStrategyTest {

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @InjectMocks
    private NotificationStrategy notificationStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendNotification() {
    	FeedDTO feed = new FeedDTO();
    	feed.setCampaignId("123");
        feed.setCampaignDescription("Mid-Autumn Sale");
        feed.setUserId("111");
        feed.setUserName("Eleven");
        feed.setEmail("eleven.11@gmail.com");
        feed.setStoreName("SuperMart");
        when(webSocketHandler.broadcastToTargetedUsers(feed)).thenReturn(true);

        boolean result = notificationStrategy.sendNotification(feed);

        assertTrue(result);
        verify(webSocketHandler, times(1)).broadcastToTargetedUsers(feed);
    }

}
