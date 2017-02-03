package pt.arquivo.assessments;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;


/**
 * Creates a qrels files
 * @author Miguel Costa
 */
public class QrelsGenerator {		
	
	public QrelsGenerator() {		
	}
	
	/**
	 * Create a qrels file from assessments stored in database
	 * @param database database string
	 * @param username database username 
	 * @param password database password
	 * @param assessmentsType type of assessment: manual or all
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void qrels(String database, String username, String password, String assessmentsType) throws IOException, SQLException, ClassNotFoundException {
		// connect database
		SqlOperations op = new SqlOperations();
		op.connect(database,username,password);	
		
		// get assessments
		Vector<Assessment> assessments=null;
		if (assessmentsType.equals("all")) {
			assessments=op.selectQueryAssessments(Assessment.GROUND_TRUTH_USER);
		}
		else if (assessmentsType.equals("manual")) {
			assessments=op.selectQueryAssessments(Assessment.GROUND_TRUTH_USER,Assessment.ASSESSMENT_TYPE_MANUAL);
		}
		else if (assessmentsType.equals("automatic")) {
			assessments=op.selectQueryAssessments(Assessment.GROUND_TRUTH_USER,Assessment.ASSESSMENT_TYPE_AUTOMATIC);
		}
		
		// close connection		
		op.close();	

		if (assessments==null) {
		        System.out.println("Wrong option "+assessmentsType);
			return;
		}
		
		// write assessments in TREC format
		for (int i=0;i<assessments.size();i++) {
			Assessment assess=assessments.get(i);
			if (assess.getRelevance()!=Assessment.RATE_IGNORE) { 				
				if (assessmentsType.equals("manual") && assess.getRelevanceType()!=Assessment.ASSESSMENT_TYPE_MANUAL) { // sanity check
					throw new IOException("Wrong relevance type.");
				}
				
				System.out.println(assess.getTopicNumber()+" 0 "+assess.getDocCode()+" "+assess.getRelevance());				
			}
		}
	}
	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length!=4) {
			System.out.println("usage: <database> <username> <password> <manual|automatic|all>");
		}
				
		QrelsGenerator gen=new QrelsGenerator();
		try {			
			gen.qrels(args[0],args[1],args[2],args[3]);
		}	
		catch (SQLException e) {
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {		
			e.printStackTrace();
		}		
	}

}
