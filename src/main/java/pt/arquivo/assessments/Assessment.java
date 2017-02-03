package pt.arquivo.assessments;

import java.util.Date;


/**
 * Encapsulates data from assessment table
 * @author Miguel Costa
 */
public class Assessment {
	
	public final static int ASSESSMENT_TYPE_MANUAL=0;
	public final static int ASSESSMENT_TYPE_AUTOMATIC=1;	
	public final static String GROUND_TRUTH_USER="groundtruth";
	public final static int RATE_HIGHLY_RELEVANT=2;
	public final static int RATE_RELEVANT=1;
	public final static int RATE_NOT_RELEVANT=0;
	public final static int RATE_IGNORE=-1;
	
	private String docCode; // document identifier
	private String docUrl;
	private Date docDate;
	private String docUrlArchived;
	private int topicNumber;
	private String topicPeriodStart;
	private String topicPeriodEnd;
	private String topicQuery;
	private int relevance; // assessment relevance
	private int relevanceType; // assessment relevance type	
	private int querydocId;
				

	public Assessment(String docUrl, Date docDate, String docUrlArchived, int relevance, int querydocId) {
		this.docUrl=docUrl;
		this.docDate=docDate;
		this.docUrlArchived=docUrlArchived;
		this.relevance=relevance;		
		this.querydocId=querydocId;
	}	
	
	public Assessment(String docCode, String docUrl, int topicNumber, String topicQuery, String topicPeriodStart, String topicPeriodEnd, int relevance, int relevanceType, int querydocId) {
		this.docCode=docCode;
		this.docUrl=docUrl;
		this.topicNumber=topicNumber;
		this.topicQuery=topicQuery;
		this.topicPeriodStart=topicPeriodStart;
		this.topicPeriodEnd=topicPeriodEnd;		
		this.relevance=relevance;
		this.relevanceType=relevanceType;
		this.querydocId=querydocId;
	}
	
	public String getDocUrl() {
		return docUrl;
	}

	public void setDocUrl(String docUrl) {
		this.docUrl = docUrl;
	}

	public Date getDocDate() {
		return docDate;
	}

	public void setDocDate(Date docDate) {
		this.docDate = docDate;
	}

	public String getDocUrlArchived() {
		return docUrlArchived;
	}

	public void setDocUrlArchived(String docUrlArchived) {
		this.docUrlArchived = docUrlArchived;
	}
	
	public String getDocCode() {
		return docCode;
	}
	
	public void setDocCode(String docCode) {
		this.docCode = docCode;
	}
	
	public int getTopicNumber() {
		return topicNumber;
	}
	
	public void setTopicNumber(int topicNumber) {
		this.topicNumber = topicNumber;
	}
	
	public int getRelevance() {
		return relevance;
	}
	
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	
	public int getQuerydocId() {
		return querydocId;
	}

	public void setQuerydocId(int querydocId) {
		this.querydocId = querydocId;
	}
	
	public String getTopicPeriodStart() {
		return topicPeriodStart;
	}

	public void setTopicPeriodStart(String topicPeriodStart) {
		this.topicPeriodStart = topicPeriodStart;
	}

	public String getTopicPeriodEnd() {
		return topicPeriodEnd;
	}

	public void setTopicPeriodEnd(String topicPeriodEnd) {
		this.topicPeriodEnd = topicPeriodEnd;
	}

	public int getRelevanceType() {
		return relevanceType;
	}

	public void setRelevanceType(int relevanceType) {
		this.relevanceType = relevanceType;
	}

	public String getTopicQuery() {
		return topicQuery;
	}

	public void setTopicQuery(String topicQuery) {
		this.topicQuery = topicQuery;
	}	
}