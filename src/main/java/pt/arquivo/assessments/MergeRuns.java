package pt.arquivo.assessments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Merge runs and rerank entries
 * @author Miguel Costa
 */
public class MergeRuns {		
	
	private final static int NFIELDS_RUNS=6;
		
	
	public MergeRuns() {		
	}
	
	/**
	 * Print to file the entry with new rank
	 * @param pwRunMerged PrintWriter
	 * @param parts entry parts
	 * @param rank new rank for this entry
	 */
	private void print(PrintWriter pwRunMerged, String parts[], int rank) {
		pwRunMerged.println(parts[0]+" "+parts[1]+" "+parts[2]+" "+rank+" "+parts[4]+" "+parts[5]);		
	}
	
	/**
	 * Create runs without duplicates
	 * @param fRun1 file with search runs
	 * @param fRun2 file with search runs  
	 * @throws IOException
	 */
	public void processRuns(String fRun1, String fRun2) throws IOException {
										
		// read run and filter run and qrels for versions of the same documents
		BufferedReader brRun1 = new BufferedReader(new FileReader(new File(fRun1)));			
		BufferedReader brRun2 = new BufferedReader(new FileReader(new File(fRun2)));		
		PrintWriter pwRunMerged=new PrintWriter(new File(fRun1+".merged"));						
		
		String oldQuery=null;
		int rank=1;		
		boolean isAddedLine1=false;
		boolean isAddedLine2=false;
		String line1 = brRun1.readLine();
		String line2 = brRun2.readLine();
		String parts1[] = line1.split( "\\s" );
		String parts2[] = line2.split( "\\s" );

		do 
		{												
			// sanity check
			if (parts1.length!=NFIELDS_RUNS) { 
				throw new IOException("ERROR: wrong number of fields.");
			}								
			// sanity check				
			if (parts2.length!=NFIELDS_RUNS) { 
				throw new IOException("ERROR: wrong number of fields.");
			}
			
			String query1=parts1[0];
			String query2=parts2[0];				
			
			// reset rank
			if (oldQuery==null || (!query1.equals(oldQuery) && !query2.equals(oldQuery))) { 
				rank=1;
			}
			
			if (query1.equals(query2)) {
				float score1=Float.parseFloat(parts1[4]);
				float score2=Float.parseFloat(parts2[4]);	
				
				if (score1>=score2) {
					print(pwRunMerged, parts1, rank);									
					isAddedLine1=true;
					oldQuery=query1;
				}
				else {
					print(pwRunMerged, parts2, rank);					
					isAddedLine2=true;
					oldQuery=query2;
				}	
			}
			else { 
				if (oldQuery!=null && (query1.equals(oldQuery) || query2.equals(oldQuery))) { // if at least one has the same query, then use it
					if (query1.equals(oldQuery)) {						
						print(pwRunMerged, parts1, rank);
						isAddedLine1=true;		
						oldQuery=query1;	
					}
					else {
						print(pwRunMerged, parts2, rank);						
						isAddedLine2=true;
						oldQuery=query2;
					}
				}
				else { // queries different and different from old query			
					print(pwRunMerged, parts1, rank);
					isAddedLine1=true;		
					oldQuery=query1;
				}
			}
			
			rank++;
						
			if (isAddedLine1) { // get new line
				isAddedLine1=false;
				line1 = brRun1.readLine();
				if (line1!=null) {
					parts1 = line1.split( "\\s" );
				}
			}
			if (isAddedLine2) { // get new line
				isAddedLine2=false;
				line2 = brRun2.readLine();
				if (line2!=null) {
					parts2 = line2.split( "\\s" );
				}
			}
		}
		while (line1!=null && line2!=null);
		
		// write the remaining entries
		if (line1==null && line2!=null) {				
			do {
				parts2 = line2.split( "\\s" );
				// sanity check				
				if (parts2.length!=NFIELDS_RUNS) { 
					throw new IOException("ERROR: wrong number of fields.");
				}	
				
				String query2=parts2[0];	
				if (!query2.equals(oldQuery)) {
					rank=1;
				}
			
				print(pwRunMerged, parts2, rank);
				rank++;
				oldQuery=parts2[0];	
			}
			while ( ( line2 = brRun2.readLine() ) != null );		
		}
		else if (line2==null && line1!=null) {
			do {			
				parts1 = line1.split( "\\s" );
				// sanity check
				if (parts1.length!=NFIELDS_RUNS) { 
					throw new IOException("ERROR: wrong number of fields.");
				}
				
				String query1=parts1[0];	
				if (!query1.equals(oldQuery)) {
					rank=1;
				}
			
				print(pwRunMerged, parts1, rank);
				rank++;
				oldQuery=parts1[0];
			}
			while ( ( line1 = brRun1.readLine() ) != null );			
		}
		
		brRun1.close();
		brRun2.close();	
		pwRunMerged.flush();
		pwRunMerged.close();						
	}
		
 	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length!=2) {
			System.out.println("usage: <file run1> <file run2>");
			System.exit(1);
		}
				
		MergeRuns merge=new MergeRuns();		
		merge.processRuns(args[0],args[1]);		
	}

}
