package sg.edu.nus.iss.voucher.feed.workflow.utility;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;

@Component
public class DTOMapper {

	public static FeedDTO toFeedDTO(Feed feed) {
		FeedDTO feedDTO = new FeedDTO();
		feedDTO.setCampaignId(feed.getCampaignId());
		feedDTO.setStoreId(feed.getStoreId());
		feedDTO.setFeedId(feed.getFeedId());
		feedDTO.setIsReaded(feed.getIsReaded());
		feedDTO.setReadTime(feed.getReadTime());
		feedDTO.setUserId(feed.getUserId());
		feedDTO.setUserName(feed.getUserName());
		feedDTO.setEmail(feed.getEmail());
        feedDTO.setCreatedDate(feed.getCreatedDate());
        feedDTO.setCategory(feed.getCategory());
		return feedDTO;
	}

}