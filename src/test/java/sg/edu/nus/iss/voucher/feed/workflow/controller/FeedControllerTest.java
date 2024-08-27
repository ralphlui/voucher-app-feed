package sg.edu.nus.iss.voucher.feed.workflow.controller;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import sg.edu.nus.iss.voucher.feed.workflow.dto.APIRequest;
import sg.edu.nus.iss.voucher.feed.workflow.dto.APIResponse;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.FeedService;

import java.util.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class FeedControllerTest {

    @Mock
    private FeedService feedService;

    @InjectMocks
    private FeedController feedController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllFeedsByEmail() {
        String email = "test@example.com";
        int page = 0;
        int size = 50;

        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setFeedId("123");
        feedDTO.setCampaign("camp123");
        feedDTO.setTargetUserEmail("targetUser@gmail.com");
        feedDTO.setIsReaded("true");

        List<FeedDTO> feedDTOList = Collections.singletonList(feedDTO);
        Map<Long, List<FeedDTO>> resultMap = new HashMap<>();
        resultMap.put(1L, feedDTOList);

        when(feedService.getFeedsByEmailWithPagination(anyString(), anyInt(), anyInt())).thenReturn(resultMap);

        APIRequest request = new APIRequest();
        request.setEmail(email);
        
        ResponseEntity<APIResponse<List<FeedDTO>>> response = feedController.getByEmail(request, page, size);

        verify(feedService, times(1)).getFeedsByEmailWithPagination(email, page, size);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEqualTo(feedDTOList);
        assertThat(response.getBody().getMessage()).isEqualTo("Successfully get all feeds for email: " + email);
        assertThat(response.getBody().getTotalRecord()).isEqualTo(1);
    }
    
    @Test
    public void testGetFeedById() {
       
        String feedId = "123";
        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setFeedId(feedId);

        Feed feed = new Feed();
        feed.setFeedId(feedId);

        when(feedService.findByFeedId(feedId)).thenReturn(feedDTO);

        ResponseEntity<APIResponse<FeedDTO>> response = feedController.getFeedById(feed);

        verify(feedService, times(1)).findByFeedId(feedId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Feed get successfully.");
        assertThat(response.getBody().getData().getFeedId()).isEqualTo(feedId);
    }
    
    @Test
    public void updateReadStatusById() {
        String feedId = "1";
        Feed feed = new Feed();
        feed.setFeedId(feedId);

        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setFeedId(feedId);

        when(feedService.updateReadStatusById(feedId)).thenReturn(feedDTO);

        ResponseEntity<APIResponse<FeedDTO>> response = feedController.updateReadStatusById(feed);

        verify(feedService, times(1)).updateReadStatusById(feedId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Read status updated successfully for Id: 1");
        assertThat(response.getBody().getData().getFeedId()).isEqualTo(feedId);
    }


    
}
