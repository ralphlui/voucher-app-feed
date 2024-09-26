package sg.edu.nus.iss.voucher.feed.workflow.controller;

import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sns.model.MessageAttributeValue;

import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SNSSubscriptionService;
import sg.edu.nus.iss.voucher.feed.workflow.dto.AuditDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.HTTPVerb;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.AuditService;

@RestController
@RequestMapping("/api/feeds")
public class SNSController {

	private static final Logger logger = LoggerFactory.getLogger(SNSController.class);

	@Autowired
	private SNSSubscriptionService snsReceiverService;
	
	@Autowired
	private AuditService auditService;	
	
	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;

	@PostMapping(value = "/sns/notification", produces = "application/json")
	public ResponseEntity<String> receiveNotification(@RequestBody String message,
			@RequestHeader Map<String, String> headers) {
		logger.info("Call receiveNotification API...");
		  AuditDTO auditDTO  = new   AuditDTO ();
		String retMessage = "";
		
		JSONParser parser = new JSONParser();
		try {
			JSONObject jsonObject = (JSONObject) parser.parse(message);
			String type = (String) jsonObject.get("Type");
			String msg = (String) jsonObject.get("Message");
			String msgId = (String) jsonObject.get("MessageId");
			String userId="";
			// Extract the message attributes from the SNS message
	        JSONObject messageAttributes = (JSONObject) jsonObject.get("MessageAttributes");
	        
	        if (messageAttributes != null && messageAttributes.containsKey("X-User-Id")) {
	            JSONObject userIdAttribute = (JSONObject) messageAttributes.get("X-User-Id");
	            userId = (String) userIdAttribute.get("Value");  // Extract the value of the attribute
	        }

	        logger.info("X-User-Id : " + userId);
	        logger.info("Type: " + type);
	        logger.info("Message: " + msg);
	        logger.info("MessageId: " + msgId);
			
	         auditDTO = auditService.createAuditDTO(userId, "Feed Notification", activityTypePrefix,"/api/feeds/sns/notification", HTTPVerb.POST);
			

			switch (type) {
			case "SubscriptionConfirmation":
				snsReceiverService.confirmSubscription(message);
				return ResponseEntity.status(HttpStatus.OK).body("Subscription confirmed");

			case "Notification":
			    String retMsg = snsReceiverService.processNotification(message,userId);
			    if (retMsg.contains("Bad Request")) {
			    	retMessage = "Failed to process notification for MessageId: "+msgId +"::"+retMsg;
					auditService.logAudit(auditDTO,400,retMessage);
			    	return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			                .body("Failed to process notification for MessageId: "+msgId +"\n"+retMsg);
			    } else {
			    	
			    	retMessage = "Notification processed successfully for MessageId: "+msgId ;
			    	
					auditService.logAudit(auditDTO,200,retMessage);
					
			    	return ResponseEntity.status(HttpStatus.OK)
			                .body("Notification processed successfully"+"\n"+retMsg);
			        
			}

			default:
				
				retMessage = "Invalid SNS notification type "+msgId ;
				auditService.logAudit(auditDTO,400,retMessage);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Invalid SNS notification type");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Calling receiveNotification API failed , " + e.toString());
			retMessage ="Calling receiveNotification API failed";
			auditDTO.setRemarks(e.toString());
			auditService.logAudit(auditDTO,500,retMessage);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
		}

	}

}
