package sg.edu.nus.iss.voucher.feed.workflow.strategy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.IFeedStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.websocket.handler.NotificationWebSocketHandler;

@Service
public class NotificationStrategy implements IFeedStrategy {

	private static final Logger logger = LoggerFactory.getLogger(NotificationStrategy.class);

	private final NotificationWebSocketHandler webSocketHandler;

	@Autowired
	public NotificationStrategy(NotificationWebSocketHandler webSocket) {
		this.webSocketHandler = webSocket;

	}

	@Override
	public boolean sendNotification(FeedDTO feedDTO) {
		boolean messageSent = false;
		try {
			 messageSent = webSocketHandler.broadcastToTargetedUsers(feedDTO);
		} catch (Exception e) {
			logger.error("Error occurred while sendLiveNotification {} ...", e.toString());
			e.printStackTrace();
			return false;
		}

		return messageSent;

	}
}
