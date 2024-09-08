package sg.edu.nus.iss.voucher.feed.workflow.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.FeedService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedService feedService;
    
    private static FeedDTO feedDTO;
    
    @BeforeAll
    static void setUp() {
        feedDTO = new FeedDTO(); 
        feedDTO.setFeedId("123");
        feedDTO.setUserId("111");
        feedDTO.setUserName("Eleven");
        feedDTO.setEmail("eleven.11@gmail.com");
        feedDTO.setUserName("Test");
        
    }

    @Test
    void testGetByUserId() throws Exception {
        String userId = "test@example.com";
        int page = 0;
        int size = 50;

        List<FeedDTO> mockFeeds = new ArrayList<>();
        
        Map<Long, List<FeedDTO>> resultMap = new HashMap<>();
        resultMap.put(10L, mockFeeds);

        when(feedService.getFeedsByUserWithPagination(userId, page, size)).thenReturn(resultMap);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds/users/{userId}", userId)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(mockFeeds.size()))
                .andExpect(jsonPath("$.message").value("Successfully get all feeds for user: " + userId))
                .andExpect(jsonPath("$.totalRecord").value(10));
    }
    
    @Test
    public void testGetFeedById() throws Exception {
        String feedId = "123";

        
        when(feedService.findByFeedId(feedId)).thenReturn(feedDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds/{id}", feedId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data.feedId").value(feedId))
                .andExpect(jsonPath("$.message").value("Feed get successfully."));
    }

    @Test
    public void testPatchFeedReadStatus() throws Exception {
        String feedId = "123";   
        feedDTO.setIsReaded("1");

        when(feedService.updateReadStatusById(feedId)).thenReturn(feedDTO);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/feeds/{id}/readStatus", feedId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data.feedId").value(feedId))
                .andExpect(jsonPath("$.message").value("Read status updated successfully for Id: " + feedId));
    }
    
    
}

