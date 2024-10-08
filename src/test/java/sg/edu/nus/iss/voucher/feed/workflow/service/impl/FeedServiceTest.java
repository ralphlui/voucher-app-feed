package sg.edu.nus.iss.voucher.feed.workflow.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.utility.EncryptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class FeedServiceTest {

    @Mock
    private FeedDAO feedDao;

    @Mock
    private EncryptionUtils encryptionUtils;

    @InjectMocks
    private FeedService feedService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(feedService, "encryptionUtils", encryptionUtils);
    }

    @Test
    void testGetFeedsByEmailWithPagination() throws Exception {
      
        String targetedUserEmail = "john@example.com";
        String encodedEmail = "encodedEmail";
        List<Feed> feeds = new ArrayList<>();
        Feed feed = new Feed();
        feed.setTargetUserEmail(encodedEmail);
        feeds.add(feed);
        
        when(encryptionUtils.encrypt(targetedUserEmail)).thenReturn(encodedEmail);
        when(encryptionUtils.decrypt(encodedEmail)).thenReturn(targetedUserEmail);
        when(feedDao.getAllFeedByEmail(anyString(), anyInt(), anyInt())).thenReturn(feeds);

        Map<Long, List<FeedDTO>> result = feedService.getFeedsByEmailWithPagination(targetedUserEmail, 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.containsKey(1L));
        assertEquals(1, result.get(1L).size());
        verify(feedDao, times(1)).getAllFeedByEmail(anyString(), anyInt(), anyInt());
    }

    @Test
    void testFindByFeedId() throws Exception {
      
        String feedId = "feed123";
        String decryptedEmail = "john@example.com";
        Feed feed = new Feed();
        feed.setFeedId(feedId);
        feed.setTargetUserEmail("encryptedEmail");

        when(encryptionUtils.decrypt(anyString())).thenReturn(decryptedEmail);
        when(feedDao.findById(feedId)).thenReturn(feed);

        FeedDTO result = feedService.findByFeedId(feedId);

        assertNotNull(result);
        assertEquals(feedId, result.getFeedId());
        verify(feedDao, times(1)).findById(feedId);
    }

   
    @Test
    void testUpdateReadStatusById() {
       
        String feedId = "feed123";
        Feed updatedFeed = new Feed();
        updatedFeed.setFeedId(feedId);
        when(feedDao.upateReadStatus(feedId)).thenReturn(true);
        when(feedDao.findById(feedId)).thenReturn(updatedFeed);

        FeedDTO result = feedService.updateReadStatusById(feedId);

        assertNotNull(result);
        assertEquals(feedId, result.getFeedId());
        verify(feedDao, times(1)).upateReadStatus(feedId);
        verify(feedDao, times(1)).findById(feedId);
    }

   
}

