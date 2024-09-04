package sg.edu.nus.iss.voucher.feed.workflow.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedEventPayload {
	private String campaign;
	private String store;
	private String preference;

}
