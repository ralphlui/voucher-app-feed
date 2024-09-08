package sg.edu.nus.iss.voucher.feed.workflow.websocket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Connection established with session ID: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();

            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            
            Map<String, Object> dataMap = (Map<String, Object>) messageMap.get("data");

            if (dataMap != null) {
               
                session.getAttributes().put("userData", dataMap);

                String userId = (String) dataMap.get("userID");

                if (userId != null) {
                    activeSessions.put(userId, session);
                    logger.info("Stored user data,role and preferences, and registered session for userId: " + userId + " with session ID: " + session.getId());
                } else {
                    logger.warn("Received message without userId in the data field: " + payload);
                    session.close(CloseStatus.BAD_DATA);
                }
            } else {
                logger.warn("Received message without data field: " + payload);
                session.close(CloseStatus.BAD_DATA);
            }
        } catch (Exception e) {
            logger.error("Error processing message: " + e.getMessage(), e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }



    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String disconnectedEmail = null;

        
        for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                disconnectedEmail = entry.getKey();
                break;
            }
        }

        if (disconnectedEmail != null) {
            activeSessions.remove(disconnectedEmail);
            logger.info("Connection closed for email: " + disconnectedEmail + " with session ID: " + session.getId());
        } else {
            logger.warn("Session ID: " + session.getId() + " not found in activeSessions.");
        }
    }

    
    
    public boolean broadcastToTargetedUsers(Feed feed) {
        boolean messageSent = false;
        String userId = feed.getUserId();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonMsg = mapper.writeValueAsString(feed);
            
            // Check if the target user id is in activeSessions map
            WebSocketSession session = activeSessions.get(userId);
            
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMsg));
                messageSent = true;
                logger.info("Live Feed sent to session for User Id: " + userId + 
                            " with session ID: " + session.getId());
            } else {
                logger.info("No active session found for User Id: " + userId);
            }
            
        } catch (Exception e) {
            logger.error("Error occurred while broadcasting to targeted users: ", e);
        }

        return messageSent;
    }

}
