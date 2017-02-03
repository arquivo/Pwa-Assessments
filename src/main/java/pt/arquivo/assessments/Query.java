package pt.arquivo.assessments;


/**
 * Encapsulates data from query table
 * @author Miguel Costa
 */
public class Query {
		
	private int id;
	private String query; // query text
	private String periodStart;
	private String periodEnd;
	private String description;
	private int type;
		
	public Query(int id, String query, String  periodStart, String periodEnd, String description, int type) {
		this.id=id;
		this.query=query;
		this.periodStart=periodStart;
		this.periodEnd=periodEnd;
		this.description=description;
		this.type=type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	

}