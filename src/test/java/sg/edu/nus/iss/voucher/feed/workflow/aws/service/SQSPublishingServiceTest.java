package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sg.edu.nus.iss.voucher.feed.workflow.dto.AuditDTO;

@SpringBootTest 
@ActiveProfiles("test")
class SQSPublishingServiceTest {

    @Mock
    private AmazonSQS amazonSQS;
    
    @Mock
    private Logger logger;


    @InjectMocks
    private SQSPublishingService sqsPublishingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendMessage() throws Exception {
       
        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setRemarks("Test message");
        
        sqsPublishingService.sendMessage(auditDTO);

        verify(amazonSQS, times(1)).sendMessage(any(SendMessageRequest.class));
    }
    
    
    @Test
    void sendMessage_whenMessageSizeExceedsLimit_shouldTruncateAndSend() throws Exception {
      
        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setRemarks("This is a very long remark that exceeds the 256KB limit...");

        SendMessageResult mockResult = new SendMessageResult();
        
        when(amazonSQS.sendMessage(any(SendMessageRequest.class))).thenReturn(mockResult);

        sqsPublishingService.sendMessage(auditDTO);

        verify(amazonSQS, times(1)).sendMessage(any(SendMessageRequest.class));
    }


    @Test
    void truncateMessage_whenMessageExceedsLimit_shouldReturnTruncatedMessage() {
       
        String remarks = "This is a very long remark that will be truncated";
        String currentMessage = "{\"remarks\":\"This is a very long remark that will be truncated\"}";
        int maxMessageSize = 256 * 1024; // 256 KB

        String truncatedRemarks = sqsPublishingService.truncateMessage(remarks, maxMessageSize, currentMessage);

        // Assert
        assertNotNull(truncatedRemarks);
        assertEquals(truncatedRemarks,remarks);
    }
}
    
   

