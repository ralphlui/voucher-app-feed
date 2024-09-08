package sg.edu.nus.iss.voucher.feed.workflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedDTO {

	private String feedId;
	private String campaignId;
	private String storeId;
	private String isReaded = "0";
	private String readTime;
	private String userId;
	private String email;
	private String userName;
	private String createdDate;
	private String category="";

	public FeedDTO() {
	}

}