package sg.edu.nus.iss.voucher.feed.workflow.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.document.utils.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.utility.GeneralUtility;

@Repository
public class FeedDAO {

	private static final Logger logger = LoggerFactory.getLogger(FeedDAO.class);
	
	@Autowired
	private AmazonDynamoDB dynamoDBClient;

	@Value("${aws.dynamodb.feed}")
	private String feedTbl;
	
	private final DynamoDB dynamoDB;
	
    @Autowired
    public FeedDAO(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
		this.dynamoDB = new DynamoDB(dynamoDBClient);
    }


	final String FEEDID = "FeedId";
	final String CAMPAIGNID="CampaignId";
	final String CAMPAIGNDESCRIPTION = "CampaignDescription";
	final String STOREID = "StoreId";
	final String STORENAME = "StoreName";
	final String ISDELETED = "IsDeleted";
	final String ISREADED = "IsReaded";
	final String READTIME = "ReadTime";
	final String USERID = "UserId";
	final String EMAIL = "Email";
	final String USERNAME = "UserName";
	final String CREATEDDATE = "CreatedDate";
	final String CATEGORY ="Category";

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 

	public List<Feed> getAllFeedByUserId(String targetedUserId, int page, int size) {
		List<Feed> feedList = new ArrayList<>();

		try {
			Table table = dynamoDB.getTable(feedTbl);

			NameMap nameMap = new NameMap().with("#UserId", USERID).with("#IsDeleted", ISDELETED);
			ValueMap valueMap = new ValueMap().withString(":UserId", targetedUserId).withString(":IsDeleted",
					"0");

			String query = "#UserId = :UserId AND #IsDeleted = :IsDeleted";
			ScanSpec scanSpec = new ScanSpec().withFilterExpression(query).withNameMap(nameMap).withValueMap(valueMap)
					.withMaxResultSize(size);
			ItemCollection<ScanOutcome> items = table.scan(scanSpec);

			Iterator<Item> iterator = items.iterator();

			while (iterator.hasNext()) {
				Item item = iterator.next();
				Feed feed = new Feed();
				feed.setFeedId(item.getString(FEEDID));
				feed.setCampaignId(item.getString(CAMPAIGNID));
				feed.setCampaignDescription(item.getString(CAMPAIGNDESCRIPTION));
				feed.setStoreId(item.getString(STOREID));
				feed.setStoreName(item.getString(STORENAME));
				feed.setUserId(item.getString(USERID));
				feed.setEmail(item.getString(EMAIL));
				feed.setUserName(item.getString(USERNAME));
				feed.setIsDeleted(item.getString(ISDELETED));
				feed.setIsReaded(item.getString(ISREADED));
				feed.setReadTime(item.getString(READTIME));
				feed.setCreatedDate(item.getString(CREATEDDATE));
				feed.setCategory(item.getString(CATEGORY));
				feedList.add(feed);
			}

			//feedList.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));
			
			 // Sort the feedList by parsed date
	        Collections.sort(feedList, new Comparator<Feed>() {
	            @Override
	            public int compare(Feed a, Feed b) {
	                LocalDateTime dateA = LocalDateTime.parse(a.getCreatedDate(), formatter);
	                LocalDateTime dateB = LocalDateTime.parse(b.getCreatedDate(), formatter);
	                return dateB.compareTo(dateA); // Sort in descending order
	            }
	        });


		} catch (Exception ex) {
			logger.error("Find all Feed by Email exception... {}", ex.toString());
		}
		return feedList;
	}
	
