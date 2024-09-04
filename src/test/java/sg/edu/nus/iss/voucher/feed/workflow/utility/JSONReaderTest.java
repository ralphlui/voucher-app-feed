package sg.edu.nus.iss.voucher.feed.workflow.utility;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import sg.edu.nus.iss.voucher.feed.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.feed.workflow.entity.FeedEventPayload;

import java.util.HashMap;

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
        String message = "{\"preference\":\"testPreference\",\"campaign\":\"{\\\"description\\\":\\\"testCampaign\\\"}\",\"store\":\"{\\\"name\\\":\\\"testStore\\\"}\"}";

        FeedEventPayload result = jsonReader.readFeedMessage(message);

        assertNotNull(result);
        assertEquals("testPreference", result.getPreference());
        assertEquals("testCampaign", result.getCampaign());
        assertEquals("testStore", result.getStore());
    }

    @Test
    public void testGetAllTargetUsers() throws ParseException {
        String mockResponse = "{\"totalRecord\":2,\"data\":[{\"email\":\"test1@example.com\",\"username\":\"user1\"},{\"email\":\"test2@example.com\",\"username\":\"user2\"}]}";

        when(apiCall.getUsersByPreferences(anyString(), anyInt(), anyInt())).thenReturn(mockResponse);

        HashMap<String, String> result = jsonReader.getUsersByPreferences("testPreference");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get("test1@example.com"));
        assertEquals("user2", result.get("test2@example.com"));
    }

}
