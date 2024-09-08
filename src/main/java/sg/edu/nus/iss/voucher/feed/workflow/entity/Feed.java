package sg.edu.nus.iss.voucher.feed.workflow.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Feed {
	
	private String feedId ="";
	private String campaignId="";
	private String campaignDescription="";
	private String storeId ="";
	private String storeName="";
	private String isDeleted = "0";
	private String isReaded = "0";
	private String readTime="";
	private String userId="";
	private String userName="";
	private String email="";
	private String createdDate="";
	private String updatedDate="";
	private String category="";
	
	
}
