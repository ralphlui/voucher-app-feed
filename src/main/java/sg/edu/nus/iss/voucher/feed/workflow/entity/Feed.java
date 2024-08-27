package sg.edu.nus.iss.voucher.feed.workflow.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Feed {
	
	private String feedId ="";
	private String campaign="";
	private String store ="";
	private String isDeleted = "0";
	private String isReaded = "0";
	private String readTime="";
	private String targetUserName="";
	private String targetUserEmail="";
	private String createdDate="";
	private String updatedDate="";
	private String category="";
	
	
}
