package pt.arquivo.assessments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;


/**
 * Splits feature datasets by time
 * @author Miguel Costa
 */
public class DatasetTemporalSplitter {		
	
	private final static int NFIELDS=72; /* number of fields in the dataset */
	private final static int MIN_YEAR=1996; /* minimum year in the corpus */
	private final static int MAX_YEAR=2009; /* maximum year in the corpus */
	//private final static int NYEARS_INTERVAL=MAX_YEAR-MIN_YEAR+1; /* corpus interval */
	public final static float DAY_MILLISEC =  24 * 60 * 60 * 1000; /* milliseconds in a day */
	
	
	public DatasetTemporalSplitter() {		
	}
			
	/**
	 * Split dataset
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
		PrintWriter pw[]=new PrintWriter[nSplits];
		for (int i=0;i<nSplits;i++) {
			int min=MIN_YEAR + interval*i;
			int max=MIN_YEAR + interval*(i+1) -1;
			System.out.println("Split "+(i+1)+" "+min+" "+max);
			
			pw[i]=new PrintWriter(new File(file+".split"+(i+1)));
		}
				
		BufferedReader brTimestamp = new BufferedReader(new FileReader(new File(timestampFile)));
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line;									
		while ( ( line = brTimestamp.readLine() ) != null ) {				
			String parts[] = line.split( "\\s" );			
			
			if (parts.length!=NFIELDS) { 
				throw new IOException("ERROR: wrong number of fields "+parts.length);
			}
			
			float timestampInDays=Float.parseFloat(parts[60].substring(3));					
			long timestamp = (long)(((float)timestampInDays) * DAY_MILLISEC);
			
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp);			
			int year=cal.get(Calendar.YEAR);			
			int idataset=(year-MIN_YEAR)/interval;
			
			line = br.readLine(); // read the normalized file
			pw[idataset].println(line);
//			System.out.println(parts[60]+" "+timestampInDays+" "+timestamp+" "+year+" "+parts[71]+" "+idataset);					
		}						
								
		// close
		brTimestamp.close();
		br.close();
		for (int i=0;i<nSplits;i++) {
			pw[i].flush();
			pw[i].close();
		}							
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
				
		DatasetTemporalSplitter dup=new DatasetTemporalSplitter();
		dup.process(args[0],args[1],Integer.parseInt(args[2]));
	}

}
