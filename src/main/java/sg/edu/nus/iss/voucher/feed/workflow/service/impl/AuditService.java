package sg.edu.nus.iss.voucher.feed.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.feed.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SQSPublishingService;
import sg.edu.nus.iss.voucher.feed.workflow.dto.AuditDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.service.IAuditService;
import sg.edu.nus.iss.voucher.feed.workflow.utility.GeneralUtility;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

@Service
public class AuditService implements IAuditService {

	private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

	@Autowired
	AuthAPICall apiCall;
	
	@Autowired
	private JSONReader jsonReader;

	@Autowired
	private SQSPublishingService sqsPublishingService;

	@Override
	public void sendMessage(AuditDTO autAuditDTO) {

		try {
			String userId = GeneralUtility.makeNotNull(autAuditDTO.getUserId());

			if (!userId.equals("")) {
					
					String userName=jsonReader.getActiveUser(userId);
					if(!userName.equals("")){
					autAuditDTO.setUsername(userName);
				
			}
			}
			
			if(autAuditDTO.getUsername().equals("")) {
				autAuditDTO.setUsername("Invalid UserName");
			}

			sqsPublishingService.sendMessage(autAuditDTO);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error sending generateMessage to SQS: {}", e);
		}

	}

	public AuditDTO createAuditDTO(String userId, String activityType, String activityTypePrefix, String endpoint,
			HTTPVerb verb) {
		AuditDTO auditDTO = new AuditDTO();
		auditDTO.setActivityType(activityTypePrefix.trim() + activityType);
		auditDTO.setUserId(userId);
		auditDTO.setRequestType(verb);
		auditDTO.setRequestActionEndpoint(endpoint);
		return auditDTO;
	}

	public void logAudit(AuditDTO auditDTO, int stausCode, String message) {
		logger.error(message);
		auditDTO.setStatusCode(stausCode);
		if (stausCode == 200) {
			auditDTO.setResponseStatus(AuditResponseStatus.SUCCESS);

		} else {
			auditDTO.setResponseStatus(AuditResponseStatus.FAILED);
		}
		auditDTO.setActivityDescription(message);
		this.sendMessage(auditDTO);

	}

}
