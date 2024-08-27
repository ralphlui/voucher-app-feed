package sg.edu.nus.iss.voucher.feed.workflow.api.connector;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
public class AuthAPICall {

	@Value("${auth.api.url}")
    private String authURL;

	private static final Logger logger = LoggerFactory.getLogger(AuthAPICall.class);

	
	public String getTargetUsers(String category, int page, int size) {
	    String responseStr = "";
	    
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    try {
	    	String encodedCategory = URLEncoder.encode(category.trim(), StandardCharsets.UTF_8.toString());
	        String url = authURL.trim() + "/user?category=" + encodedCategory + "&page=" + page + "&size=" + size;
	        logger.info("getTargetUsers url : " + url);
	        RequestConfig config = RequestConfig.custom()
	                .setConnectTimeout(30000)
	                .setConnectionRequestTimeout(30000)
	                .setSocketTimeout(30000)
	                .build();
	        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	        HttpGet request = new HttpGet(url);
	        CloseableHttpResponse httpResponse = httpClient.execute(request);
	        try {
	            byte[] responseByteArray = EntityUtils.toByteArray(httpResponse.getEntity());
	            responseStr = new String(responseByteArray, Charset.forName("UTF-8"));
	            logger.info("getTargetUsers: " + responseStr);
	        } catch (Exception e) {
	            e.printStackTrace();
	            logger.error("getTargetUsers exception... {}", e.toString());
	        } finally {
	            try {
	                httpResponse.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	                logger.error("getTargetUsers exception... {}", e.toString());
	            }
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        logger.error("getTargetUsers exception... {}", ex.toString());
	    }
	    return responseStr;
	}
	
	
}
