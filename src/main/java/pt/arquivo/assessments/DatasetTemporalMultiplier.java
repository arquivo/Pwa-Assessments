package pt.arquivo.assessments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;


/**
 * Multiply features of datasets according the timestamp of a version
 * @author Miguel Costa
 */
public class DatasetTemporalMultiplier {		
	
	private final static int NFIELDS=72; /* number of fields in the dataset */
	private final static int NFEATURES=68;
	private final static int MIN_YEAR=1996; /* minimum year in the corpus */
	private final static int MAX_YEAR=2009; /* maximum year in the corpus */
	//private final static int NYEARS_INTERVAL=MAX_YEAR-MIN_YEAR+1; /* corpus interval */
	public final static float DAY_MILLISEC =  24 * 60 * 60 * 1000; /* milliseconds in a day */
	
	
	public DatasetTemporalMultiplier() {		
	}
			
	/**
	 * Add features
	 * @param file dataset file (normalized)
	 * @param timestampFile dataset file with timestamp (not normalized)
	 * @param nSplits number of splits
	 * @note @file and @timestampFile have the same entries (one normalized and the other not) 
	 * @throws IOException
	 */
	public void process(String file, String timestampFile, int nSplits) throws IOException {
					
		// check
		if ((MAX_YEAR-MIN_YEAR+1)%nSplits != 0) {
			throw new IOException("Can not split datasets with equal years using "+nSplits);
		}
		int interval=(MAX_YEAR-MIN_YEAR+1)/nSplits;				
		
		// print splits information				
		for (int i=0;i<nSplits;i++) {
			int min=MIN_YEAR + interval*i;
			int max=MIN_YEAR + interval*(i+1) -1;
			System.out.println("Multiplied features for "+(i+1)+" "+min+" "+max);
		}
		
		// process file
		PrintWriter pw=new PrintWriter(new File(file+".multiplied"));
		BufferedReader brTimestamp = new BufferedReader(new FileReader(new File(timestampFile)));
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line;									
		while ( ( line = brTimestamp.readLine() ) != null ) {				
			String parts[] = line.split( "\\s" );			
			
			if (parts.length!=NFIELDS) { 
				throw new IOException("ERROR: wrong number of fields "+parts.length);
			}
			
			// get version timestamp
			float timestampInDays=Float.parseFloat(parts[60].substring(3));					
			long timestamp = (long)(((float)timestampInDays) * DAY_MILLISEC);			
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp);			
			int year=cal.get(Calendar.YEAR);			
			int idataset=(year-MIN_YEAR)/interval;
			
			// write dataset instance for this version
			line = br.readLine(); // read the normalized file
			parts = line.split( "\\s" );					   
			pw.print(parts[0]+" "+parts[1]+" ");
			
			int k=1;
			for (int j=0;j<nSplits;j++) {			
				for (int i=0;i<NFEATURES;i++) {
					
					if (j==idataset) {
						String fparts[]=parts[i+2].split( ":" );		    	
						pw.print(k+":"+fparts[1]+" ");
					}
					else {
						pw.print(k+":0 ");
					}
					
			    	k++;
			    }								
			}
					    		    		    		    		
		    pw.println(parts[NFEATURES+2]+" "+parts[NFEATURES+3]);																				
		}						
								
		// close
		brTimestamp.close();
		br.close();
		pw.flush();
		pw.close();									
	}
		
 	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length!=3) {
			System.out.println("usage: <dataset file> <dataset file (not normalized)> <# splits>");
			System.exit(1);
		}
				
		DatasetTemporalMultiplier dup=new DatasetTemporalMultiplier();
		dup.process(args[0],args[1],Integer.parseInt(args[2]));
	}

}
