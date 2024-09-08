package sg.edu.nus.iss.voucher.feed.workflow.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessagePayload {
	private String category;
	private String campaignId;
	private String campaignDescription;
    private String storeId;
    private String storeName;
   
}
