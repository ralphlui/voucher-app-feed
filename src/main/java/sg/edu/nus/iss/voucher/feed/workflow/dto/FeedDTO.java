package sg.edu.nus.iss.voucher.feed.workflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedDTO {

	private String feedId="";
	private String campaignId="";
	private String campaignDescription="";
	private String storeId="";
	private String storeName="";
	private String isReaded = "0";
	private String userId="";
	private String email="";
	private String userName="";
	private String category="";

	public FeedDTO() {
	}

}