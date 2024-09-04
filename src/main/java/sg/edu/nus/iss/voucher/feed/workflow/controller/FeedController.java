package sg.edu.nus.iss.voucher.feed.workflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.feed.workflow.dto.*;
import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.FeedService;
import sg.edu.nus.iss.voucher.feed.workflow.utility.*;

@RestController
@Validated
@RequestMapping("/api/feeds")
public class FeedController {
	private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

	@Autowired
	private FeedService feedService;
	
	@GetMapping(value = "/users/{userId}", produces = "application/json")
	public ResponseEntity<APIResponse<List<FeedDTO>>> getByUserId(@PathVariable("userId") String userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {

		logger.info("Call feeds by UserId feed API...");
		try {
			String message = "";
			logger.info("userId: " + userId);
		
			 userId = GeneralUtility.makeNotNull(userId).trim();
			if (!userId.equals("")) {
				
				Map<Long, List<FeedDTO>> resultMap = feedService.getFeedsByUserWithPagination(userId, page, size);
				List<FeedDTO> feedDTOList = new ArrayList<FeedDTO>();
				long totalRecord = 0;
				if (resultMap.size() == 0) {
					String mesasge = "No feed found for user: ";
					logger.error(mesasge);
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.success(feedDTOList, mesasge + userId, totalRecord));
				}
				for (Map.Entry<Long, List<FeedDTO>> entry : resultMap.entrySet()) {
					totalRecord = entry.getKey();
					feedDTOList = entry.getValue();
					logger.info("totalRecord: " + totalRecord);
					logger.info("FeedDTO List: " + feedDTOList);
				}
				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(feedDTOList, "Successfully get all feeds for user: " + userId,
								totalRecord));
			} else {
				message = "Bad Request:User could not be blank.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Calling getAllByEmail feed API failed , " + e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(e.toString()));
		}
	}
	
	
	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<APIResponse<FeedDTO>> getFeedById(@PathVariable("id") String id) {
		try {
			logger.info("Calling getById Feed API...");
			String message = "";
			String feedId = GeneralUtility.makeNotNull(id);
			logger.info("feedId: " + feedId);
			if (!GeneralUtility.makeNotNull(feedId).equals("")) {
				FeedDTO feedDTO = feedService.findByFeedId(feedId);
				if (feedDTO!= null && GeneralUtility.makeNotNull(feedDTO.getFeedId()).equals(feedId)) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(APIResponse.success(feedDTO, "Feed get successfully."));
				
				} else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.error("Feed not found for Id: " + feedId));
				}
				
			 
			}else {
				message = "Bad Request:FeedId could not be blank.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
				
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Calling getById feed API failed , " + e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(e.toString()));
		}
	}
	
	@PatchMapping(value = "/{id}/readStatus", produces = "application/json")
	public ResponseEntity<APIResponse<FeedDTO>> patchFeedReadStatus(@PathVariable("id") String id) {
		try {
			logger.info("Calling updateReadStatusById Feed API...");
			String feedId = GeneralUtility.makeNotNull(id);
			logger.info("feedId: " + feedId);
			String message = "";
			if (!GeneralUtility.makeNotNull(feedId).equals("")) {
				FeedDTO feedDTO = feedService.updateReadStatusById(feedId);

				if (GeneralUtility.makeNotNull(feedDTO.getFeedId()).equals(feedId)) {
					return ResponseEntity.status(HttpStatus.OK).body(
							APIResponse.success(feedDTO, "Read status updated successfully for Id: "+feedId));
				} else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(APIResponse.error("Feed not found for Id: " + feedId));
				}
			} else {
				message = "Bad Request:FeedId could not be blank.";
				logger.error(message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Calling patchFeedReadStatus feed API failed , " + e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(e.toString()));
		}
	}
	
	
}
