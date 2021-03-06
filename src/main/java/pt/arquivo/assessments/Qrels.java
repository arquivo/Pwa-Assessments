package pt.arquivo.assessments;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;


/**
 * Encapsulates qrels of a query
 * @author Miguel Costa
 */
public class Qrels {

	private String qid;
	private Vector<String> entries=new Vector<String>(); 
	
	public Qrels(String qid) {
		this.qid=qid;
	}
	
	public String getQid() {
		return qid;
	}

	public void setQid(String qid) {
		this.qid = qid;
	}

	public Vector<String> getEntries() {
		return entries;
	}

	public void setEntries(Vector<String> entries) {
		this.entries = entries;
	}
	
	public void addEntry(String entry) {
		this.entries.add(entry);
	}
	
	/**
	 * Erase versions of the same document (considered duplicated for navigational queries)
	 * @param idsUsed docids used 
	 * @param duplicates url radicals used
	 * @param idUrlRadical structure with the id-urlRadical map
	 */
	public void eraseDup(Vector<String> idsUsed, Set<String> duplicates, Hashtable<String, String> idUrlRadical) {
		
		// erase all versions duplicated from the versions selected in the run
		for (int j=0; j<idsUsed.size(); j++) {
				String id=idsUsed.get(j);
				String docUrlRadical=idUrlRadical.get(id);
					
				for (int i=0; i<entries.size(); i++) {
					String parts[] = entries.get(i).split( "\\s" );			
			
					if (!id.equals(parts[2])) { // remove only if it is not this doc
						String entryDocUrlRadical=idUrlRadical.get(parts[2]);
						if (docUrlRadical.equals(entryDocUrlRadical)) {			
					
						    System.out.println("erasing "+entries.get(i)+", because it has the same radical "+entryDocUrlRadical+" than doc "+id+" with "+docUrlRadical);		
							entries.remove(i);
							i--;
						}
					}
				}
		}
		
		// erase remaining versions duplicated but not in the run
		Set<String> hIdsUsed = new HashSet<String>();		
		for (int j=0; j<idsUsed.size(); j++) {
			hIdsUsed.add(idsUsed.get(j));
		}
		
		for (int i=0; i<entries.size(); i++) {
			String parts[] = entries.get(i).split( "\\s" );	

			if (!hIdsUsed.contains(parts[2])) { // remove only if it is not this doc
								
				String entryDocUrlRadical=idUrlRadical.get(parts[2]);
				System.out.println("doc NOT in run "+ parts[2]+" "+entryDocUrlRadical);				
				if (duplicates.contains(entryDocUrlRadical)) {			
			
				    System.out.println("X erasing "+entries.get(i)+", because it has the same radical "+entryDocUrlRadical);			
					entries.remove(i);
					i--;
				}
				else {
					duplicates.add(entryDocUrlRadical);	
				}
			}			
			
		}
	}
	
	
}
