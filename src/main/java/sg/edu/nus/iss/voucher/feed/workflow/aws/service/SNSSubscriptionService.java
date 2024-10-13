package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.EmailStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.NotificationStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.utility.*;

@Service
public class SNSSubscriptionService {

	@Autowired
	private JSONReader jsonReader;

	@Autowired
	private FeedDAO feedDAO;

	@Autowired
	private EmailStrategy emailStrategy;

	@Autowired
	private NotificationStrategy notificationStrategy;

	private static final Logger logger = LoggerFactory.getLogger(SNSSubscriptionService.class);


	public String processNotification(String snsMessage) {
		String retMsg = "";
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(snsMessage);
			String message = jsonNode.get("Message").asText();
			logger.info("Received message: {}", message);

			if (message == null || message.isEmpty()) {
				logger.info("Message is null or empty.");
				return retMsg = "Bad Request:Message is null or empty.";
			}

			MessagePayload feedMsg = jsonReader.readFeedMessage(message);
			if (feedMsg == null) {
				logger.error("Failed to parse the feed message. The message payload is null or invalid.");
				return retMsg = "Bad Request:Failed to parse the feed message. The message payload is null or invalid.";
			}

			String category = GeneralUtility.makeNotNull(feedMsg.getCategory());
			if (category.isEmpty()) {
				logger.info("Category is empty.");
				return retMsg = "Bad Request:Category is empty.";
			}

			ArrayList<TargetUser> targetUsers = getTargetUsers(category);
			if (targetUsers.isEmpty()) {
				logger.warn("No users found for the category: {}", category);
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
					FeedDTO feedDTO=  DTOMapper.toFeedDTO(savedFeed);
					
					return sendNotifications(feedDTO);
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

	private boolean sendNotifications(FeedDTO feedDTO) {
		boolean isSendNotification = notificationStrategy.sendNotification(feedDTO);
		logger.info("isSendLiveFeed: {}", isSendNotification);
		boolean isSendEmail = emailStrategy.sendNotification(feedDTO);
		logger.info("isSendEmail: {}", isSendEmail);

		return isSendEmail || isSendNotification;
	}

}
