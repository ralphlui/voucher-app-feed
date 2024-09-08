package sg.edu.nus.iss.voucher.feed.workflow.utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import sg.edu.nus.iss.voucher.feed.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import voucher.management.app.auth.entity.User;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class JSONReaderTest {

    @InjectMocks
    private JSONReader jsonReader;

    @Mock
    private AuthAPICall apiCall;

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
    public void testGetAllTargetUsers() throws Exception {
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


        when(apiCall.getUsersByPreferences(eq("Food"), eq(0), anyInt())).thenReturn(page1Response.toString());
       
        ArrayList<User> users = jsonReader.getUsersByPreferences("Food");

        assertEquals(2, users.size(), "The number of users returned should be 2");

        User firstUser = users.get(0);
        assertEquals("1", firstUser.getUserId());
        assertEquals("user1@example.com", firstUser.getEmail());
        assertEquals("User One", firstUser.getUsername());

        User secondUser = users.get(1);
        assertEquals("2", secondUser.getUserId());
        assertEquals("user2@example.com", secondUser.getEmail());
        assertEquals("User Two", secondUser.getUsername());

      
        verify(apiCall).getUsersByPreferences(eq("Food"), eq(0), anyInt());
    }



}
