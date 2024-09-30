package sg.edu.nus.iss.voucher.feed.workflow.utility;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sg.edu.nus.iss.voucher.feed.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import sg.edu.nus.iss.voucher.feed.workflow.entity.TargetUser;

@Component
public class JSONReader {

	@Value("${api.list.call.page.max-size}")
	public String pageMaxSize;

	@Autowired
	AuthAPICall apiCall;

	private static final Logger logger = LoggerFactory.getLogger(JSONReader.class);

	public MessagePayload readFeedMessage(String message) {
		MessagePayload feedMsg = new MessagePayload();
		try {
			// Parse the SNS notification payload
			JSONObject messageObject = (JSONObject) new JSONParser().parse(message);
			if (messageObject != null) {
				
				String category = (String) messageObject.get("category");

				JSONObject campaign = (JSONObject) messageObject.get("campaign");
				String campaignId = (String) campaign.get("campaignId");
				String campaignDescription = (String) campaign.get("description");

				JSONObject store = (JSONObject) messageObject.get("store");
				String storeId = (String) store.get("storeId");
				String storeName = (String) store.get("name");

				// Log or process the data
				logger.info("Category: " + category);
				logger.info("Campaign ID: " + campaignId);
				logger.info("Campaign Description: " + campaignDescription);
				logger.info("Store ID: " + storeId);
				logger.info("Store Name: " + storeName);
				
				feedMsg.setCategory(category);
				feedMsg.setCampaignId(campaignId);
				feedMsg.setCampaignDescription(campaignDescription);
				feedMsg.setStoreId(storeId);
				feedMsg.setStoreName(storeName);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Read Feed Message exception... {}", ex.toString());
		}
		return feedMsg;
	}

	public ArrayList<TargetUser> getUsersByPreferences(String preferences) {
		int page = 0;
		int size = Integer.parseInt(pageMaxSize);
		int totalRecord;

		ArrayList<TargetUser> targetUsers = new ArrayList<TargetUser>();
		do {
			String responseStr = apiCall.getUsersByPreferences(preferences, page, size);

			try {

				JSONParser parser = new JSONParser();
				JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);

				totalRecord = ((Long) jsonResponse.get("totalRecord")).intValue();

				JSONArray data = (JSONArray) jsonResponse.get("data");
				for (Object obj : data) {
					JSONObject user = (JSONObject) obj;
					logger.info("User: " + user.toJSONString());
					
					String userId = GeneralUtility.makeNotNull(user.get("userID").toString());
					String email = GeneralUtility.makeNotNull(user.get("email").toString());
					String username = GeneralUtility.makeNotNull(user.get("username").toString());

					if (!email.isEmpty() ) {
						TargetUser targetUser = new TargetUser();
						targetUser.setUserId(userId);
						targetUser.setEmail(email);
						targetUser.setUsername(username);
						targetUsers.add(targetUser);
					}
				}

				page++;
			} catch (ParseException e) {
				e.printStackTrace();
				logger.error("Error parsing JSON response for getUsersByPreferences... {}", e.toString());
				break;
			}
		} while (totalRecord > page * size);
		return targetUsers;
	}

	
	public  String getActiveUser(String userId) {		 

		String userName = "";
		
			String responseStr = apiCall.getActiveUser(userId);

			try {

				JSONParser parser = new JSONParser();
				JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);	
				JSONObject data = (JSONObject) jsonResponse.get("data");
			 	logger.info("User: " + data.toJSONString());
					
			    userName = GeneralUtility.makeNotNull(data.get("username").toString());

			} catch (ParseException e) {
				e.printStackTrace();
				logger.error("Error parsing JSON response for getActiveUser... {}", e.toString());
				 
			}
		
		return userName;
	}

}
