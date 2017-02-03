package pt.arquivo.assessments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;


/**
 * Add binary features to datasets according the timestamp of a version
 * @author Miguel Costa
 */
public class DatasetTemporalBinaryFeatures {		
	
	private final static int NFIELDS=72; /* number of fields in the dataset */
	private final static int NFEATURES=68;
	private final static int MIN_YEAR=1996; /* minimum year in the corpus */
	private final static int MAX_YEAR=2009; /* maximum year in the corpus */
	//private final static int NYEARS_INTERVAL=MAX_YEAR-MIN_YEAR+1; /* corpus interval */
	public final static float DAY_MILLISEC =  24 * 60 * 60 * 1000; /* milliseconds in a day */
	private final static int MONTHS=12;
	
	private int syear[];
	private int eyear[];
	private int smonth[];
	private int emonth[];
	
	
	public DatasetTemporalBinaryFeatures() {		
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
			
		syear=new int[nSplits];
		eyear=new int[nSplits];
		smonth=new int[nSplits];
		emonth=new int[nSplits];
		
		// check
		int months=0;
		if ((MAX_YEAR-MIN_YEAR+1)%nSplits != 0) {
			float mod=(MAX_YEAR-MIN_YEAR+1)%nSplits;
			
			if (mod/(float)nSplits == 0.5) {
				months=6;
			}
			else if (mod/(float)nSplits == 0.25) {
				months=3;
			}
			else {
				
				System.out.println((MAX_YEAR-MIN_YEAR+1)%nSplits);
				System.out.println(months);
				throw new IOException("Can not split datasets with equal years using "+nSplits);
			}
		}
		int interval=(MAX_YEAR-MIN_YEAR+1)/nSplits;				
		
		// print splits information		
		int auxmonths=0;
		int auxyears=0;
		for (int i=0;i<nSplits;i++) {
			//int min=MIN_YEAR + interval*i;
			//int max=MIN_YEAR + interval*(i+1) -1;
			//System.out.println("Add features to "+(i+1)+" "+min+" "+max);
			
			int min=MIN_YEAR + interval*i;
			int max=MIN_YEAR + interval*(i+1) -1;			
									
			int startyear=min+auxyears;						
			int startmonth=auxmonths;
			if (auxmonths==0) {
				startmonth=1;
			}	
			else {
				startmonth++;
			}
							
			auxmonths+=months;					
			if (auxmonths==MONTHS) {
				auxmonths=0;				
				auxyears++;
			}
			int endmonth=auxmonths;
			if (auxmonths==0) {
				endmonth=MONTHS;
			}
			
			int endyear=max+auxyears;
			if (auxmonths>0) {
				endyear++;
			}			
			
			System.out.println("Add features to "+(i+1)+" "+startyear+","+startmonth+" "+endyear+","+endmonth);		
						
			syear[i]=startyear;
			eyear[i]=endyear;
			smonth[i]=startmonth;
			emonth[i]=endmonth;
						
			//System.out.println(getDatasetNumber(syear[i],smonth[i])); check
			//System.out.println(getDatasetNumber(eyear[i],emonth[i])); check
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
			int month=cal.get(Calendar.MONTH);	
			//int idataset=(year-MIN_YEAR)/interval;
			int idataset=getDatasetNumber(year,month);
			
			// write dataset instance for this version
			line = br.readLine(); // read the normalized file
			parts = line.split( "\\s" );					   
			pw.print(parts[0]+" "+parts[1]+" ");
									
			int k=1;
			for (int i=0;i<NFEATURES;i++) {			
				String fparts[]=parts[i+2].split( ":" );		    	
				pw.print(k+":"+fparts[1]+" ");				
				k++;
			}
			if (nSplits>1) {
				for (int j=0;j<nSplits;j++) {
					if (j==idataset) {
						pw.print(k+":1 ");
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
	 * Returns the number of the dataset given a year and a month
	 * @param year
	 * @param month
	 * @return
	 */
	private int getDatasetNumber(int year, int month) throws IOException {
		for (int i=0; i<syear.length; i++) {
			
			Date mydate= new Date(year - 1900, month - 1, 1);
			Date sdate= new Date(syear[i] - 1900, smonth[i] - 1, 1);
			Date edate= new Date(eyear[i] - 1900, emonth[i] - 1, 1);
			
			if ((mydate.after(sdate) || mydate.equals(sdate)) && (mydate.before(edate) || mydate.equals(edate))) {
				return i;
			}
		}
			
		throw new IOException("Wrong dataset for year "+year+" and month "+month);		
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
				
		DatasetTemporalBinaryFeatures dup=new DatasetTemporalBinaryFeatures();
		dup.process(args[0],args[1],Integer.parseInt(args[2]));
	}

}
