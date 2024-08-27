package sg.edu.nus.iss.voucher.feed.workflow.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.LiveFeed;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationWebSocketHandlerTest {

    @InjectMocks
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Mock
    private WebSocketSession webSocketSession;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAfterConnectionEstablished() throws Exception {
        when(webSocketSession.getId()).thenReturn("session123");

        notificationWebSocketHandler.afterConnectionEstablished(webSocketSession);

    }

    @Test
    public void testHandleTextMessagePayload() throws Exception {
        when(webSocketSession.getId()).thenReturn("session123");
        String jsonPayload = "{\"email\":\"test@example.com\"}";

        notificationWebSocketHandler.handleTextMessage(webSocketSession, new TextMessage(jsonPayload));

        assertEquals(webSocketSession, notificationWebSocketHandler.activeSessions.get("test@example.com"));
    }


    @Test
    public void testAfterConnectionClosed() throws Exception {
        String email = "test@example.com";
        when(webSocketSession.getId()).thenReturn("session123");

        notificationWebSocketHandler.activeSessions.put(email, webSocketSession);

        notificationWebSocketHandler.afterConnectionClosed(webSocketSession, CloseStatus.NORMAL);

        assertNull(notificationWebSocketHandler.activeSessions.get(email));
    }

    @Test
    public void testBroadcastToTargetedUsers() throws Exception {
        Feed feed = new Feed();
        feed.setFeedId("feed123");
        feed.setCampaign("Campaign");
        feed.setStore("Store");
        feed.setTargetUserEmail("test@example.com");

        LiveFeed liveFeed = new LiveFeed();
        liveFeed.setFeedId(feed.getFeedId());
        liveFeed.setMessage(feed.getCampaign() + " campaign at " + feed.getStore());

        ObjectMapper mapper = new ObjectMapper();
        String jsonMsg = mapper.writeValueAsString(liveFeed);

        when(webSocketSession.isOpen()).thenReturn(true);
        notificationWebSocketHandler.activeSessions.put("test@example.com", webSocketSession);

        boolean result = notificationWebSocketHandler.broadcastToTargetedUsers(feed);

        assertTrue(result);
        verify(webSocketSession).sendMessage(new TextMessage(jsonMsg));
    }

    @Test
    public void testBroadcastToTargetedUsersNoActiveSession() throws Exception {
        Feed feed = new Feed();
        feed.setFeedId("feed123");
        feed.setCampaign("Campaign");
        feed.setStore("Store");
        feed.setTargetUserEmail("nonexistent@example.com");

        boolean result = notificationWebSocketHandler.broadcastToTargetedUsers(feed);

        assertFalse(result);
        verify(webSocketSession, never()).sendMessage(any(TextMessage.class));
    }
}
