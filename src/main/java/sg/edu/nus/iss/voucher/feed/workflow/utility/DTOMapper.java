package sg.edu.nus.iss.voucher.feed.workflow.utility;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;

@Component
public class DTOMapper {

	public static FeedDTO toFeedDTO(Feed feed) {
		FeedDTO feedDTO = new FeedDTO();
		feedDTO.setCampaign(feed.getCampaign());
		feedDTO.setStore(feed.getStore());
		feedDTO.setFeedId(feed.getFeedId());
		feedDTO.setIsReaded(feed.getIsReaded());
		feedDTO.setReadTime(feed.getReadTime());
		feedDTO.setTargetUserEmail(feed.getTargetUserEmail());
		feedDTO.setTargetUserName(feed.getTargetUserName());
        feedDTO.setCreatedDate(feed.getCreatedDate());
		return feedDTO;
	}

}
