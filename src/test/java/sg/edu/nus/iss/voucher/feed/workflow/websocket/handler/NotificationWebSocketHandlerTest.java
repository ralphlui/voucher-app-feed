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
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;

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
        String jsonPayload = "{\n"
        		+ "    \"data\": {\n"
        		+ "        \"userID\": \"111\",\n"
        		+ "        \"email\": \"eleven.11@gmail.com\",\n"
        		+ "        \"username\": \"Eleven11\",\n"
        		+ "        \"role\": \"CUSTOMER\",\n"
        		+ "        \"preferences\": [\n"
        		+ "            \"food\",\n"
        		+ "            \"household\",\n"
        		+ "            \"clothing\"\n"
        		+ "        ],\n"
        		+ "        \"active\": true,\n"
        		+ "        \"verified\": true\n"
        		+ "    }\n"
        		+ "}\n"
        		+ "";

        notificationWebSocketHandler.handleTextMessage(webSocketSession, new TextMessage(jsonPayload));

        assertEquals(webSocketSession, notificationWebSocketHandler.activeSessions.get("111"));
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
        feed.setCampaignId("123");
        feed.setCampaignDescription("Mid-Autumn Sale");
        feed.setUserId("111");
        feed.setUserName("John");
        feed.setEmail("test@example.com");
        feed.setStoreName("SuperMart");

        ObjectMapper mapper = new ObjectMapper();
        String jsonMsg = mapper.writeValueAsString(feed);

        when(webSocketSession.isOpen()).thenReturn(true);
        notificationWebSocketHandler.activeSessions.put("111", webSocketSession);

        boolean result = notificationWebSocketHandler.broadcastToTargetedUsers(feed);

        assertTrue(result);
        verify(webSocketSession).sendMessage(new TextMessage(jsonMsg));
    }

    @Test
    public void testBroadcastToTargetedUsersNoActiveSession() throws Exception {
        Feed feed = new Feed();
        feed.setFeedId("feed123");
        feed.setEmail("nonexistent@example.com");
        feed.setCampaignId("123");
        feed.setCampaignDescription("Mid-Autumn Sale");
        feed.setStoreName("SuperMart");

        boolean result = notificationWebSocketHandler.broadcastToTargetedUsers(feed);

        assertFalse(result);
        verify(webSocketSession, never()).sendMessage(any(TextMessage.class));
    }
}
