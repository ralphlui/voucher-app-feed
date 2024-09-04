package sg.edu.nus.iss.voucher.feed.workflow.utility;

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
import sg.edu.nus.iss.voucher.feed.workflow.entity.FeedEventPayload;

@Component
public class JSONReader {

	@Value("${api.list.call.page.max-size}")
	public String pageMaxSize;

	@Autowired
	AuthAPICall apiCall;

	private static final Logger logger = LoggerFactory.getLogger(JSONReader.class);

	public FeedEventPayload readFeedMessage(String message) {
		FeedEventPayload feedMsg = new FeedEventPayload();
		try {
			JSONObject jsonMessage = (JSONObject) new JSONParser().parse(message);
			if (jsonMessage != null) {
				String preference = jsonMessage.get("preference").toString();
				String campaign = jsonMessage.get("campaign").toString();
				JSONObject jsonObjCampaign = (JSONObject) new JSONParser().parse(campaign);
				String campaignDescription = jsonObjCampaign.get("description").toString();

				String store = jsonMessage.get("store").toString();
				JSONObject jsonObjStore = (JSONObject) new JSONParser().parse(store);
				String storeName = jsonObjStore.get("name").toString();

				feedMsg.setPreference(GeneralUtility.makeNotNull(preference));
				feedMsg.setCampaign(GeneralUtility.makeNotNull(campaignDescription));
				feedMsg.setStore(GeneralUtility.makeNotNull(storeName));

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Updating Feed Status by feedId exception... {}", ex.toString());
		}
		return feedMsg;
	}

	public HashMap<String, String> getUsersByPreferences(String preferences) {
		int page = 1;
		int size = Integer.parseInt(pageMaxSize);
		int totalRecord;

		HashMap<String, String> targetUsers = new HashMap<String, String>();
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
					String email = GeneralUtility.makeNotNull(user.get("email").toString());
					String username = GeneralUtility.makeNotNull(user.get("username").toString());
					if (!email.isEmpty()) {
						targetUsers.put(email.trim(), username.trim());
					}
				}

				page++;
			} catch (ParseException e) {
				e.printStackTrace();
				logger.error("Error parsing JSON response... {}", e.toString());
				break;
			}
		} while (totalRecord > page * size);
		return targetUsers;
	}

	public String getUserEmailById(String userId) {

		String email = "";

		String responseStr = apiCall.getUserById(userId);
		try {

			JSONParser parser = new JSONParser();
			JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);

			JSONArray data = (JSONArray) jsonResponse.get("data");
			for (Object obj : data) {
				JSONObject user = (JSONObject) obj;
				logger.info("User: " + user.toJSONString());
				email = GeneralUtility.makeNotNull(user.get("email").toString());

			}
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error("Error parsing JSON response... {}", e.toString());

		}

		return email;
	}

}
