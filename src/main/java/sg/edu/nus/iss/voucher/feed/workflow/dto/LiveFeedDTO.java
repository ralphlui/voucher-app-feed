package sg.edu.nus.iss.voucher.feed.workflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveFeedDTO {

	private String feedId ="";
	private String campaignId="";
	private String campaignDescription="";
	private String storeId ="";
	private String storeName="";
	private String userId;
	private String email;
	private String userName;
}
