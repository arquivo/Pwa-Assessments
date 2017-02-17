package pt.arquivo.assessments;

import java.sql.Timestamp;

public class Doc {
	
	private long id;
	private String url;
	private Timestamp date;
	private String urlarchived;
	private String code;
	
	public Doc( long id , String url , Timestamp date , String urlarchived , String code  ) {
		this.id 			= id;
		this.url 			= url;
		this.date 			= date;
		this.urlarchived 	= urlarchived;
		this.code 			= code;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}
	public String getUrlarchived() {
		return urlarchived;
	}
	public void setUrlarchived(String urlarchived) {
		this.urlarchived = urlarchived;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	
}
