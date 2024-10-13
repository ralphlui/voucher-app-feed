package sg.edu.nus.iss.voucher.feed.workflow.strategy;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;

@Component
public interface IFeedStrategy {
	boolean sendNotification(FeedDTO feedDTO);
}
