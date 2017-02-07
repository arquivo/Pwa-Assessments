package pt.arquivo.assessments;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.archive.access.nutch.jobs.EntryPageExpansion;


/**
 * Query PWA and parses results for assessment
 * @author Miguel Costa
 */
public class QueryResultsScrapper {
	
	private static SimpleDateFormat formatterTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // timestamp displayed in searchTests.jsp	
	
	private final static int TIMEOUT=120 * 1000; // 2 minutes
	private final static int HITS_PER_PAGE=1500; // number of hits returned
	private final static int TOP_K_POOLED=10; // number of hits pooled
	private final static int HITS_PER_DUP=0; // maximum hits per site (0 means no restriction)
	private final static int QUERY_MATCHES=-2; // number of query matches (-2 is the default, which is 10K) 	
	private final static String TREC_FORMAT_UNUSED="Q0";
	private final static String TREC_FORMAT_RUN="pwa";
	
	
	//private final static String URL_QUERY="http://experimental.arquivo.pt/nutchwax/search.jsp?hitsPerPage=10&dateStart=01%2F01%2F1996&dateEnd=31%2F12%2F2010&query=";
	//private final static String DOMAIN="t3.tomba.fccn.pt:8080";	
	//private final static String DOMAIN="p15.arquivo.pt:8080";
	private final static String DOMAIN="p18.arquivo.pt";
	//private final static String URL_QUERY="http://"+DOMAIN+"/nutchwax/searchTests.jsp?hitsPerPage="+HITS_PER_PAGE+"&hitsPerDup="+HITS_PER_DUP+"&queryMatches="+QUERY_MATCHES+"&dateStart=01%2F01%2F1994&dateEnd=31%2F12%2F2010&query=";										
	//private final static String URL_QUERY="http://"+DOMAIN+"/nutchwax/searchTests.jsp?summary=false&hitsPerPage="+HITS_PER_PAGE+"&hitsPerDup="+HITS_PER_DUP+"&queryMatches="+QUERY_MATCHES+"&query=";
	private final static String URL_QUERY="http://"+DOMAIN+"/searchTests.jsp?summary=false&hitsPerPage="+HITS_PER_PAGE+"&hitsPerDup="+HITS_PER_DUP+"&queryMatches="+QUERY_MATCHES+"&query=";
	private final static int NUM_FEATURES=68;
	private SqlOperations op = null;
	private int nInserted=0;
	private int nExcluded=0;
	
				
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Validate.isTrue(args.length==7, "usage: <queries file> <queriesType (0-nav, 1-inf)> <ranking features (e.g. 0+1+2)> <features boosts (e.g. 1+1+1)> <database> <username> <password>");		
	
