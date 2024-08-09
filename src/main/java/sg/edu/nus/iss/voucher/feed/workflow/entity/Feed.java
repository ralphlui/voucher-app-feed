package sg.edu.nus.iss.voucher.feed.workflow.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

public class Feed {
	@Id
	@UuidGenerator(style = UuidGenerator.Style.AUTO)
	private String feedId;
	
	@Column(nullable = false)
	private String campaignId;

	@Column(nullable = false)
	private boolean isDeleted = false;
	
	@Column(nullable = false)
	private boolean isRead = false;
	
	@Column(nullable = true, columnDefinition = "datetime")
	private LocalDateTime readTime;
		
	@Column(nullable = false)
	private String targetUserId;

	@Column(nullable = false, columnDefinition = "datetime default now()")
	private LocalDateTime createdDate;
}
