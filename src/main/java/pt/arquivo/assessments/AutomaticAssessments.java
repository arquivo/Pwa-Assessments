package pt.arquivo.assessments;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


/**
 * Query PWA and parses results for assessment
 * @author Miguel Costa
 */
public class AutomaticAssessments {
	
	//private final static String DOMAIN="p15.arquivo.pt:8080";
	private final static String DOMAIN="p18.arquivo.pt";
	//private final static String URL_QUERY="http://"+DOMAIN+"/nutchwax/search.jsp?query=";	
	private final static String URL_QUERY="http://"+DOMAIN+"/search.jsp?query=";
	
	private final static int TIMEOUT=120 * 1000; // 2 minutes
	
	//private static SimpleDateFormat formatterDateRestriction = new SimpleDateFormat("dd/MM/yyyy"); // part of the date range restriction
	private static SimpleDateFormat formatterTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
	private SqlOperations op=null;

	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length!=3) {
			System.out.println("usage: <database> <username> <password>");
		}
				
		AutomaticAssessments auto=new AutomaticAssessments();
		try {
			auto.create(args[0],args[1],args[2]);
		}	
		catch (SQLException e) {
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {		
			e.printStackTrace();
		}
		catch (ParseException e) {		
			e.printStackTrace();
		}		
	}

	
	/**
	 * Create automatic asssessments
	 * @param database database string
	 * @param username database username 
	 * @param password database password
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void create(String database, String username, String password) throws IOException, SQLException, ClassNotFoundException, ParseException {
		// connect database
		op = new SqlOperations();
		op.connect(database,username,password);	
		
		// get assessments
		Vector<Assessment> assessments=op.selectQueryAssessments(Assessment.GROUND_TRUTH_USER,Assessment.ASSESSMENT_TYPE_MANUAL); 			
		
		// get versions for each url assessed for a topic
		for (int i=0;i<assessments.size();i++) {
			Assessment assessment=assessments.get(i);
			if (assessment.getRelevance()!=Assessment.RATE_IGNORE) {						
				processAssesssment(assessment);
			}					
		}				
		
		// close connection		
		op.close();		
	}
	
	/**
	 * Insert automatic assessment for navigational queries  
	 * TODO for informational queries
	 * @param assessment
	 */
	private void processAssesssment(Assessment assessment) throws IOException, ParseException, SQLException {
		
		String url=URL_QUERY+assessment.getDocUrl(); 
		if (assessment.getTopicPeriodStart()!=null && !assessment.getTopicPeriodStart().equals("")) {			
			if (assessment.getTopicPeriodEnd()==null || assessment.getTopicPeriodEnd().equals("")) { // sanity check
				throw new IOException("end date restriction missing!");
			}
									
			String encodedPeriodStart = convertArchiveQuery(assessment.getTopicPeriodStart());		
			String encodedPeriodEnd = convertArchiveQuery(assessment.getTopicPeriodEnd());						
			url+="&dateStart="+encodedPeriodStart+"&dateEnd="+encodedPeriodEnd;																		
		}			
				
		System.out.println(url);
		
		Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();				
		Elements li = doc.select("table[class=tabela-principal] > tbody > tr > td > a[href]");
		//Elements li = doc.select("td[class=mainBody] > a[href]");		
		for (int i=0; i<li.size(); i++) {
			Element l=li.get(i);	
			String archiveUrl=l.attr("abs:href");
			archiveUrl=archiveUrl.substring(0,archiveUrl.indexOf("?"));					
			String docCode=archiveUrl.substring(archiveUrl.lastIndexOf("/")+1,archiveUrl.length());
			String docDate=l.text();
			int topicNumber=assessment.getTopicNumber();
									
			// see if doc exists, otherwise create it
			Date date=formatterTimestamp.parse(docDate);									
			//int docid=op.selectDocId(assessment.getDocUrl(),new Timestamp(date.getTime())); TODO remove			
			int docid=op.selectDocIdPerCode(docCode);
			//System.out.println("docid:"+docid+" "+assessment.getDocUrl()+" "+docDate);			
			if (docid==-1) {
				docid=op.selectDocMax()+1;	
				op.insertDoc(docid,assessment.getDocUrl(),new Timestamp(date.getTime()),archiveUrl,docCode);			
			}
					
			// create querydoc
			int qiddocid=op.selectQueryDocId(topicNumber,docid);
			//System.out.println("qiddocid:"+qiddocid+" "+topicNumber+" "+docid);						
			if (qiddocid==-1) {
				qiddocid=op.selectQueryDocMax()+1;	
				op.insertQueryDoc(qiddocid,topicNumber,docid,"");								
				op.insertAssessment(qiddocid, Assessment.GROUND_TRUTH_USER, assessment.getRelevance(), null, Assessment.ASSESSMENT_TYPE_AUTOMATIC);						
			}
			else {
				if (assessment.getQuerydocId()!=qiddocid) { // sanity check
					//throw new IOException("Different querydoc: "+assessment.getQuerydocId()+" "+qiddocid); this happen for the manually changed
					System.err.println("Different querydoc: "+assessment.getQuerydocId()+" "+qiddocid);
				}				
			}
		}		
	}
	
	private String convertArchiveQuery(String s) throws UnsupportedEncodingException{
		return URLEncoder.encode(s,"UTF-8");				
	}
}
