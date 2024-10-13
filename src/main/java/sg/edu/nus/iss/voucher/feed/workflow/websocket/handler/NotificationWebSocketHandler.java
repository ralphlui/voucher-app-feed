package sg.edu.nus.iss.voucher.feed.workflow.websocket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;

public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	try {
    	 String query = session.getUri().getQuery();
         Map<String, String> queryParams = getQueryParams(query);
         String userId = queryParams.get("userId");
         if (userId != null) {
             activeSessions.put(userId, session);
             logger.info("Stored user Id and registered session for userId: " + userId + " with session ID: " + session.getId());
         } else {
             logger.warn("Received message without userId in the header: " + query);
             session.close(CloseStatus.BAD_DATA);
         }
         
        logger.info("Connection established with session ID: " + session.getId());
    	 } catch (Exception e) {
             logger.error("Error processing message: " + e.getMessage(), e);
             session.close(CloseStatus.SERVER_ERROR);
         }
    }
    
    private Map<String, String> getQueryParams(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String disconnectedUserId = null;

        
        for (Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet()) {
            if (entry.getValue().equals(session)) {
            	disconnectedUserId = entry.getKey();
                break;
            }
        }

        if (disconnectedUserId != null) {
            activeSessions.remove(disconnectedUserId);
            logger.info("Connection closed for userId: " + disconnectedUserId + " with session ID: " + session.getId());
        } else {
            logger.warn("Session ID: " + session.getId() + " not found in activeSessions.");
        }
    }

    
    
    public boolean broadcastToTargetedUsers(FeedDTO feedDTO) {
        boolean messageSent = false;
        String userId = feedDTO.getUserId();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonMsg = mapper.writeValueAsString(feedDTO);
            
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
