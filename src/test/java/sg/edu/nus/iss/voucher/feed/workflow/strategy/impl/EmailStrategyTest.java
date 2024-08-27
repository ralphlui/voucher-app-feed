package sg.edu.nus.iss.voucher.feed.workflow.strategy.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SesSenderService;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class EmailStrategyTest {

    @Mock
    private SesSenderService sesSenderService;

    @InjectMocks
    private EmailStrategy emailStrategy;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailStrategy, "emailFrom", "test@example.com");
        ReflectionTestUtils.setField(emailStrategy, "frontendURL", "http://test-frontend-url.com");
    }

    @Test
    void testSendNotification() throws Exception {
       
        Feed feed = new Feed();
        feed.setCampaign("Mid-Autumn");
        feed.setTargetUserName("John");
        feed.setTargetUserEmail("john@example.com");
        feed.setStore("SuperMart");

        when(sesSenderService.sendEmail(anyString(), anyList(), anyString(), anyString())).thenReturn(true);

        boolean result = emailStrategy.sendNotification(feed);

        assertTrue(result);
        
    }

}

