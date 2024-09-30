package sg.edu.nus.iss.voucher.feed.workflow.utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQS;

import sg.edu.nus.iss.voucher.feed.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import sg.edu.nus.iss.voucher.feed.workflow.entity.TargetUser;

import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class JSONReaderTest {
	
	@MockBean
    private AmazonSNS amazonSNS;

    @MockBean
    private AmazonSQS amazonSQS;


    @Autowired
    private JSONReader jsonReader;

    @MockBean
    private AuthAPICall apiCall;
    
    static String userId ="1";
    
    private static final String PREFERENCES = "Food";
    private static final String PAGE_MAX_SIZE = "10";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        jsonReader.pageMaxSize = "10"; 
    }

    @Test
    public void testReadFeedMessage() {
    	JSONObject campaign = new JSONObject();
        campaign.put("campaignId", "123");
        campaign.put("description", "Happy Hour");

        JSONObject store = new JSONObject();
        store.put("storeId", "456");
        store.put("name", "MUJI");

        JSONObject message = new JSONObject();
        message.put("category", "Food");
        message.put("campaign", campaign);
        message.put("store", store);

        String messageString = message.toString();
        MessagePayload result = jsonReader.readFeedMessage(messageString);

        assertNotNull(result);
        assertEquals("Food", result.getCategory(), "Category mismatch");
        assertEquals("123", result.getCampaignId(), "Campaign ID mismatch");
        assertEquals("Happy Hour", result.getCampaignDescription(), "Campaign description mismatch");
        assertEquals("456", result.getStoreId(), "Store ID mismatch");
        assertEquals("MUJI", result.getStoreName(), "Store name mismatch");
    
    }
    
    
    @Test
    public void testGetUsersByPreferences() {
    	
    	 // Prepare mock responses
        JSONObject page1Response = new JSONObject();
        page1Response.put("totalRecord", 5L);
        JSONArray dataArrayPage1 = new JSONArray();

        // Create user objects for page 1
        JSONObject user1 = new JSONObject();
        user1.put("userID", "1");
        user1.put("email", "user1@example.com");
        user1.put("username", "User One");

        JSONObject user2 = new JSONObject();
        user2.put("userID", "2");
        user2.put("email", "user2@example.com");
        user2.put("username", "User Two");

        dataArrayPage1.add(user1);
        dataArrayPage1.add(user2);
        page1Response.put("data", dataArrayPage1);
        
        when(apiCall.getUsersByPreferences(PREFERENCES, 0, Integer.parseInt(PAGE_MAX_SIZE))).thenReturn(page1Response.toJSONString());
        when(apiCall.getUsersByPreferences(PREFERENCES, 1, Integer.parseInt(PAGE_MAX_SIZE))).thenReturn(page1Response.toJSONString());
        
        ArrayList<TargetUser> users = jsonReader.getUsersByPreferences(PREFERENCES);

        assertEquals(2, users.size());
        assertEquals("user1@example.com", users.get(0).getEmail());
        assertEquals("user2@example.com", users.get(1).getEmail());
    }



}