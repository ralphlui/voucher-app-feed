package sg.edu.nus.iss.voucher.feed.workflow.strategy.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SESSenderService;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class EmailStrategyTest {

    @Mock
    private SESSenderService sesSenderService;

    @InjectMocks
    private EmailStrategy emailStrategy;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailStrategy, "emailFrom", "test@example.com");
        ReflectionTestUtils.setField(emailStrategy, "frontendURL", "http://test-frontend-url.com");
    }

    @Test
    void testSendNotification() throws Exception {
       
    	FeedDTO feed = new FeedDTO();
        feed.setCampaignId("123");
        feed.setCampaignDescription("Mid-Autumn Sale");
        feed.setUserId("111");
        feed.setUserName("Eleven");
        feed.setEmail("eleven.11@gmail.com");
        feed.setStoreName("SuperMart");

        when(sesSenderService.sendEmail(anyString(), anyList(), anyString(), anyString())).thenReturn(true);

        boolean result = emailStrategy.sendNotification(feed);

        assertTrue(result);
        
    }

}