	public Feed findById(String feedId) {
	    Feed  feed = new Feed();
	    try {
	        Map<String, AttributeValue> key = new HashMap<>();
	        key.put(FEEDID, new AttributeValue().withS(feedId.trim()));

	        Map<String, AttributeValue> item = dynamoDBClient.getItem(
	                new GetItemRequest().withTableName(feedTbl).withKey(key)).getItem();

	        if (item != null && !item.isEmpty()) {
	           
	            feed.setFeedId(GeneralUtility.makeNotNull(item.get(FEEDID).getS()));
	            feed.setCampaignId(GeneralUtility.makeNotNull(item.get(CAMPAIGNID).getS()));	            
	            feed.setStoreId(GeneralUtility.makeNotNull(item.get(STOREID).getS()));
	            feed.setUserName(GeneralUtility.makeNotNull(item.get(USERNAME).getS()));
	            feed.setEmail(GeneralUtility.makeNotNull(item.get(EMAIL).getS()));
	            feed.setUserId(GeneralUtility.makeNotNull(item.get(USERID).getS()));
	            feed.setCategory(GeneralUtility.makeNotNull(item.get(CATEGORY).getS()));
	            feed.setIsDeleted(GeneralUtility.makeNotNull(item.get(ISDELETED).getS()));
	            feed.setIsReaded(GeneralUtility.makeNotNull(item.get(ISREADED).getS()));
	            feed.setReadTime(GeneralUtility.makeNotNull(item.get(READTIME).getS()));
	            feed.setCreatedDate(GeneralUtility.makeNotNull(item.get(CREATEDDATE).getS()));
	            feed.setCampaignDescription(GeneralUtility.makeNotNull(item.get(CAMPAIGNDESCRIPTION).getS()));
	            feed.setStoreName(GeneralUtility.makeNotNull(item.get(STORENAME).getS()));
	        }

	    } catch (Exception ex) {
	        logger.error("Find Feed by Id exception: {}", ex.getMessage(), ex);
	    }
	    return feed;
	}
	
	public boolean upateReadStatus(String id) {
		boolean retVal = false;
		 try {
			 Table table = dynamoDB.getTable(feedTbl);
			 UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(FEEDID,id)
					 .withUpdateExpression("set IsReaded = :IsReaded , ReadTime = :ReadTime")
					 .withValueMap(new ValueMap().withString(":IsReaded", "1").withString(":ReadTime",
							 LocalDateTime.now().format(formatter))).withReturnValues(ReturnValue.UPDATED_NEW);
			 UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
			 if(outcome != null && outcome.getItem() !=null) {
				 retVal = true;
			 }
			 
		 }catch (Exception ex) {
		        logger.error("Update Feed read status exception: {}", ex.getMessage(), ex);
		    }
		return retVal;
	}
	
	public Feed saveFeed(Feed feed) {
		 Feed addedFeed = new Feed();
		try {
			String feedId =UUID.randomUUID().toString();
			 Table table = dynamoDB.getTable(feedTbl);
			 feed.setCreatedDate( LocalDateTime.now().format(formatter));
			 feed.setFeedId(feedId);
			 
			 PutItemOutcome outCome = table.putItem(new Item().withPrimaryKey(FEEDID, feed.getFeedId())
					 .with(CREATEDDATE, feed.getCreatedDate())
					 .with(USERID, feed.getUserId())
					 .with(EMAIL, feed.getEmail())
					 .with(USERNAME, feed.getUserName())
					 .with(ISDELETED,"0").with(ISREADED, "0")
					 .with(CAMPAIGNID, feed.getCampaignId())
					 .with(CAMPAIGNDESCRIPTION, feed.getCampaignDescription())
					 .with(STOREID, feed.getStoreId())
					 .with(STORENAME, feed.getStoreName())
					 .with(CATEGORY, feed.getCategory())
					 .with(READTIME, ""));
			 
			
			 addedFeed = findById(feedId);
					 
		}catch (Exception ex) {
			ex.printStackTrace();
	        logger.error("Save Feed exception: {}", ex.getMessage(), ex);
	    }
		return addedFeed;
		
	}
	
	public boolean checkFeedExistsByUserAndCampaign(String userId,String campaignId) {
		boolean isExists = false;

	    try {
	        Table table = dynamoDB.getTable(feedTbl);

	        NameMap nameMap = new NameMap()
	            .with("#UserId", USERID)
	            .with("#CampaignId", CAMPAIGNID)
	            .with("#IsDeleted", ISDELETED);
	        
	        ValueMap valueMap = new ValueMap()
	            .withString(":UserId", userId)
	            .withString(":CampaignId", campaignId)
	            .withString(":IsDeleted", "0");

	        String filterExpression = "#UserId = :UserId AND #CampaignId = :CampaignId AND #IsDeleted = :IsDeleted";

	        ScanSpec scanSpec = new ScanSpec()
	            .withFilterExpression(filterExpression)
	            .withNameMap(nameMap)
	            .withValueMap(valueMap);

	        ItemCollection<ScanOutcome> items = table.scan(scanSpec);

	        if (items.iterator().hasNext()) {
	            isExists = true;
	        }

	    } catch (Exception ex) {
	        logger.error("Error while checking feed existence for userId: {} and campaignId: {}. Exception: {}", userId, campaignId, ex.toString());
	    }

	    return isExists;
	}
	
}
