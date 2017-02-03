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
 * Adds ranking features for all assessments
 * @author Miguel Costa
 */
public class FeaturesGenerator {
	
	private final static int NUM_FEATURES=68;
	private final static String DOMAIN="p18.arquivo.pt"; //p15
	private final static String URL_EXPLAIN="http://"+DOMAIN+"/explain.jsp?query=";		
	private final static int TIMEOUT=120 * 1000; // 2 minutes
		
	private SqlOperations op=null;
	private String allFunctions=null;
	private String allBoosts=null;

	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length!=4) {
			System.out.println("usage: <database> <username> <password> <manual|automatic|all>");			
		}
				
		FeaturesGenerator auto=new FeaturesGenerator();
		try {
			auto.create(args[0],args[1],args[2],args[3]);
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
	 * Create ranking features from asssessments
	 * @param database database string
	 * @param username database username 
	 * @param password database password
	 * @param assessmentsType type of assessment: manual or all
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void create(String database, String username, String password, String assessmentsType) throws IOException, SQLException, ClassNotFoundException, ParseException {
		
		// functions and boosts to call explain.jsp
		allFunctions=new String();
		allBoosts=new String();
		for (int i=0;i<NUM_FEATURES;i++) {
			if (i==0) {
				allFunctions+=i;
				allBoosts+="1";
			}
			else {
				allFunctions+="+"+i;
				allBoosts+="+"+"1";
			}
		}
		
		// connect database
		op = new SqlOperations();
		op.connect(database,username,password);	
		
		// get assessments
		Vector<Assessment> assessments=op.selectQueryAssessments(Assessment.GROUND_TRUTH_USER); 			
		
		// get versions for each url assessed for a topic
		for (int i=0;i<assessments.size();i++) {
			Assessment assessment=assessments.get(i);
			//if (assessment.getRelevance()!=Assessment.RATE_IGNORE) {	
			if (assessment.getRelevance()!=Assessment.RATE_IGNORE) {				
				if (assessmentsType.equals("manual") && assessment.getRelevanceType()==Assessment.ASSESSMENT_TYPE_MANUAL) {
					processAssesssment(assessment);
				}
				else if (assessmentsType.equals("automatic") && assessment.getRelevanceType()==Assessment.ASSESSMENT_TYPE_AUTOMATIC) {
					processAssesssment(assessment);
				}
				else if (assessmentsType.equals("all")) {
					processAssesssment(assessment);
				}
			}					
		}				
		
		// close connection		
		op.close();		
	}
	
	/**
	 * Insert features for navigational queries  
	 * @param database database string
	 * @param username database username 
	 * @param password database password
	 * @param assessmentsType type of assessment: manual or all
	 * 		
	 * TODO for informational queries
	 * @param assessment
	 */
	private void processAssesssment(Assessment assessment) throws IOException, ParseException, SQLException {
							
		String encodedQuery = convertArchiveQuery(assessment.getTopicQuery());						
		String url=URL_EXPLAIN+encodedQuery+"&sfunctions="+allFunctions+"&sboosts="+allBoosts;
		String docCode=assessment.getDocCode();
		// example of docCode: id982index5
		String idx=docCode.substring(docCode.indexOf("index")+5);
		String id=docCode.substring(2,docCode.indexOf("index"));

		url+="&idx="+idx+"&id="+id;																									
		
		// connect to explain.jsp and collect feature values
		Document docExplain = Jsoup.connect(url).timeout(TIMEOUT).get(); 										
		Element lexplain = docExplain.select("span[class=features]").first();										
		String lExplainFeatures = (lexplain!=null ? lexplain.text() : null); // extract features																		
									
		String features=op.selectQueryDocFeatures(assessment.getQuerydocId()); // get features already stored in database
		//if (features==null || features.equals("")) {
		if (lExplainFeatures==null || lExplainFeatures.equals("")) {
			System.err.println("No features for querydoc "+assessment.getQuerydocId()+" new: "+lExplainFeatures+" URL: "+url);			
		}
		else {		
			System.out.println(assessment.getRelevance()+" qid:"+assessment.getTopicNumber()+" "+lExplainFeatures+" # "+docCode);
		}			
		
		/* cannot check this. All temporal features based on query time will be different.
		if (!features.equals(lExplainFeatures)) { 
			throw new IOException("Features are different: NEW: "+features+" OLD: "+lExplainFeatures);
		}
		*/
	}
	
	/**
	 * Encode query
	 * @param s query
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String convertArchiveQuery(String s) throws UnsupportedEncodingException{
		return URLEncoder.encode(s,"UTF-8");				
	}
}
