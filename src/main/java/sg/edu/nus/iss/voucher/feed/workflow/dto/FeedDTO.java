package sg.edu.nus.iss.voucher.feed.workflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedDTO {

	private String feedId;
	private String campaign;
	private String store;
	private String isReaded = "0";
	private String readTime;
	private String targetUserName;
	private String targetUserEmail;
	private String createdDate;

	public FeedDTO() {
	}

}