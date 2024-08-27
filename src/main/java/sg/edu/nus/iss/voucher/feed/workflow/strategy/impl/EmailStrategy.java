package sg.edu.nus.iss.voucher.feed.workflow.strategy.impl;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SesSenderService;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.IFeedStrategy;

@Service
public class EmailStrategy implements IFeedStrategy {

	private static final Logger logger = LoggerFactory.getLogger(EmailStrategy.class);

	@Autowired
	private SesSenderService sesSenderService;

	@Value("${aws.ses.from}") String emailFrom;

	@Value("${frontend.url}") String frontendURL;

	@Override
	public boolean sendNotification(Feed feed) {

		try {

			String campaignURL = frontendURL + "/components/customer/campaigns";
			logger.info("campaignURL... {}", campaignURL);

			String subject = "Explore Our Exciting [[campaign]] Campaign" ;
			subject = subject.replace("[[campaign]]", feed.getCampaign());
			
			String body = "Dear [[name]],<br><br>" 
					+ "You’re invited to join us for a special event at [[store]]<br><br>" 
					+ "We’ve got exciting promotions and offers lined up just for you. Don’t miss out!<br>"
					+ " <h3> Vist  <a href=\"[[URL]]\" target=\"_self\">[[campaign]]</a> for more details and RSVP."
					+ " </h3>" 
					+ "Thank you for being a valued customer." + "<br><br>"
					+ "<i>(This is an auto-generated email, please do not reply)</i>";

			body = body.replace("[[name]]", feed.getTargetUserName());
			body = body.replace("[[store]]", feed.getStore());
			body = body.replace("[[campaign]]", feed.getCampaign());
			body = body.replace("[[URL]]", campaignURL);
			

			boolean isSend = sesSenderService.sendEmail(emailFrom, Arrays.asList(feed.getTargetUserEmail()), subject, body);
			logger.info("Email notification for Campaign id: " + feed.getCampaign() + " sent to user "
					+ feed.getTargetUserEmail() + ".");
			return isSend;
			

		} catch (Exception e) {
			logger.error("Error occurred while sendFeedEmail {} ..." , e.toString());
			e.printStackTrace();
		}
		return false;
	}

}