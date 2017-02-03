package pt.arquivo.assessments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;


/**
 * Normalize feature datasets
 * @author Miguel Costa
 */
public class Normalize {		
	
	private final static int NFIELDS=72;
	private final static int NFEATURES=68;
		
	
	public Normalize() {		
	}
		
	/**
	 * Normalize
	 * @param file file
	 * @throws IOException
	 */
	public void process(String file) throws IOException {
										
		// read run and filter run and qrels for versions of the same documents
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));			
		Vector<ArrayList<Float>> features=new Vector<ArrayList<Float>>();
		
		// init lists
		for (int i=0;i<NFEATURES;i++) {
	    	features.add(new ArrayList<Float>());
	    }
		
		String line=null;
		while ( ( line = br.readLine() ) != null) {
		    String parts[] = line.split( "\\s" );
		    // sanity check
		    if (parts.length!=NFIELDS) { 
		    	throw new IOException("ERROR: wrong number of fields.");
		    }
			
		    for (int i=0;i<NFEATURES;i++) {
		    	String fparts[]=parts[i+2].split( ":" );
		    	features.get(i).add( Float.parseFloat(fparts[1]) );
		    }
		}
		br.close();

		// compute avg and std
		float avg[]=new float[NFEATURES];
		float std[]=new float[NFEATURES];
		for (int i=0;i<NFEATURES;i++) {
		    avg[i]=mean(features.get(i));
		    std[i]=sd(features.get(i));
		    
		    //System.out.println(avg[i]+" "+std[i]);
		}

		br = new BufferedReader(new FileReader(new File(file)));			
		PrintWriter pw=new PrintWriter(new File(file+".normalized"));								
		while ( ( line = br.readLine() ) != null) {
			String parts[] = line.split( "\\s" );
			
		    pw.print(parts[0]+" "+parts[1]+" ");
		    for (int i=0;i<NFEATURES;i++) {
		    	String fparts[]=parts[i+2].split( ":" );
		    	float f=Float.parseFloat(fparts[1]);
		    	float newF=(f-avg[i])/std[i];
		    	pw.print(fparts[0]+":"+newF+" ");
		    }
		    pw.println(parts[NFEATURES+2]+" "+parts[NFEATURES+3]);
		}
		pw.flush();
		pw.close();
		br.close();						
	}
		

    /**
     * Sum all list elements
     */
	public float sum (List<Float> a){
		float sum = 0;
		for (Float f : a) {
			sum += f;
        }
		return sum;
	}
	    
    /**
     * Mean of all list elements
     */
    public float mean (List<Float> a) {
      	float sum = sum(a);
       	float mean = sum / (a.size() * 1.0f);
       	return mean;
    }

    /**
     * Standard deviation of all list elements
     */
    public float sd (List<Float> a) {
    	float sum = 0;
    	float mean = mean(a);
    	
    	for (Float f : a) {
    		sum += Math.pow((f - mean), 2);
    	}
    	return (float)Math.sqrt( sum / ( a.size() - 1 ) ); // sample deviation
    }	

 	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length!=1) {
		    System.out.println("usage: <file feature dataset>");
		    System.exit(1);
		}
				
		Normalize norm=new Normalize();		
		norm.process(args[0]);		
	}

}
