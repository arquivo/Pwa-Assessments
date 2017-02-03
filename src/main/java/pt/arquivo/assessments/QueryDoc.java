package pt.arquivo.assessments;

import java.util.Date;

/**
 * Encapsulates data from queriesdocuments table
 */
public class QueryDoc {
	private int id; // queriesdocuments entry id
	private int queryId; // query id
	private int docId; // document id
	private String query;
	private String description;
	private String periodStart;
	private String periodEnd;
	private String url;
	private String urlArchived;
	private Date date;		
	private int nAssessments; // number of assessments
	
	/**
	 * 
	 * @param id
	 * @param queryId
	 * @param docId
	 * @param query
	 * @param description
	 * @param periodStart
	 * @param periodEnd
	 * @param url
	 * @param urlArchived
	 * @param date
	 * @param nAssessments
	 */
	public QueryDoc(int id, int queryId, int docId, String query, String description, String periodStart, String periodEnd, String url, String urlArchived, Date date, int nAssessments) {
		this.id=id;
		this.queryId=queryId;
		this.docId=docId;	
		this.query=query;
		this.description=description;
		this.periodStart=periodStart;
		this.periodEnd=periodEnd;
		this.url=url;
		this.urlArchived=urlArchived;
		this.date=date;
		this.nAssessments=nAssessments;
	}	
		
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getQueryId() {
		return queryId;
	}
	
	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	
	public int getDocId() {
		return docId;
	}
	
	public void setDocId(int docId) {
		this.docId = docId;
	}
	
	public int getnAssessments() {
		return nAssessments;
	}
	
	public void setnAssessments(int nAssessments) {
		this.nAssessments = nAssessments;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = query;
	}
	
	public String getPeriodStart() {
		return periodStart;
	}

	public void setPeriodStart(String periodStart) {
		this.periodStart = periodStart;
	}

	public String getPeriodEnd() {
		return periodEnd;
	}

	public void setPeriodEnd(String periodEnd) {
		this.periodEnd = periodEnd;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrlArchived() {
		return urlArchived;
	}
	
	public void setUrlArchived(String urlArchived) {
		this.urlArchived = urlArchived;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
}