		QueryResultsScrapper scrapper=new QueryResultsScrapper();
		try {
			scrapper.processResults(args[0],Integer.parseInt(args[1]),args[2],args[3],args[4],args[5],args[6]);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extract data from results
	 * @param fileQueries query file
	 * @param features feature indexes
	 * @param boosts feature boosts
	 * @param database database string
	 * @param username database username 
	 * @param password database password
	 * @throws IOException
	 */
	private void processResults(String fileQueries, int queriesType, String features, String boosts, String database, String username, String password) throws IOException, ClassNotFoundException, SQLException, ParseException {		
		
		float nutchMax=0, nutchMin=Float.MAX_VALUE;
		float luceneMax=0, luceneMin=Float.MAX_VALUE;
		float boostolderMax=0, boostolderMin=Float.MAX_VALUE;
		
		// store features
		HashMap<Integer,Float> featuresBoosts=new HashMap<Integer,Float>(); 
		String sfeatures[]=features.split("\\+");
		String sboosts[]=boosts.split("\\+");		
		for (int i=0;i<sfeatures.length;i++) {
			featuresBoosts.put(new Integer(sfeatures[i]), new Float(sboosts[i]));
		}
		
		// functions and boosts to call explain.jsp
		String allFunctions=new String();
		String allBoosts=new String();
		for (int i=0;i<NUM_FEATURES;i++) {
			if (i==0) {
				allFunctions+=i;
				allBoosts+="1";
			}
			else {
				allFunctions+="+"+i;
				allBoosts+="+"+"1";
			}
		}
		
		// connect database
		op = new SqlOperations();
		op.connect(database,username,password);	
		
		// for each topic of topics.xml			
		Document docTopics = Jsoup.parse(new File(fileQueries), "UTF-8", "");			
		Elements topics = docTopics.select("topics > topic");							
		for (Element topic : topics) {		
						
			int topicNumber = Integer.parseInt(topic.attr("number"));			
			Element query = topic.select("query").first();
			Element periodStart = topic.select("period > start").first();
			Element periodEnd = topic.select("period > end").first();
			Element description = topic.select("description[lang=pt]").first();
			
			System.out.println("QUERY: "+query.text());
		
			String squery=query.text();						
			String encodedQuery = convertArchiveQuery(squery);			
			String url = URL_QUERY+encodedQuery+"&sfunctions="+features+"&sboosts="+boosts;
			String encodedPeriodStart = null;
			String encodedPeriodEnd = null;
			if (periodStart!=null) {				
				encodedPeriodStart = convertArchiveQuery(periodStart.text());		
				encodedPeriodEnd = convertArchiveQuery(periodEnd.text());
				squery+=" "+periodStart.text()+" "+periodEnd.text();
				url+="&dateStart="+encodedPeriodStart+"&dateEnd="+encodedPeriodEnd;				
			}					
			Set<String> duplicatesByRel = new HashSet<String>();
			
			// get all url radicals from other ranking models already collected and stored in database
			Set<String> duplicatesByRelFromOtherModels = new HashSet<String>();
			readDocsForTopic(duplicatesByRelFromOtherModels,query.text());					
			
			System.out.println("URL:"+url); 						
			
			// for each element li of the web page (result) for this query
			Document doc = Jsoup.connect(url).timeout(TIMEOUT).get();			
			Elements li = doc.select("li");
			int rank=1;
			for (int i=0; i<li.size() && rank<=TOP_K_POOLED; i++) {				
				Element l=li.get(i);																	
				Element archivelink = l.select("a[href]").first();					
				Element date = l.select("span[class=date]").first();
				Element link = l.select("span[class=url]").first();
				Element explainlink = l.select("a[href]").get(2);								
									
				String explainlinkHref=explainlink.attr("abs:href");	
				int index=explainlinkHref.indexOf("&sfunctions");										
				explainlinkHref=explainlinkHref.substring(0,index)+"&sfunctions="+allFunctions+"&sboosts="+allBoosts; // get all scores of all features															
							
				// connect to explain.jsp and collect feature values
				Document docExplain = Jsoup.connect(explainlinkHref).timeout(TIMEOUT).get(); 										
				Element lexplain = docExplain.select("span[class=features]").first();										
				String lExplainFeatures = lexplain.text(); // extract features										
										
				String docUrl=link.text();
				// filter duplicates (including aliases) by relevance
				String docUrlRadical=EntryPageExpansion.getRadical(docUrl);
				boolean bstore=true;
				if (duplicatesByRel.contains(docUrlRadical)) {  
					if (queriesType==0) { // all URL aliases have the same topical relevance for navigational topics
						bstore=false;
					}
					if (queriesType==1) { // all URL aliases with the same content (near duplicates) have the same topical relevance for informational						
						// if they are near-duplicates TODO {						
							bstore=false;
					    // }
					}
				}
											
				if (bstore) {																																
					String docCode=archivelink.attr("abs:href");
					docCode=docCode.substring(docCode.lastIndexOf("/")+1,docCode.length());
					
					if (!duplicatesByRelFromOtherModels.contains(docUrlRadical)) { 					
						System.out.println(rank+" ADDED: "+docUrl+" "+docUrlRadical);										
						String pStart=(periodStart!=null) ? periodStart.text() : null;
						String pEnd=(periodEnd!=null) ? periodEnd.text() : null;								
						storeData(topicNumber,query.text(),pStart,pEnd,description.text(),queriesType,link.text(),date.text(),archivelink.attr("abs:href"),docCode,lExplainFeatures);
					}
					else {							
						System.out.println(rank+" FILTERED FROM other model: "+docUrl+" "+docUrlRadical);
					}
										
					float score=getScore(lExplainFeatures, featuresBoosts);										
					System.out.println("TREC_FORMAT: "+topicNumber+" "+TREC_FORMAT_UNUSED+" "+docCode+" "+rank+" "+score+" "+TREC_FORMAT_RUN+"_"+features+"_"+boosts);					
		
					duplicatesByRel.add(docUrlRadical);
					rank++;
				}
				else {							
					System.out.println("FILTERED: "+docUrl+" "+docUrlRadical);
				}																		
			}
			
			if (rank<TOP_K_POOLED) {
				System.err.println("Less than "+TOP_K_POOLED+" versions found for query "+squery+". Only "+rank+" were found.");
				System.exit(1);
			}
		}
		
		// close connection		
		op.close();							
		
		System.out.println(nInserted+" pairs of query-document inserted.");
		System.out.println(nExcluded+" pairs of query-document excluded.");		
	}
	
	private String convertArchiveUrl(String s) {
		String saux=s.replace(DOMAIN,"arquivo.pt");
		return saux;		
	}
	
	private String convertArchiveDate(String s) {
		return s.replace(",","");		
	}
	
	private String convertArchiveQuery(String s) throws UnsupportedEncodingException{
		return URLEncoder.encode(s,"UTF-8");				
	}

	/**
	 * Store data into database
	 * @param topicNumber topic number
	 * @param query query issued to the system
	 * @param periodStart start date of temporal restriction
	 * @param periodEnd end date of temporal restriction
	 * @param queryType type of query: 0 navigational, 1 informational
	 * @param url URL of the original page
	 * @param dateArchived date of archive
	 * @param urlArchived url for archived page
	 * @param docCode document identifier
	 * @param features list of features and their scores
	 */
	private void storeData(int topicNumber, String query, String periodStart, String periodEnd, String description, int queryType, String url, String dateArchived, String urlArchived, String docCode, String features) throws SQLException, ParseException, IOException {
		
		/*
		int qid=op.selectQueryId(query,queryType);
		if (qid==-1) {
			qid=op.selectQueryCount();	
			op.insertQuery(qid,query,periodStart,periodEnd,description,queryType);
		}
		*/
		
		int qid=op.selectQueryId(query,queryType);
		if (qid==-1) {	
			op.insertQuery(topicNumber,query,periodStart,periodEnd,description,queryType);
		}
		else if (qid!=topicNumber) { // sanity check
			throw new IOException("Same query, but with a different topic number: "+qid+" != "+topicNumber);
		}		
		
		Date date=formatterTimestamp.parse(dateArchived);		
		//int docid=op.selectDocId(url,new Timestamp(date.getTime()));   this fails because when I extract all versions I give the same URL as the first version and they can differ due to alias (e.g index.html)
		int docid=op.selectDocIdPerCode(docCode);
		if (docid==-1) {
			docid=op.selectDocMax()+1;	
			op.insertDoc(docid,url,new Timestamp(date.getTime()),urlArchived,docCode);			
		}
		
		int qiddocid=op.selectQueryDocId(topicNumber,docid);
		if (qiddocid==-1) {
			qiddocid=op.selectQueryDocMax()+1;	
			op.insertQueryDoc(qiddocid,topicNumber,docid,features);
			nInserted++;
		}	
		else {
			nExcluded++;
		}
	}
	
	/**
	 * Read the url radicals already stored from other ranking models
	 * @param duplicatesByRelFromOtherModels
	 * @param query topic's query
	 */
	private void readDocsForTopic(Set<String> duplicatesByRelFromOtherModels, String query) throws SQLException {
		Vector<String> urls=op.selectQueryDocs(query);
		for (int i=0;i<urls.size();i++) {
			if (duplicatesByRelFromOtherModels.contains(EntryPageExpansion.getRadical(urls.get(i)))) { // sanity check
				System.err.println("Url radical already stored!");
				System.exit(1);
			}
		
			duplicatesByRelFromOtherModels.add(EntryPageExpansion.getRadical(urls.get(i)));			
		}
	}	
	
	/**
	 * Get document's score
	 * @param lExplainFeatures list of features extracted from explain.jsp
	 * @param featuresBoosts boosts of features in query request
	 * @return
	 */
	private float getScore(String lExplainFeatures, HashMap<Integer,Float>featuresBoosts) {
		float score=0;					
		String saux[]=lExplainFeatures.split(" ");
		for (int j=0;j<saux.length;j++) {
			String parts[]=saux[j].split(":");
			Integer featureId=new Integer(parts[0]);
			Float featureScore=new Float(parts[1]);
			if (featuresBoosts.get(featureId)!=null) {
				score+= featureScore * featuresBoosts.get(featureId);
			}
		}	
		
		return score;
	}
		
}
