package sg.edu.nus.iss.voucher.feed.workflow.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FeedServiceTest {

    @Mock
    private FeedDAO feedDao;

    @InjectMocks
    private FeedService feedService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetFeedsByUserWithPagination_ValidUser() {
        String userId = "test@example.com";
        int page = 1;
        int size = 10;

        List<Feed> mockFeeds = new ArrayList<>();
        Feed feed = new Feed();
        feed.setFeedId("feed1");
        mockFeeds.add(feed);

        when(feedDao.getAllFeedByUserId(userId, page, size)).thenReturn(mockFeeds);

        Map<Long, List<FeedDTO>> result = feedService.getFeedsByUserWithPagination(userId, page, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(1L).size());  // Assuming 1 record exists
        verify(feedDao, times(1)).getAllFeedByUserId(userId, page, size);
    }

    @Test
    public void testGetFeedsByUserWithPagination_InvalidUser() {
        String userId = "";
        int page = 1;
        int size = 10;

        Map<Long, List<FeedDTO>> result = feedService.getFeedsByUserWithPagination(userId, page, size);

        assertTrue(result.isEmpty());
        verify(feedDao, never()).getAllFeedByUserId(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetFeedsByUserWithPagination_NoFeedsFound() {
        String userId = "test@example.com";
        int page = 1;
        int size = 10;

        when(feedDao.getAllFeedByUserId(userId, page, size)).thenReturn(Collections.emptyList());

        Map<Long, List<FeedDTO>> result = feedService.getFeedsByUserWithPagination(userId, page, size);

        assertTrue(result.isEmpty());
        verify(feedDao, times(1)).getAllFeedByUserId(userId, page, size);
    }

    @Test
    public void testFindByFeedId_FeedExists() {
        String feedId = "feed1";
        Feed mockFeed = new Feed();
        mockFeed.setFeedId(feedId);

        when(feedDao.findById(feedId)).thenReturn(mockFeed);

        FeedDTO result = feedService.findByFeedId(feedId);

        assertNotNull(result);
        assertEquals(feedId, result.getFeedId());
        verify(feedDao, times(1)).findById(feedId);
    }

    @Test
    public void testFindByFeedId_FeedNotFound() {
        String feedId = "nonExistingFeed";

        when(feedDao.findById(feedId)).thenReturn(null);

        FeedDTO result = feedService.findByFeedId(feedId);

        assertNull(result);
        verify(feedDao, times(1)).findById(feedId);
    }


    @Test
    public void testUpdateReadStatusById_Success() {
        String feedId = "feed1";
        Feed mockFeed = new Feed();
        mockFeed.setFeedId(feedId);

        when(feedDao.upateReadStatus(feedId)).thenReturn(true);
        when(feedDao.findById(feedId)).thenReturn(mockFeed);

        FeedDTO result = feedService.updateReadStatusById(feedId);

        assertNotNull(result);
        assertEquals(feedId, result.getFeedId());
        verify(feedDao, times(1)).upateReadStatus(feedId);
        verify(feedDao, times(1)).findById(feedId);
    }

    @Test
    public void testUpdateReadStatusById_Failure() {
        String feedId = "feed1";

        when(feedDao.upateReadStatus(feedId)).thenReturn(false);

        FeedDTO result = feedService.updateReadStatusById(feedId);

        assertNotNull(result); 
        verify(feedDao, times(1)).upateReadStatus(feedId);
        verify(feedDao, never()).findById(anyString()); 
    }
}

