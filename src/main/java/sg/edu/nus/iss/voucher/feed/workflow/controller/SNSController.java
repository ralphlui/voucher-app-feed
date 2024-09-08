package sg.edu.nus.iss.voucher.feed.workflow.controller;

import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SNSSubscriptionService;

@RestController
@RequestMapping("/api/feeds")
public class SNSController {

	private static final Logger logger = LoggerFactory.getLogger(SNSController.class);

	@Autowired
	private SNSSubscriptionService snsReceiverService;

	@PostMapping(value = "/sns/notification", produces = "application/json")
	public ResponseEntity<String> receiveNotification(@RequestBody String message,
			@RequestHeader Map<String, String> headers) {
		logger.info("Call receiveNotification API...");

		JSONParser parser = new JSONParser();
		try {
			JSONObject jsonObject = (JSONObject) parser.parse(message);
			String type = (String) jsonObject.get("Type");
			String msg = (String) jsonObject.get("Message");
			String msgId = (String) jsonObject.get("MessageId");

			logger.info("Type: " + type);
			logger.info("Message: " + msg);
			logger.info("MessageId: " + msgId);

			switch (type) {
			case "SubscriptionConfirmation":
				snsReceiverService.confirmSubscription(message);
				return ResponseEntity.status(HttpStatus.OK).body("Subscription confirmed");

			case "Notification":
			    String retMsg = snsReceiverService.processNotification(message);
			    if (retMsg.contains("Bad Request")) {
			    	return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			                .body("Failed to process notification for MessageId: "+msgId +"\n"+retMsg);
			    } else {
			    	return ResponseEntity.status(HttpStatus.OK)
			                .body("Notification processed successfully"+"\n"+retMsg);
			        
			}

			default:
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Invalid SNS notification type");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Calling receiveNotification API failed , " + e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
		}

	}

}
