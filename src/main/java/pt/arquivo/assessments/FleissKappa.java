package pt.arquivo.assessments;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;


/**
 * Generated matrix to compute Fleiss Kappa
 * @author Miguel Costa
 */
public class FleissKappa {
		
	
	public FleissKappa() {		
	}
	
	/**
	 * Create matrix to compute Fleiss Kappa
	 * @param database database string
	 * @param username database username 
	 * @param password database password	
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void kappa(String database, String username, String password) throws IOException, SQLException, ClassNotFoundException {
		// connect database
		SqlOperations op = new SqlOperations();
		op.connect(database,username,password);	
		
		int counterQuerydoc=0;
		int lastQuerydoc=0;
		int counterRate=-1;
		ResultSet results=op.selectQueryAssessmentsQuerydocRate();
		if (results!=null) {
			while (results.next()) {
				int querydoc=results.getInt(1);
				int rate=results.getInt(2);
				int count=results.getInt(3);
				
				//sanity check				
				if (/*counterQuerydoc!=querydoc*/ lastQuerydoc>querydoc || counterRate!=rate) {
					throw new IOException("Wrong value: "+counterQuerydoc+" "+querydoc+" "+counterRate+" "+rate);
				}				
				System.out.print(count+" ");
								
				counterRate++;
				if (counterRate==3) {
					counterRate=-1;
					counterQuerydoc++;
					System.out.println();	
				}
				lastQuerydoc=querydoc;
			}
			results.close();  		      
		} 		
				
		// close connection		
		op.close();		
				
	}
	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length!=3) {
			System.out.println("usage: <database> <username> <password>");
		}
				
		FleissKappa gen=new FleissKappa();
		try {			
			gen.kappa(args[0],args[1],args[2]);
		}	
		catch (SQLException e) {
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {		
			e.printStackTrace();
		}		
	}

}
