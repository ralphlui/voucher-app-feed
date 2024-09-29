package sg.edu.nus.iss.voucher.feed.workflow.rabbitmq.handler;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.LiveFeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import sg.edu.nus.iss.voucher.feed.workflow.entity.TargetUser;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.EmailStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.NotificationStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.utility.DTOMapper;
import sg.edu.nus.iss.voucher.feed.workflow.utility.GeneralUtility;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;


@Service
public class NotificationSubscriberService {
	
	@Autowired
	private JSONReader jsonReader;

	@Autowired
	private FeedDAO feedDAO;

	@Autowired
	private EmailStrategy emailStrategy;

	@Autowired
	private NotificationStrategy notificationStrategy;

	private static final Logger logger = LoggerFactory.getLogger(NotificationSubscriberService.class);

	@RabbitListener(queues = "${rabbitmq.queue}")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
      
		try {
			
			this.processNotification(message);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred in RabbitListener: "+e.toString());
		}
    }
	

	public String processNotification(String snsMessage) {
		String retMsg = "";
		try {
			
			MessagePayload feedMsg = jsonReader.readFeedMessage(snsMessage);
			if (feedMsg == null) {
				logger.error("Failed to parse the feed message. The message payload is null or invalid.");
				return retMsg = "Bad Request:Failed to parse the feed message. The message payload is null or invalid.";
			}

			String category = GeneralUtility.makeNotNull(feedMsg.getCategory());
			if (category.isEmpty()) {
				logger.error("Category is empty.");
				return retMsg = "Bad Request:Category is empty.";
			}

			ArrayList<TargetUser> targetUsers = getTargetUsers(category);
			if (targetUsers.isEmpty()) {
				logger.error("No users found for the category: {}", category);
				return retMsg = "Bad Request:No users found for the category: " + category;
			}

			logger.info("Processing target users: {}", targetUsers);
			retMsg = "Processed user:";
			for (TargetUser targetUser : targetUsers) {
				boolean processed = processTargetUser(targetUser, feedMsg);
				logger.info("Processed user: {} with result: {}", targetUser.getUserId(), processed);
				retMsg += targetUser.getUserId() + ":" + processed ;

			}

		} catch (Exception ex) {
			logger.error("Bad Request:Process Message Exception: {}", ex.toString(), ex);
		}
		return retMsg;
	}

	ArrayList<TargetUser> getTargetUsers(String category) {

		ArrayList<TargetUser> targetUsers = jsonReader.getUsersByPreferences(category);
		return targetUsers;
	}

	public boolean processTargetUser(TargetUser targetUser, MessagePayload feedMsg) {

		try {
			String userId = GeneralUtility.makeNotNull(targetUser.getUserId()).trim();
			logger.info("userId: {}", userId);

			if (userId.isEmpty()) {
				return false;
			}

			// Check checkFeedExistsByUserAndCampaign
			boolean isExists = feedDAO.checkFeedExistsByUserAndCampaign(targetUser.getUserId(),
					feedMsg.getCampaignId());
			logger.info("checkFeedExistsByUserAndCampaign: {}", isExists);
			//
			if (!isExists) {
				Feed feed = createFeed(feedMsg, targetUser);

				Feed savedFeed = feedDAO.saveFeed(feed);
				if (savedFeed.getFeedId().isEmpty()) {
					return false;
				}else {
					LiveFeedDTO liveFeedDTO=  DTOMapper.toLiveFeedDTO(savedFeed);
					liveFeedDTO.setCampaignDescription(feed.getCampaignDescription());
					liveFeedDTO.setStoreName(feed.getStoreName());
					return sendNotifications(liveFeedDTO);
				}

				
			} else {
				return true;
			}

		} catch (Exception ex) {
			logger.error("Process Message Exception: {}", ex.toString(), ex);
		}
		return false;
	}

	private Feed createFeed(MessagePayload feedMsg, TargetUser targetUser) throws Exception {

		Feed feed = new Feed();
		feed.setUserId(targetUser.getUserId());
		feed.setEmail(targetUser.getEmail());
		feed.setUserName(targetUser.getUsername());
		feed.setCategory(GeneralUtility.makeNotNull(feedMsg.getCategory()));
		feed.setCampaignId(GeneralUtility.makeNotNull(feedMsg.getCampaignId()));
		feed.setCampaignDescription(GeneralUtility.makeNotNull(feedMsg.getCampaignDescription()));
		feed.setStoreId(GeneralUtility.makeNotNull(feedMsg.getStoreId()));
		feed.setStoreName(GeneralUtility.makeNotNull(feedMsg.getStoreName()));
		return feed;
	}

	private boolean sendNotifications(LiveFeedDTO liveFeedDTO) {
		boolean isSendNotification = notificationStrategy.sendNotification(liveFeedDTO);
		logger.info("isSendLiveFeed: {}", isSendNotification);
		boolean isSendEmail = emailStrategy.sendNotification(liveFeedDTO);
		logger.info("isSendEmail: {}", isSendEmail);

		return isSendEmail || isSendNotification;
	}
}

