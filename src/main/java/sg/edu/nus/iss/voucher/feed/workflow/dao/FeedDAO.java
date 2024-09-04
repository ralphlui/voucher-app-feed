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
	final String CAMPAIGN = "Campaign";
	final String STORE = "Store";
	final String ISDELETED = "IsDeleted";
	final String ISREADED = "IsReaded";
	final String READTIME = "ReadTime";
	final String TARGETUSEREMAIL = "TargetUserEmail";
	final String TARGETUSERENAME = "TargetUserName";
	final String CREATEDDATE = "CreatedDate";

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 

	public List<Feed> getAllFeedByEmail(String targetedUserId, int page, int size) {
		List<Feed> feedList = new ArrayList<>();

		try {
			Table table = dynamoDB.getTable(feedTbl);

			NameMap nameMap = new NameMap().with("#TargetUserEmail", TARGETUSEREMAIL).with("#IsDeleted", ISDELETED);
			ValueMap valueMap = new ValueMap().withString(":TargetUserEmail", targetedUserId).withString(":IsDeleted",
					"0");

			String query = "#TargetUserEmail = :TargetUserEmail AND #IsDeleted = :IsDeleted";
			ScanSpec scanSpec = new ScanSpec().withFilterExpression(query).withNameMap(nameMap).withValueMap(valueMap)
					.withMaxResultSize(size);
			ItemCollection<ScanOutcome> items = table.scan(scanSpec);

			Iterator<Item> iterator = items.iterator();

			while (iterator.hasNext()) {
				Item item = iterator.next();
				Feed feed = new Feed();
				feed.setFeedId(item.getString(FEEDID));
				feed.setCampaign(item.getString(CAMPAIGN));
				feed.setStore(item.getString(STORE));
				feed.setTargetUserEmail(item.getString(TARGETUSEREMAIL));
				feed.setTargetUserName(item.getString(TARGETUSERENAME));
				feed.setIsDeleted(item.getString(ISDELETED));
				feed.setIsReaded(item.getString(ISREADED));
				feed.setReadTime(item.getString(READTIME));
				feed.setCreatedDate(item.getString(CREATEDDATE));
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
	    Feed feed = null;
	    try {
	        Map<String, AttributeValue> key = new HashMap<>();
	        key.put(FEEDID, new AttributeValue().withS(feedId.trim()));

	        Map<String, AttributeValue> item = dynamoDBClient.getItem(
	                new GetItemRequest().withTableName(feedTbl).withKey(key)).getItem();

	        if (item != null && !item.isEmpty()) {
	            feed = new Feed();
	            feed.setFeedId(GeneralUtility.makeNotNull(item.get(FEEDID).getS()));
	            feed.setCampaign(GeneralUtility.makeNotNull(item.get(CAMPAIGN).getS()));
	            feed.setStore(GeneralUtility.makeNotNull(item.get(STORE).getS()));
	            feed.setTargetUserName(GeneralUtility.makeNotNull(item.get(TARGETUSERENAME).getS()));
	            feed.setTargetUserEmail(GeneralUtility.makeNotNull(item.get(TARGETUSEREMAIL).getS()));
	            feed.setIsDeleted(GeneralUtility.makeNotNull(item.get(ISDELETED).getS()));
	            feed.setIsReaded(GeneralUtility.makeNotNull(item.get(ISREADED).getS()));
	            feed.setReadTime(GeneralUtility.makeNotNull(item.get(READTIME).getS()));
	            feed.setCreatedDate(GeneralUtility.makeNotNull(item.get(CREATEDDATE).getS()));
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
					 .with(TARGETUSEREMAIL, feed.getTargetUserEmail())
					 .with(TARGETUSERENAME, feed.getTargetUserName())
					 .with(ISDELETED,"0").with(ISREADED, "0")
					 .with(CAMPAIGN, feed.getCampaign())
					 .with(STORE, feed.getStore())
					 .with(READTIME, ""));
			 
			
			 addedFeed = findById(feedId);
					 
		}catch (Exception ex) {
			ex.printStackTrace();
	        logger.error("Save Feed exception: {}", ex.getMessage(), ex);
	    }
		return addedFeed;
		
	}
	
}
