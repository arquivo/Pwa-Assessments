package pt.arquivo.assessments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.archive.access.nutch.jobs.EntryPageExpansion;


/**
 * Removes duplicates from runs and qrels files
 * @author Miguel Costa
 */
public class DuplicatesPerQuery {		
	
	private final static int NFIELDS_IDURL=4;
	private final static int NFIELDS_RUNS=6;
	
	private Hashtable<String,String> idUrlRadical = new Hashtable<String,String>();
	private Vector<Qrels> vqrels=new Vector<Qrels>();
	
	
	public DuplicatesPerQuery() {		
	}
	
	/**
	 * Read duplicates file and create data structures in memory
	 * @param fIdUrl file that maps ids in urls
	 * @throws IOException	 
	 */
	public void readIdUrl(String fIdUrl) throws IOException {
			
		BufferedReader br = new BufferedReader(new FileReader(new File(fIdUrl)));
		String line;		
		Vector<String> idsAux=null;

		while ( ( line = br.readLine() ) != null ) {				
			String parts[] = line.replaceAll("\\s+", " ").split( "\\s" );			
			
			if (parts.length!=NFIELDS_IDURL) { 
				throw new IOException("ERROR: wrong number of fields "+parts.length+". "+line);
			}
			
			String id=parts[1];
			String url=parts[3];
			String urlRadical=EntryPageExpansion.getRadical(url);			
			idUrlRadical.put(new String(id), new String(urlRadical));		
		}					
		br.close();				
	}
	
	/**
	 * Read qrels from each query
	 * @param fQrels qrels file
	 * @throws IOException
	 */
	public void readQrels(String fQrels) throws IOException {
				
		BufferedReader brQrels = new BufferedReader(new FileReader(new File(fQrels)));			
				
		String oldQuery=null;
		Qrels qrels=null;
		String line;	
		while ( ( line = brQrels.readLine() ) != null ) {						
			String parts[] = line.split( "\\s" );	
			
			String query=parts[0];
			if (oldQuery==null || !query.equals(oldQuery)) {
				
				if (oldQuery!=null) {
					vqrels.add(qrels);
				}
				qrels=new Qrels(query);									
			}
			
			qrels.addEntry(new String(line));
			oldQuery=query;
		}		
		vqrels.add(qrels);
		brQrels.close();		
	}
	
	/**
	 * Print qrels for each query
	 * @param fQrels qrels file
	 * @throws IOException
	 */
	public void printQrels(String fQrels) throws IOException {
		// print qrels file
		PrintWriter pw=new PrintWriter(new File(fQrels+".filtered"));
		
		for (int i=0; i<vqrels.size(); i++) {
			Vector<String> entries=vqrels.get(i).getEntries();
			for (int j=0; j<entries.size(); j++) {	
				pw.println(entries.get(j));
			}
		}
		
		pw.flush();
		pw.close();	
	}
	
	/**
	 * Erase qrels with duplicated versions
	 * @param query query
	 * @param idsUsed docids used 
	 * @param duplicates url radicals used
	 * @param idUrlRadical structure with the id-urlRadical map
	 */
	private void eraseDup(String query, Vector<String> idsUsed, Set<String> duplicates, Hashtable<String, String> idUrlRadical) throws IOException { 
		int i=0;
		while (!vqrels.get(i).getQid().equals(query)) {
			i++;
		}
		// sanity check
		if (!vqrels.get(i).getQid().equals(query)) {
			throw new IOException("Wrong query.");
		}
		vqrels.get(i).eraseDup(idsUsed,duplicates,idUrlRadical);
	}
	
	/**
	 * Create runs without duplicates
	 * @param fRuns file with search runs
	 * @param fQrels file with qrels  
	 * @throws IOException
	 */
	public void processRuns(String fRuns, String fQrels) throws IOException {
										
		// read run and filter run and qrels for versions of the same documents
		BufferedReader brRuns = new BufferedReader(new FileReader(new File(fRuns)));			
		PrintWriter pwRuns=new PrintWriter(new File(fRuns+".filtered"));
				
		Set<String> duplicates=null;
		Vector<String> idsUsed=null;
		String oldQuery=null;
		String line;			
		
		while ( ( line = brRuns.readLine() ) != null ) {				
			String parts[] = line.split( "\\s" );			
			
			if (parts.length!=NFIELDS_RUNS) { 
				throw new IOException("ERROR: wrong number of fields.");
			}
			
			String query=parts[0];
			String id=parts[2];			
			String docUrlRadical=idUrlRadical.get(id);
			
			if (oldQuery==null || !query.equals(oldQuery)) {
				if (oldQuery!=null) {
				    System.out.println("query "+oldQuery);					
				    eraseDup(oldQuery,idsUsed,duplicates,idUrlRadical);
				}
				duplicates=new HashSet<String>();	
				idsUsed=new Vector<String>();
			}			
			if (!duplicates.contains(docUrlRadical)) {  // ignore the versions of the same document				
				pwRuns.println(line); // I can ignore the rank field because the order is maintained after discarding entries
				duplicates.add(docUrlRadical);
				idsUsed.add(id);
			}			
									
			oldQuery=query;		
		}					
		System.out.println("query "+oldQuery);
		eraseDup(oldQuery,idsUsed,duplicates,idUrlRadical);
		brRuns.close();		
		
		pwRuns.flush();
		pwRuns.close();					
	}
		
 	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length!=3) {
			System.out.println("usage: <file id-url> <file runs> <file qrels>");
			System.exit(1);
		}
				
		DuplicatesPerQuery dup=new DuplicatesPerQuery();
		dup.readIdUrl(args[0]);
		dup.readQrels(args[2]);
		dup.processRuns(args[1],args[2]);
		dup.printQrels(args[2]);
	}

}
