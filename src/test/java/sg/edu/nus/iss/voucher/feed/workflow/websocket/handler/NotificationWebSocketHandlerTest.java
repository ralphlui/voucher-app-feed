package sg.edu.nus.iss.voucher.feed.workflow.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationWebSocketHandlerTest {

    @InjectMocks
    private NotificationWebSocketHandler handler;

    @MockBean
    private WebSocketSession session;

	private FeedDTO feedDTO;

	@BeforeEach
	public void setUp() {
		
		session = Mockito.mock(WebSocketSession.class);
		feedDTO = new FeedDTO();
		feedDTO.setUserId("11111");
	}

	@Test
	public void testAfterConnectionEstablished () throws Exception {

		URI mockUri = new URI("ws://localhost:8080/ws/liveFeeds?userId=11111");
		when(session.getUri()).thenReturn(mockUri);
		when(session.getId()).thenReturn("1234");

		handler.afterConnectionEstablished(session);

		Map<String, WebSocketSession> activeSessions = handler.activeSessions;
		assertTrue(activeSessions.containsKey("11111"));
		assertEquals(session, activeSessions.get("11111"));
	}

	@Test
	public void testAfterConnectionClosed() throws Exception {

		URI mockUri = new URI("ws://localhost:8080/ws/liveFeeds?userId=11111");
		when(session.getUri()).thenReturn(mockUri);
		handler.activeSessions.put("11111", session);

		handler.afterConnectionClosed(session, CloseStatus.NORMAL);

		assertFalse(handler.activeSessions.containsKey("11111"));
	}

	@Test
	public void testBroadcastToTargetedUsers() throws Exception {

		when(session.isOpen()).thenReturn(true);
		handler.activeSessions.put("11111", session);

		ObjectMapper mapper = new ObjectMapper();
		String jsonMessage = mapper.writeValueAsString(feedDTO);

		boolean messageSent = handler.broadcastToTargetedUsers(feedDTO);

		verify(session, times(1)).sendMessage(new TextMessage(jsonMessage));
		assertTrue(messageSent);
	}

	@Test
	public void testBroadcastToTargetedUsers_NoActiveSession() throws IOException {

		boolean messageSent = handler.broadcastToTargetedUsers(feedDTO);

		verify(session, never()).sendMessage(any(TextMessage.class));
		assertFalse(messageSent);
	}

	@Test
	public void testBroadcastToTargetedUsers_SessionClosed() throws IOException {

		when(session.isOpen()).thenReturn(false);
		handler.activeSessions.put("11111", session);

		boolean messageSent = handler.broadcastToTargetedUsers(feedDTO);

		verify(session, never()).sendMessage(any(TextMessage.class));
		assertFalse(messageSent);
	}
	
}
