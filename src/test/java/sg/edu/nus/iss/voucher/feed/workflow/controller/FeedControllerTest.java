package sg.edu.nus.iss.voucher.feed.workflow.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import sg.edu.nus.iss.voucher.feed.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.feed.workflow.dto.AuditDTO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.HTTPVerb;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.AuditService;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.FeedService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedService feedService;
    
    @MockBean
	private AuthAPICall authAPICall; 
    
    @MockBean
    private AuditService auditService;
    
    @Value("${audit.activity.type.prefix}")
	String activityTypePrefix;
    
    private static FeedDTO feedDTO;
    private static AuditDTO auditDTO ;
    
    static String userId ="user123";
    
    @BeforeAll
    static void setUp() {
        feedDTO = new FeedDTO(); 
        feedDTO.setFeedId("123");
        feedDTO.setUserId("111");
        feedDTO.setUserName("Eleven");
        feedDTO.setEmail("eleven.11@gmail.com");
        feedDTO.setUserName("Test");
        auditDTO = new AuditDTO();
        
    }

    @Test
    void testGetByUserId() throws Exception {
       
        int page = 0;
        int size = 50;

        List<FeedDTO> mockFeeds = new ArrayList<>();
        
        Map<Long, List<FeedDTO>> resultMap = new HashMap<>();
        resultMap.put(10L, mockFeeds);

        when(feedService.getFeedsByUserWithPagination(userId, page, size)).thenReturn(resultMap);
               
        when(auditService.createAuditDTO(userId, "Feed List by User", activityTypePrefix,"/api/feeds/users/"+userId, HTTPVerb.GET))
            .thenReturn(auditDTO);



        mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds/users/{userId}", userId)
        		.header("X-User-Id", userId)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(mockFeeds.size()))
                .andExpect(jsonPath("$.message").value("Successfully get all feeds"))
                .andExpect(jsonPath("$.totalRecord").value(10));

        verify(auditService).logAudit(auditDTO, 200, "Successfully get all feeds");
    }
    
    @Test
    public void testGetFeedById() throws Exception {
        String feedId = "123";

        when(auditService.createAuditDTO(userId, "Find Feed by Id", activityTypePrefix,"/api/feeds/"+feedId, HTTPVerb.GET))
        .thenReturn(auditDTO);
        
        when(feedService.findByFeedId(feedId)).thenReturn(feedDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds/{id}", feedId)
        		.header("X-User-Id", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data.feedId").value(feedId))
                .andExpect(jsonPath("$.message").value("Feed get successfully."));
        verify(auditService).logAudit(auditDTO, 200, "Feed get successfully.");
    }

    @Test
    public void testPatchFeedReadStatus() throws Exception {
        String feedId = "123";   
        feedDTO.setIsReaded("1");
        
        when(auditService.createAuditDTO(userId, "Update Feed Status", activityTypePrefix,"/api/feeds/"+feedId+"/readStatus", HTTPVerb.PATCH))
        .thenReturn(auditDTO);

        when(feedService.updateReadStatusById(feedId)).thenReturn(feedDTO);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/feeds/{id}/readStatus", feedId)
        		.header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.data.feedId").value(feedId))
                .andExpect(jsonPath("$.message").value("Read status updated successfully for Id: " + feedId));
        
        verify(auditService).logAudit(auditDTO, 200, "Read status updated successfully for Id: " + feedId);
    }
    
    
}

