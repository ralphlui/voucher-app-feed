package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import static org.mockito.Mockito.*;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;


@SpringBootTest 
@ActiveProfiles("test")
public class SQSQueueHandlerTest {

    @Mock
    private AmazonSQS amazonSQS;

    @Mock
    private AmazonSNS amazonSNS;

    @Mock
    private SNSSubscriptionService snsSubscriptionService;
    
    @Value("${aws.sqs.queue.feed.name}")
    private String queueName;

    @Value("${aws.sns.feed.topic.arn}")
    private String topicArn;
    
    @Value("${aws.sqs.feed.arn.prefix}")
	String sqsArnPrefix;

    @InjectMocks
    private SQSQueueHandler sqsQueueHandler;

    private final String queueUrl = "http://localhost:4566/000000000000/testQueue";
    private final String queueArn = "arn:aws:sqs:ap-southeast-1:000000000000:testQueue";
    private final String messageBody = "{\"Message\":\"Test message\"}";

    @BeforeEach
    public void setUp() { 
         sqsQueueHandler.queueName = queueName;
         sqsQueueHandler.topicArn = topicArn;
         sqsQueueHandler.sqsArnPrefix = sqsArnPrefix;
    }


    @Test
    public void testCreateQueue() {
      
        when(amazonSQS.getQueueUrl(queueName)).thenThrow(new QueueDoesNotExistException("Queue does not exist"));
 
        CreateQueueResult createQueueResult = new CreateQueueResult().withQueueUrl(queueUrl);
        when(amazonSQS.createQueue(any(CreateQueueRequest.class))).thenReturn(createQueueResult);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("QueueArn", queueArn);
        GetQueueAttributesResult queueAttributesResult = new GetQueueAttributesResult().withAttributes(attributes);
        when(amazonSQS.getQueueAttributes(any(GetQueueAttributesRequest.class))).thenReturn(queueAttributesResult);
 
        sqsQueueHandler.createQueueIfNotExists();
 
        verify(amazonSQS).createQueue(any(CreateQueueRequest.class));
        verify(amazonSNS).subscribe(any(SubscribeRequest.class));
      
    }

    @Test
    public void testProcessFeedData() {
        when(snsSubscriptionService.processNotification(messageBody)).thenReturn("Processed");

        sqsQueueHandler.processFeedData(messageBody);

        verify(snsSubscriptionService).processNotification(messageBody);
       
    }
}
