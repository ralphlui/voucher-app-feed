package sg.edu.nus.iss.voucher.feed.workflow.service;

import java.util.List;
import java.util.Map;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;

public interface IFeedService {
	public Map<Long, List<FeedDTO>> getFeedsByUserWithPagination(String userId, int page, int size);
	
	public FeedDTO findByFeedId(String feedId);
	
	public FeedDTO updateReadStatusById(String feedId);
	
}
