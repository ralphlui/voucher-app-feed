package sg.edu.nus.iss.voucher.feed.workflow.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.feed.workflow.dao.*;
import sg.edu.nus.iss.voucher.feed.workflow.dto.*;
import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.service.IFeedService;
import sg.edu.nus.iss.voucher.feed.workflow.utility.*;

@Service
public class FeedService implements IFeedService {

	private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

	private final FeedDAO feedDao;

	@Autowired
	public FeedService(FeedDAO feedDao) {
		this.feedDao = feedDao;
	}


	@Override
	public Map<Long, List<FeedDTO>> getFeedsByUserWithPagination(String userId, int page, int size) {

		logger.info("Getting all feeds by Email");
		Map<Long, List<FeedDTO>> result = new HashMap<>();
		List<FeedDTO> feedDTOList = new ArrayList<FeedDTO>();
		try {

			logger.info("userId: " + userId);
			if (!userId.equals("")) {
				
				List<Feed> feeds = feedDao.getAllFeedByUserId(userId, page, size);
				long totalRecord = feeds.size();
				if (totalRecord > 0) {
					logger.info("Found {}, converting to Feed DTOs...", totalRecord);
					for (Feed feed : feeds) {
						
						FeedDTO feedDTO = DTOMapper.toFeedDTO(feed);
						feedDTOList.add(feedDTO);
					}

					result.put(totalRecord, feedDTOList);
				} else {
					logger.info("No feed found...");
				}
			} else {
				logger.info("No Email found for UserId {}...", userId);
			}
		} catch (Exception ex) {
			logger.error("Find read Feed by User exception... {}", ex.toString());
		}
		return result;
	}

	@Override
	public FeedDTO findByFeedId(String feedId) {
		FeedDTO feedDTO = null;
		try {
			Feed feed = feedDao.findById(feedId);

			if (feed != null && feed.getFeedId() != null && !feed.getFeedId().isEmpty()) {
				feedDTO = DTOMapper.toFeedDTO(feed);
			} else {
				logger.info("Feed not found for feedId {}...", feedId);
			}
		} catch (Exception ex) {
			logger.error("findByFeedId exception: {}", ex.getMessage(), ex);
		}
		return feedDTO;
	}

	@Override
	public FeedDTO updateReadStatusById(String feedId) {
		FeedDTO feedDTO = new FeedDTO();
		try {
			if (feedId != null && !feedId.isEmpty()) {

				boolean success = feedDao.upateReadStatus(feedId);
				if (success) {
					Feed updatedFeed = feedDao.findById(feedId);
					feedDTO = DTOMapper.toFeedDTO(updatedFeed);
				} else {
					logger.info("Feed read status update failed for feedId {}...", feedId);
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Updating Feed Status by feedId exception... {}", ex.toString());

		}
		return feedDTO;
	}

}
