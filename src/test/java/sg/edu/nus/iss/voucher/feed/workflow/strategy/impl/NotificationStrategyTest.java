package sg.edu.nus.iss.voucher.feed.workflow.strategy.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
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
    	Feed feed = new Feed();
        feed.setCampaign("Mid-Autumn Sale");
        feed.setTargetUserName("John");
        feed.setTargetUserEmail("john@example.com");
        feed.setStore("SuperMart");
        when(webSocketHandler.broadcastToTargetedUsers(feed)).thenReturn(true);

        boolean result = notificationStrategy.sendNotification(feed);

        assertTrue(result);
        verify(webSocketHandler, times(1)).broadcastToTargetedUsers(feed);
    }

}
