package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.FeedEventPayload;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.EmailStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.NotificationStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.utility.EncryptionUtils;
import sg.edu.nus.iss.voucher.feed.workflow.utility.GeneralUtility;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class SqsReceiverService {

	@Autowired
	private AmazonSQS amazonSQS;

	@Value("${aws.sqs.queue.feed.url}") String feedQueueURL;
	

	@Autowired
	private JSONReader jsonReader;

	@Autowired
	private FeedDAO feedDAO;
	
	@Autowired
	private EncryptionUtils encryptionUtils;


	@Autowired
	private EmailStrategy emailStrategy;

	@Autowired
	private NotificationStrategy notificationStrategy;
	

	private static final Logger logger = LoggerFactory.getLogger(SqsReceiverService.class);

	@Scheduled(fixedRate = 500) // Poll every 5 seconds
	public void pollQueue() {
		try {
			
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withQueueUrl(feedQueueURL)
					.withMaxNumberOfMessages(10).withWaitTimeSeconds(20);
			

			List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
			for (Message message : messages) {
				
				boolean success = processMessage(message);
				if (success) {
					deleteMessage(message);
				}
			}
		} catch (Exception ex) {
			logger.error("poll Queue Exception: {}", ex.toString(), ex);
		}
	}
	
	boolean processMessage(Message message) {
	    try {
	        String messageBody = message.getBody();
	        logger.info("Received message: {}", messageBody);

	        if (messageBody == null || messageBody.isEmpty()) {
	            logger.info("Message body is null or empty.");
	            return false;
	        }

	        FeedEventPayload feedMsg = jsonReader.readFeedMessage(messageBody);
	        if (feedMsg == null) {
	            logger.info("FeedMessage is null.");
	            return false;
	        }

	        String category = GeneralUtility.makeNotNull(feedMsg.getPreference());
	        if (category.isEmpty()) {
	            logger.info("Category is empty.");
	            return false;
	        }

	        HashMap<String, String> targetUsers = getTargetUsers(category);
	        if (targetUsers.isEmpty()) {
	            logger.info("Target users are empty.");
	            return false;
	        }

	        logger.info("Processing target users: {}", targetUsers);

	        for (Map.Entry<String, String> entry : targetUsers.entrySet()) {
	            boolean processed = processTargetUser(entry, feedMsg, category);
	            logger.info("Processed user: {} with result: {}", entry.getKey(), processed);

	            if (processed) {
	                return true;
	            }
	        }

	    } catch (Exception ex) {
	        logger.error("Process Message Exception: {}", ex.toString(), ex);
	    }
	    return false;
	}


     HashMap<String, String> getTargetUsers(String category) {
    	 
		HashMap<String, String> targetUsers = jsonReader.getUsersByPreferences(category);
	   
	    return targetUsers;
	}

	private boolean processTargetUser(Map.Entry<String, String> entry, FeedEventPayload feedMsg, String category) {

		try {
			String email = GeneralUtility.makeNotNull(entry.getKey()).trim();
			logger.info("email: {}", email);

			if (email.isEmpty()) {
				return false;
			}

			String encodedTargetedUserEmail = encryptionUtils.encrypt(email);
			logger.info("encodedTargetedUserEmail: {}", encodedTargetedUserEmail);

			Feed feed = createFeed(feedMsg, category, entry.getValue(), encodedTargetedUserEmail);

			Feed savedFeed = feedDAO.saveFeed(feed);
			if (savedFeed.getFeedId().isEmpty()) {
				return false;
			}

			savedFeed.setTargetUserEmail(encryptionUtils.decrypt(savedFeed.getTargetUserEmail()));

			return sendNotifications(savedFeed);
		} catch (Exception ex) {
			logger.error("Process Message Exception: {}", ex.toString(), ex);
		}
		return false;
	}

	private Feed createFeed(FeedEventPayload feedMsg, String category, String userName, String encodedEmail) {
	    Feed feed = new Feed();
	    feed.setTargetUserEmail(encodedEmail);
	    feed.setTargetUserName(userName);
	    feed.setCampaign(GeneralUtility.makeNotNull(feedMsg.getCampaign()));
	    feed.setCategory(category);
	    feed.setStore(GeneralUtility.makeNotNull(feedMsg.getStore()));
	    return feed;
	}

	private boolean sendNotifications(Feed savedFeed) {
	    boolean isSendNotification = notificationStrategy.sendNotification(savedFeed);
	    logger.info("isSendLiveFeed: {}", isSendNotification);
	    boolean isSendEmail = emailStrategy.sendNotification(savedFeed);
	    logger.info("isSendEmail: {}", isSendEmail);
	    
	    return isSendEmail || isSendNotification;
	}


	void deleteMessage(Message message) {
		try {
			DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest().withQueueUrl(feedQueueURL)
					.withReceiptHandle(message.getReceiptHandle());

			amazonSQS.deleteMessage(deleteMessageRequest);
		} catch (Exception ex) {
			logger.error("Delete Message Exception: {}", ex.toString(), ex);
		}
	}
}
