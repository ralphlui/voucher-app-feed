package sg.edu.nus.iss.voucher.feed.workflow.strategy;

import org.springframework.stereotype.Component;

import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;

@Component
public interface IFeedStrategy {
	boolean sendNotification(Feed feed);
}
