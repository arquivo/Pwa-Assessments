package pt.arquivo.assessments;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class MigrateCodeDocs {

	private SqlOperations op = null;
	public MigrateCodeDocs() {}
	private final String DOMAIN = "http://p18.arquivo.pt/wayback/";
	private final String urlBarOP = "/";
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException , ParseException {
		if ( args.length!=3 ) {
			System.out.println("usage: <database> <username> <password>");
			System.exit(1);
		}
				
		MigrateCodeDocs migrate=new MigrateCodeDocs( );		
		migrate.processRuns( args[0] , args[1] , args[2] );		
	}
	
	private void processRuns( String database , String username , String password ) throws ParseException {
	    Vector< Doc > docs;
	    String oldFormat = "yyyy-MM-dd HH:mm:ss";
	    String newFormat = "yyyyMMddHHmmss";
	    String newTimestampFormat;
	    String newurlarchived;
	    String newCode;
	    
		try{
			op = new SqlOperations( );
			op.connect( database , username , password );	
			docs = op.selectDocs( );
			
			for( Doc doc : docs ) {
				//System.out.println( "["+doc.getId()+"] ["+doc.getUrl()+"] ["+doc.getDate()+"] ["+doc.getUrlarchived()+"] ["+doc.getCode()+"]" );
				DateFormat formatter = new SimpleDateFormat( oldFormat );
				Date oldFormatedDate = formatter.parse( doc.getDate().toString() );
				newTimestampFormat = new SimpleDateFormat( newFormat ).format( oldFormatedDate );
				newurlarchived = DOMAIN.concat( newTimestampFormat ).concat( urlBarOP ).concat( doc.getUrl( ) );
				newCode = newTimestampFormat.concat( urlBarOP ).concat( doc.getUrl( ) );
				System.out.println("[" + doc.getId( ) + "][" + doc.getDate( ) + "][Updating] Tstamp = " + newTimestampFormat + " url = " + doc.getUrl() + " newurlarchived = " + newurlarchived + " newCode = " + newCode );
				try {
					op.updateDocs( newurlarchived , newCode , doc.getId( ) );
				} catch( SQLException e ) {
					e.printStackTrace( );
				}
			}
			
		} catch( SQLException e ) {
			e.printStackTrace( );
			return;
		}
	}
	
	
}
