package sg.edu.nus.iss.voucher.feed.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import sg.edu.nus.iss.voucher.feed.workflow.websocket.handler.NotificationWebSocketHandler;


@SpringBootApplication
@EnableScheduling
public class VoucherAppFeedApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoucherAppFeedApplication.class, args);
	}
	
	@Bean
    public NotificationWebSocketHandler notificationWebSocketHandler() {
        return new NotificationWebSocketHandler();
    }

}
