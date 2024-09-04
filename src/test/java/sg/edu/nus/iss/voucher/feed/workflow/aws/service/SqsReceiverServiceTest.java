package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.FeedEventPayload;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.EmailStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.NotificationStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.utility.EncryptionUtils;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class SqsReceiverServiceTest {

    @Mock
    private AmazonSQS amazonSQS;

    @Mock
    private JSONReader jsonReader;

    @Mock
    private FeedDAO feedDAO;

    @Mock
    private EncryptionUtils encryptionUtils;

    @Mock
    private EmailStrategy emailStrategy;

    @Mock
    private NotificationStrategy notificationStrategy;

    @InjectMocks
    private SqsReceiverService sqsReceiverService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testProcessMessage() {
       
        String messageBody = "{\n"
                + "    \"preference\":\"test\",\n"
                + "    \"campaign\":{\n"
                + "     \"description\":\"Aroma from Nature II\"\n"
                + "    },\n"
                + "    \"store\":{\n"
                + "       \"name\":\"MUJI\" \n"
                + "    }\n"
                + "}";
        Message mockMessage = new Message().withBody(messageBody);
        FeedEventPayload feedMessage = new FeedEventPayload(); 
        feedMessage.setPreference("test");
        feedMessage.setCampaign("Aroma from Nature II");
        feedMessage.setStore("MUJI");
        when(jsonReader.readFeedMessage(messageBody)).thenReturn(feedMessage);

        HashMap<String, String> targetUsers = new HashMap<>();
        targetUsers.put("user@example.com", "User");
        when(sqsReceiverService.getTargetUsers(anyString())).thenReturn(targetUsers);
        
        Feed mockFeed = new Feed();
        mockFeed.setFeedId("123");
        mockFeed.setTargetUserEmail("user@example.com");
        
        when(feedDAO.saveFeed(any(Feed.class))).thenReturn(mockFeed);
        when(notificationStrategy.sendNotification(any(Feed.class))).thenReturn(true);
        when(emailStrategy.sendNotification(any(Feed.class))).thenReturn(true);

        boolean result = sqsReceiverService.processMessage(mockMessage);

        assertTrue(result);
        verify(feedDAO, times(1)).saveFeed(any(Feed.class));
        verify(amazonSQS, never()).deleteMessage(any(DeleteMessageRequest.class)); 
    }



    @Test
    void testDeleteMessage() {
      
        Message mockMessage = new Message();
        mockMessage.setReceiptHandle("receiptHandle");

        sqsReceiverService.deleteMessage(mockMessage);

        verify(amazonSQS, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }



    @Test
    void testGetTargetUsers() {
       
        HashMap<String, String> expectedTargetUsers = new HashMap<>();
        expectedTargetUsers.put("user1@example.com", "User One");
        expectedTargetUsers.put("user2@example.com", "User Two");

        when(jsonReader.getUsersByPreferences("preference1")).thenReturn(expectedTargetUsers);

        HashMap<String, String> actualTargetUsers = sqsReceiverService.getTargetUsers("preference1");

        assertEquals(expectedTargetUsers, actualTargetUsers);
    }
}


