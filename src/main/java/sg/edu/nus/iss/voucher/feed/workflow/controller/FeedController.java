package sg.edu.nus.iss.voucher.feed.workflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.edu.nus.iss.voucher.feed.workflow.dto.*;
import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.AuditService;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.FeedService;
import sg.edu.nus.iss.voucher.feed.workflow.utility.*;

@RestController
@Validated
@RequestMapping("/api/feeds")
public class FeedController {
	private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

	@Autowired
	private FeedService feedService;
	
	@Autowired
	private AuditService auditService;
	
	
	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;

	
	@GetMapping(value = "/users/{userId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<FeedDTO>>> getByUserId(@RequestHeader("X-User-Id") String XUserId,@PathVariable("userId") String userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {

		logger.info("Call feeds by UserId feed API...");
		AuditDTO auditDTO = auditService.createAuditDTO(XUserId, "Feed List by User", activityTypePrefix,"/api/feeds/users/"+userId, HTTPVerb.GET);
		String message = "";
		try {
			
			logger.info("userId: " + userId);
		
			 userId = GeneralUtility.makeNotNull(userId).trim();
			if (!userId.equals("")) {
				
				Map<Long, List<FeedDTO>> resultMap = feedService.getFeedsByUserWithPagination(userId, page, size);
				List<FeedDTO> feedDTOList = new ArrayList<FeedDTO>();
				long totalRecord = 0;
				if (resultMap.size() == 0) {
					String mesasge = "Feeds not found.";
					logger.error(mesasge);
					auditService.logAudit(auditDTO,200,message);
				
					return ResponseEntity.status(HttpStatus.OK)
							.body(APIResponse.success(feedDTOList, mesasge , totalRecord));
				}
				for (Map.Entry<Long, List<FeedDTO>> entry : resultMap.entrySet()) {
					totalRecord = entry.getKey();
					feedDTOList = entry.getValue();
					logger.info("totalRecord: " + totalRecord);
					logger.info("FeedDTO List: " + feedDTOList);
				}
				
				message = "Successfully get all feeds";
				auditService.logAudit(auditDTO,200,message);
				
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(feedDTOList, message,
								totalRecord));
			} else {
				message = "Bad Request:User could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "An unexpected error occurred. Please contact support." ;
			logger.error(message);
			auditDTO.setRemarks(e.toString());
			auditService.logAudit(auditDTO,500,message);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
	
	
	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<FeedDTO>> getFeedById(@RequestHeader("X-User-Id") String userId, @PathVariable("id") String id) {
		
		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Find Feed by Id", activityTypePrefix,"/api/feeds/"+id, HTTPVerb.GET);
		String message = "";
		
		try {
			logger.info("Calling getById Feed API...");
			
			String feedId = GeneralUtility.makeNotNull(id);
			logger.info("feedId: " + feedId);
			if (!GeneralUtility.makeNotNull(feedId).equals("")) {
				FeedDTO feedDTO = feedService.findByFeedId(feedId);
				if (feedDTO!= null && GeneralUtility.makeNotNull(feedDTO.getFeedId()).equals(feedId)) {
					message ="Feed get successfully.";
					auditService.logAudit(auditDTO,200,message);
					
					return ResponseEntity.status(HttpStatus.OK)
							.body(APIResponse.success(feedDTO, message));
				
				} else {
					message ="Feed not found for Id: " + feedId;
					auditService.logAudit(auditDTO,404,message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.error(message));
				}
				
			 
			}else {
				message = "Bad Request:FeedId could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO,400,message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
				
		} catch (Exception e) {
			e.printStackTrace();
			message = "An unexpected error occurred. Please contact support." ;
			logger.error(message + e.toString());
			auditDTO.setRemarks(e.toString());
			auditService.logAudit(auditDTO,500,message);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
	
	@PatchMapping(value = "/{id}/readStatus", produces = "application/json")
	public ResponseEntity<APIResponse<FeedDTO>> patchFeedReadStatus(@RequestHeader("X-User-Id") String userId,@PathVariable("id") String id) {
		
		AuditDTO auditDTO = auditService.createAuditDTO(userId, "Update Feed Status", activityTypePrefix,"/api/feeds/"+id+"/readStatus", HTTPVerb.PATCH);
		String message = "";
		
		try {
			logger.info("Calling updateReadStatusById Feed API...");
			String feedId = GeneralUtility.makeNotNull(id);
			logger.info("feedId: " + feedId);
			 
			if (!GeneralUtility.makeNotNull(feedId).equals("")) {
				FeedDTO feedDTO = feedService.updateReadStatusById(feedId);

				if (GeneralUtility.makeNotNull(feedDTO.getFeedId()).equals(feedId)) {
					message = "Read status updated successfully for Id: "+feedId;
					auditService.logAudit(auditDTO,200,message);
					return ResponseEntity.status(HttpStatus.OK).body(
							APIResponse.success(feedDTO, message));
				} else {
					message ="Feed not found for Id: " + feedId;
					auditService.logAudit(auditDTO,404,message);
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.error(message));
				}
			} else {
				message = "Bad Request:FeedId could not be blank.";
				auditService.logAudit(auditDTO,400,message);
				logger.error(message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "An unexpected error occurred. Please contact support." ;
			logger.error(message + e.toString());
			auditDTO.setRemarks(e.toString());
			auditService.logAudit(auditDTO,500,message);
			 
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}
	
	
}
