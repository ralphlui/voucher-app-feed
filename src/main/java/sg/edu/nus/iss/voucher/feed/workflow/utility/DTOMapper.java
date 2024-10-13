package sg.edu.nus.iss.voucher.feed.workflow.utility;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;

@Component
public class DTOMapper {

	public static FeedDTO toFeedDTO(Feed feed) {
		FeedDTO feedDTO = new FeedDTO();
		feedDTO.setCampaignId(GeneralUtility.makeNotNull(feed.getCampaignId()));
		feedDTO.setCampaignDescription(GeneralUtility.makeNotNull(feed.getCampaignDescription()));
		feedDTO.setStoreId(GeneralUtility.makeNotNull(feed.getStoreId()));
		feedDTO.setStoreName(GeneralUtility.makeNotNull(feed.getStoreName()));
		feedDTO.setFeedId(GeneralUtility.makeNotNull(feed.getFeedId()));
		feedDTO.setIsReaded(GeneralUtility.makeNotNull(feed.getIsReaded()));
		feedDTO.setUserId(GeneralUtility.makeNotNull(feed.getUserId()));
		feedDTO.setUserName(GeneralUtility.makeNotNull(feed.getUserName()));
		feedDTO.setEmail(GeneralUtility.makeNotNull(feed.getEmail()));
        feedDTO.setCategory(GeneralUtility.makeNotNull(feed.getCategory()));
		return feedDTO;
	}

	
}
