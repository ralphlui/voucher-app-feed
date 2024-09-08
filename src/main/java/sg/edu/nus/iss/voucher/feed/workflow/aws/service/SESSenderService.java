package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

@Service
public class SESSenderService {

	private static final Logger logger = LoggerFactory.getLogger(SESSenderService.class);

	@Autowired
	private AmazonSimpleEmailService sesClient;

	public boolean sendEmail(String from, Collection<String> recipientsTo, String subject, String body)
			throws Exception {
		boolean isSent = false;

		logger.info("From: " + from);
		logger.info("To: " + recipientsTo.size() + " :: " + recipientsTo.toString());
		logger.info("body: " + body);
		logger.info("subject: " + subject);
		try {

			SendEmailRequest request = new SendEmailRequest()
					.withDestination(new Destination().withToAddresses(recipientsTo))
					.withMessage(new Message()
							.withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(body)))
							.withSubject(new Content().withCharset("UTF-8").withData(subject)))
					.withSource(from);

			sesClient.sendEmail(request);
			isSent = true;
			logger.info("Email sent successfully.");
		} catch (Exception ex) {
			logger.error("sendEmail exception... {}", ex.toString());

			isSent = false;
		}
		return isSent;

	}
}
