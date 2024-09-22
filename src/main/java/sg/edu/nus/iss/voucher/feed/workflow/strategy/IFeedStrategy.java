package sg.edu.nus.iss.voucher.feed.workflow.strategy;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.voucher.feed.workflow.dto.LiveFeedDTO;

@Component
public interface IFeedStrategy {
	boolean sendNotification(LiveFeedDTO liveFeedDTO);
}
