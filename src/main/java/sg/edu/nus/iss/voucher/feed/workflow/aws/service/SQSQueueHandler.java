package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;

import sg.edu.nus.iss.voucher.feed.workflow.utility.GeneralUtility;

@Profile("!test")
@Service
public class SQSQueueHandler {

	@Value("${aws.sqs.queue.feed.name}")
	String queueName;

	@Value("${aws.sns.feed.topic.arn}")
	String topicArn;

	@Autowired
	private AmazonSQS amazonSQS;

	@Autowired
	private AmazonSNS amazonSNS;

	@Autowired
	private SNSSubscriptionService snsSubscriptionService;

	private static final Logger logger = LoggerFactory.getLogger(SQSQueueHandler.class);

	@EventListener(ApplicationReadyEvent.class)
	public void createQueueIfNotExists() {
		String queueUrl = "";
		try {

			GetQueueUrlResult queueUrlResult = amazonSQS.getQueueUrl(queueName);
			queueUrl = queueUrlResult.getQueueUrl();
			logger.info("Feed Queue already exists: " + queueUrl);

		} catch (QueueDoesNotExistException e) {

			CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
			queueUrl = amazonSQS.createQueue(createQueueRequest).getQueueUrl();
			logger.info("Queue created: " + queueUrl);
		}

		if (!GeneralUtility.makeNotNull(queueUrl).equals("")) {

			GetQueueAttributesRequest queueAttributesRequest = new GetQueueAttributesRequest(queueUrl)
					.withAttributeNames("QueueArn");

			String queueArn = amazonSQS.getQueueAttributes(queueAttributesRequest).getAttributes().get("QueueArn");
			logger.info("queueArn:" + queueArn);

			SubscribeRequest subscribeRequest = new SubscribeRequest(topicArn, "sqs", queueArn);
			amazonSNS.subscribe(subscribeRequest);
		}
	}

	@Scheduled(fixedDelay = 5000)
	public void consumeMessages() {

		try {
			if (doesQueueExist(queueName)) {
				String queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();

				ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
						.withMaxNumberOfMessages(10).withWaitTimeSeconds(5);

				List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();

				for (Message message : messages) {
					
					logger.info("Received Message: " + message.getBody());

					processFeedData(message.getBody());

					amazonSQS.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));

					logger.info("Message deleted successfully.");

				}
			}
		} catch (Exception e) {
			logger.info("Error consuming messages: " + e.getMessage());
		}
	}

	public boolean doesQueueExist(String queueName) {
        // Get all queues in the account
        List<String> queueUrls = amazonSQS.listQueues(new ListQueuesRequest()).getQueueUrls();

        // Check if any of the URLs end with the desired queue name
        return queueUrls.stream().anyMatch(url -> url.endsWith("/" + queueName));
    }

	public void processFeedData(String snsMessage) {

		logger.info("Processing feed data: " + snsMessage);

		String retMsg = snsSubscriptionService.processNotification(snsMessage);

		logger.info("processNotification: " + retMsg);
	}
}
