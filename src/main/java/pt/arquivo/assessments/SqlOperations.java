package pt.arquivo.assessments;

import java.sql.*;   // All we need for JDBC
import java.sql.Date;
import java.text.*;
import java.io.*;
import java.util.*;

/**
 * Class to handle SQL operations for assessments
 * @author Miguel Costa
 */
public class SqlOperations {

	  
	  private final static String SQL_QUERY_QUERY_ID="select id from queries where query=? and type=?";
	  private final static String SQL_QUERY_QUERIES="select id,query,periodStart,periodEnd,description,type from queries order by id";
	  
	  private final static String SQL_QUERY_DOC_ID="select id from docs where url=? and date=?";
	  private final static String SQL_QUERY_DOC_ID_PER_CODE="select id from docs where code=?";

	  private final static String SQL_QUERY_QUERYDOC_ID="select id from queriesdocs where query=? and doc=?";	  
	  private final static String SQL_QUERY_QUERYDOC_WITH_LESS_ASSESSMENTS=
	      "select qd.id, qd.query, qd.doc, q.query, q.description, q.periodstart, q.periodend, d.url, d.urlarchived, d.date, count(*) as c "+
	      "from queriesdocs qd LEFT OUTER JOIN assessments a ON (qd.id=a.querydoc), queries q, docs d "+
	      "where qd.query=q.id and qd.doc=d.id "+
	      "group by qd.id, qd.query, qd.doc, q.query, q.description, q.periodstart, q.periodend, d.url, d.urlarchived, d.date "+
	      "having qd.id NOT IN (select querydoc from assessments where userid = ? OR userid ='groundtruth') and count(*) < ? "+
	      "order by c asc "+
	      "limit 1 "+
	      "offset ?"; 
	  /*private final static String SQL_QUERY_QUERYDOC_WITH_LESS_ASSESSMENTS=
		      "select qd.id, qd.query, qd.doc, q.query, q.description, q.periodstart, q.periodend, d.url, d.urlarchived, d.date, count(*) as c "+
		      "from queriesdocs qd LEFT OUTER JOIN assessments a ON (qd.id=a.querydoc), queries q, docs d "+
		      "where qd.query=q.id and qd.doc=d.id "+
		      "group by qd.id, qd.query, qd.doc, q.query, q.description, q.periodstart, q.periodend, d.url, d.urlarchived, d.date "+
		      "order by c asc "+
		      "limit 1 "+
		      "offset ?";*/
	  private final static String SQL_QUERY_QUERYDOC_FEATURES="select features from queriesdocs where id=?";
	  
	  private final static String SQL_QUERY_ASSESSMENTS_REMAINING_COUNT=
	      "select count(*) from ( "+
	      "select qd.id, qd.query, qd.doc, q.query, d.url, d.urlarchived, d.date, count(*) as c "+
	      "from queriesdocs qd LEFT OUTER JOIN assessments a ON (qd.id=a.querydoc), queries q, docs d "+
	      "where userid<>'groundtruth' qd.query=q.id and qd.doc=d.id "+
	      "group by qd.id, qd.query, qd.doc, q.query, d.url, d.date, d.urlarchived "+	      
	      "having qd.id NOT IN (select querydoc from assessments where userid ='groundtruth') and count(*) < ? "+
	      "order by c asc "+
	      ") as aliascount";
	  
	  private final static String SQL_QUERY_ASSESSMENTS_REMAINING_USER_COUNT=
	      "select count(*) from ( "+
	      "select qd.id, qd.query, qd.doc, q.query, d.url, d.urlarchived, d.date, count(*) as c "+
	      "from queriesdocs qd LEFT OUTER JOIN assessments a ON (qd.id=a.querydoc), queries q, docs d "+
	      "where qd.query=q.id and qd.doc=d.id "+
	      "group by qd.id, qd.query, qd.doc, q.query, d.url, d.date, d.urlarchived "+
	      "having qd.id NOT IN (select querydoc from assessments where userid = ? OR userid ='groundtruth') and count(*) < ? "+
	      "order by c asc "+
      ") as aliascount";
	  	  	 
	  private final static String SQL_QUERY_DOCS =
	      "select d.url "+
	      "from queriesdocs qd, queries q, docs d "+
	      "where q.query=? and qd.query=q.id and qd.doc=d.id ";	      	  
	  
	  
	  private final static String SQL_QUERY_ASSESSMENTS =		  		 
		  "select d.code, d.url, q.id, q.query, q.periodstart, q.periodend, a.rate, a.type, qd.id, count(*) as c "+
		  "from assessments a, queriesdocs qd, queries q, docs d " +		  
		  "where a.userid=? and a.querydoc=qd.id and qd.query=q.id and qd.doc=d.id "+ 
		  "group by d.code, d.url, q.id, q.query, q.periodstart, q.periodend, a.rate, a.type, qd.id ";
		  //"having count(*) = 1 ";
	  
	  private final static String SQL_QUERY_ASSESSMENTS_OF_TYPE =
		  "select d.code, d.url, q.id, q.query, q.periodstart, q.periodend, a.rate, a.type, qd.id, count(*) as c "+
		  "from assessments a, queriesdocs qd, queries q, docs d " +		  
		  "where a.userid=? and a.type=? and a.querydoc=qd.id and qd.query=q.id and qd.doc=d.id "+  //and qd.query>25 "+  
		  "group by d.code, d.url, q.id, q.query, q.periodstart, q.periodend, a.rate, a.type,qd.id ";		
	  
	  private final static String SQL_QUERY_ASSESSMENTS_USER_ID_QUERY_ID =
		  "select d.url, d.date, d.urlarchived, a.rate, a.querydoc "+ 
		  "from assessments a, queriesdocs qd, docs d "+ 
		  "where a.userid=? and qd.query=? and a.querydoc=qd.id and qd.doc=d.id "+ 
		  "order by a.rate desc";
		  		  	 
	  private final static String SQL_QUERY_ASSESSMENTS_PER_QUERYDOC_RATE =
		  "select distinct a.querydoc, foo.rate, 0 as c "+
		  "from assessments a, (select distinct(rate) from assessments) as foo "+
		  "where foo.rate not in (select b.rate from assessments b where b.querydoc=a.querydoc) "+
		  "UNION "+
		  "select a.querydoc, a.rate, count(*) as c "+
		  "group by a.querydoc, a.rate "+
		  "from assessments a "+
		  "order by querydoc, rate";	  
	  
	  private final static String SQL_QUERY_ASSESSMENTS_DIVERGENT =		  
	  	  "select a.querydoc, q.query, q.periodstart, q.periodend, d.url, d.urlarchived, d.date, a.rate, a.userid "+
	  	  "from assessments a, queriesdocs qd, queries q, docs d "+
	  	  "where a.querydoc in ( "+
	  	  " select b.querydoc "+
	  	  " from assessments b "+
	  	  " where b.userid!='"+Assessment.GROUND_TRUTH_USER+"' "+
	  	  " group by b.querydoc, b.rate "+ 
	  	  " having count(*)!=3 ) "+
	  	  "and a.querydoc=qd.id and qd.query=q.id and qd.doc=d.id "+
	  	  "order by a.querydoc, a.userid";
		  	 
	  private final static String SQL_QUERY_QUERY_COUNT="select count(*) from queries";
	  private final static String SQL_QUERY_DOC_COUNT="select count(*) from docs";
	  private final static String SQL_QUERY_QUERYDOC_COUNT="select count(*) from queriesdocs";
	  private final static String SQL_QUERY_ASSESSMENTS_COUNT="select count(*) from assessments";
	  private final static String SQL_QUERY_ASSESSMENTS_USER_COUNT="select count(*) from assessments where userid = ?";
	  	  
	  private final static String SQL_QUERY_DOC_MAX="select max(id) from docs";
	  private final static String SQL_QUERY_QUERYDOC_MAX="select max(id) from queriesdocs";
	  
	  private final static String SQL_INSERT_QUERY="insert into queries (id,query,periodStart,periodEnd,description,type) values (?,?,?,?,?,?)";	  
	  private final static String SQL_INSERT_DOC="insert into docs (id,url,date,urlarchived,code) values (?,?,?,?,?)";
	  private final static String SQL_INSERT_QUERYDOC="insert into queriesdocs (id,query,doc,features) values (?,?,?,?)";	  
	  private final static String SQL_INSERT_ASSESSMENT="insert into assessments (querydoc,userid,rate,comment,creationtime,type) values (?,?,?,?,current_timestamp,?)";
	  
	  private final static String SQL_UPDATE_ASSESSMENT="update assessments set rate=? where querydoc=? and userid=?";
	 
	  private final static String SQL_QUERY_All_DOCS = "select id,url,date,urlarchived,code from docs where  urlarchived ilike '%p15%'";
	  
	  private final static String SQL_UPDATE_DOCS = "update docs set urlarchived = ? , code = ? where id = ?";
	  
	  private Connection        db;        // A connection to the database
	  private DatabaseMetaData  dbmd;      //
	                    
	  private PreparedStatement psInsertQuery;
	  private PreparedStatement psInsertDoc;
	  private PreparedStatement psInsertQueryDoc;
	  private PreparedStatement psInsertAssessment;
	  private PreparedStatement psUpdateAssessment;	  
	  
	  private PreparedStatement psQueryQueryId;
	  private PreparedStatement psQueryQueries;
	  private PreparedStatement psQueryDocId;
      private PreparedStatement psQueryDocIdPerCode;
	  private PreparedStatement psQueryQueryDocId;	  	  
	  private PreparedStatement psQueryQueryDocWithLessAssessments;
	  private PreparedStatement psQueryQueryDocFeatures;
	  
	  private PreparedStatement psQueryQueryCount;
	  private PreparedStatement psQueryDocCount;
	  private PreparedStatement psQueryQueryDocCount;
	  private PreparedStatement psQueryAssessmentsCount;
	  private PreparedStatement psQueryAssessmentsUserCount;
	  private PreparedStatement psQueryAssessmentsRemainingCount;
	  private PreparedStatement psQueryAssessmentsRemainingUserCount;
	  
	  private PreparedStatement psQueryDocMax;
	  private PreparedStatement psQueryQueryDocMax;
	  
	  private PreparedStatement psQueryDocs;
	  private PreparedStatement psQueryAssessments;	
	  private PreparedStatement psQueryAssessmentsOfType;
	  private PreparedStatement psQueryAssessmentsUserQuery;
	  private PreparedStatement psQueryAssessmentsQuerydocRate;
	  private PreparedStatement psQueryAssessmentsDivergent;
	  
	  private PreparedStatement psQueryGetDocs;
	  private PreparedStatement psUpdateDocs;
	 
	  /**
	   * Connect to database
	   * @param database
	   * @param username
	   * @param password
	   * @throws ClassNotFoundException
	   * @throws SQLException
	   */
	  public void connect(String database, String username, String password) throws  SQLException {
		  System.out.println( "[PwaAssessments][SqlOperations] database["+database+"] username["+username+"] password["+password+"]" );
		  try{
			  Class.forName("org.postgresql.Driver"); //load the driver
	  	  } catch (ClassNotFoundException e) {
	  		  System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();
			return;
	  	  }
		  System.out.println("PostgreSQL JDBC Driver Registered!");
		  db = null;
		  try {
			  db = DriverManager.getConnection("jdbc:postgresql:"+database,
		                                     username,
		                                     password); //connect to the db
		  } catch (SQLException e) {
				System.out.println("Connection Failed! Check output console");
				e.printStackTrace();
				return;
		  }
		  if (db != null) {
				System.out.println("You made it, take control your database now!");
		  } else {
				System.out.println("Failed to make connection!");
				return;
		  }
		  dbmd = db.getMetaData(); //get MetaData to confirm connection
		  System.out.println("Connection to "+dbmd.getDatabaseProductName()+" "+
		                       dbmd.getDatabaseProductVersion()+" successful.\n");
		  
		  psInsertQuery = db.prepareStatement(SQL_INSERT_QUERY);
		  psInsertDoc = db.prepareStatement(SQL_INSERT_DOC);
		  psInsertQueryDoc = db.prepareStatement(SQL_INSERT_QUERYDOC);	
		  psInsertAssessment = db.prepareStatement(SQL_INSERT_ASSESSMENT);
		  psUpdateAssessment  = db.prepareStatement(SQL_UPDATE_ASSESSMENT);
		  
		  psQueryQueryId = db.prepareStatement(SQL_QUERY_QUERY_ID);
		  psQueryQueries = db.prepareStatement(SQL_QUERY_QUERIES);
		  psQueryDocId = db.prepareStatement(SQL_QUERY_DOC_ID);
		  psQueryDocIdPerCode = db.prepareStatement(SQL_QUERY_DOC_ID_PER_CODE);
		  psQueryQueryDocId = db.prepareStatement(SQL_QUERY_QUERYDOC_ID);
		  psQueryQueryDocWithLessAssessments = db.prepareStatement(SQL_QUERY_QUERYDOC_WITH_LESS_ASSESSMENTS);
		  psQueryQueryDocFeatures = db.prepareStatement(SQL_QUERY_QUERYDOC_FEATURES);
		  
		  psQueryQueryCount = db.prepareStatement(SQL_QUERY_QUERY_COUNT);
		  psQueryDocCount = db.prepareStatement(SQL_QUERY_DOC_COUNT);
		  psQueryQueryDocCount = db.prepareStatement(SQL_QUERY_QUERYDOC_COUNT);		  
		  psQueryAssessmentsCount = db.prepareStatement(SQL_QUERY_ASSESSMENTS_COUNT);		  
		  psQueryAssessmentsUserCount = db.prepareStatement(SQL_QUERY_ASSESSMENTS_USER_COUNT);		  
		  psQueryAssessmentsRemainingCount = db.prepareStatement(SQL_QUERY_ASSESSMENTS_REMAINING_COUNT);
		  psQueryAssessmentsRemainingUserCount = db.prepareStatement(SQL_QUERY_ASSESSMENTS_REMAINING_USER_COUNT);
		  
		  psQueryDocMax = db.prepareStatement(SQL_QUERY_DOC_MAX);
		  psQueryQueryDocMax = db.prepareStatement(SQL_QUERY_QUERYDOC_MAX);
		  
		  psQueryDocs = db.prepareStatement(SQL_QUERY_DOCS);
		  psQueryAssessments = db.prepareStatement(SQL_QUERY_ASSESSMENTS);		
		  psQueryAssessmentsOfType = db.prepareStatement(SQL_QUERY_ASSESSMENTS_OF_TYPE);
		  psQueryAssessmentsUserQuery = db.prepareStatement(SQL_QUERY_ASSESSMENTS_USER_ID_QUERY_ID);	
		  
		  psQueryAssessmentsQuerydocRate = db.prepareStatement(SQL_QUERY_ASSESSMENTS_PER_QUERYDOC_RATE);
		  psQueryAssessmentsDivergent = db.prepareStatement(SQL_QUERY_ASSESSMENTS_DIVERGENT);		  
		  
		  psQueryGetDocs = db.prepareStatement( SQL_QUERY_All_DOCS );
		  psUpdateDocs = db.prepareStatement( SQL_UPDATE_DOCS );
	  }
	  
	  /**
	   * Close resources
	   * @throws SQLException
	   */
	  public void close() throws SQLException {
		  psInsertQuery.close();
		  psInsertDoc.close();
		  psInsertQueryDoc.close();
		  psInsertAssessment.close();
		  psUpdateAssessment.close();
		  psQueryQueryId.close();
		  psQueryQueries.close();
		  psQueryDocId.close();
		  psQueryDocIdPerCode.close();
		  psQueryQueryDocId.close();	
		  psQueryQueryDocWithLessAssessments.close();
		  psQueryQueryDocFeatures.close();
		  psQueryQueryCount.close();
		  psQueryDocCount.close();
		  psQueryQueryDocCount.close();
		  psQueryAssessmentsCount.close();
		  psQueryAssessmentsUserCount.close();
		  psQueryAssessmentsRemainingCount.close();
		  psQueryAssessmentsRemainingUserCount.close();
		  psQueryDocs.close();
		  psQueryAssessments.close();
		  psQueryAssessmentsOfType.close();
		  psQueryAssessmentsUserQuery.close();
		  psQueryAssessmentsQuerydocRate.close();
		  psQueryAssessmentsDivergent.close();
		  psQueryDocMax.close();
		  psQueryQueryDocMax.close();
		  psQueryGetDocs.close( );
		  psUpdateDocs.close( );
		  db.commit();
		  db.close();
	  }
	  
	  /**
	   * Query query 
	   * @return query object
	   * @throws SQLException
	   */
	  public Vector< Doc > selectDocs( ) throws SQLException {			  		  
		  ResultSet results = psQueryGetDocs.executeQuery( );	
		  Vector<Doc> queries=new Vector<Doc>();		  
		  if (results!=null) { 
			  while (results.next()) {
				  queries.add( new Doc( results.getLong( 1 ) , results.getString( 2 ) , results.getTimestamp( 3 ) , results.getString( 4 ) , results.getString( 5 ) ));
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return queries;			
	  }
	  
	  public void updateDocs( String urlarchived , String code , long id ) throws SQLException {
		  psUpdateDocs.setString( 1 , urlarchived );
		  psUpdateDocs.setString( 2 , code );
		  psUpdateDocs.setLong( 3 , id );
		  int res = psUpdateDocs.executeUpdate();		  		  
		  if ( res == 0 ) {
			  throw new SQLException( "document[" + id + "] updating faild!" );		      
		  } 
	  }
	  
	  /**
	   * Query query id
	   * @return id if exists; -1 otherwise
	   * @throws SQLException
	   */
	  public int selectQueryId( String query , int queryType ) throws SQLException {	
		  psQueryQueryId.setString(1,query);		 
		  psQueryQueryId.setInt(2,queryType);
		  
		  ResultSet results = psQueryQueryId.executeQuery();	
		  int id=-1;
		  if (results!=null) {
			  if (results.next()) {
				  id=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return id;			
	  }
	  
	  /**
	   * Query query 
	   * @return query object
	   * @throws SQLException
	   */
	  public Vector<Query> selectQueries() throws SQLException {			  		  
		  ResultSet results = psQueryQueries.executeQuery();	
		  Vector<Query> queries=new Vector<Query>();		  
		  if (results!=null) { 
			  while (results.next()) {
				  queries.add(new Query(results.getInt(1),results.getString(2),results.getString(3),results.getString(4),results.getString(5),results.getInt(6)));
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return queries;			
	  }
	  
	  /**
	   * Query doc id
	   * @return id if exists; -1 otherwise
	   * @throws SQLException
	   */
	  public int selectDocId(String url, Timestamp date) throws SQLException {		
		  psQueryDocId.setString(1,url);		 
		  psQueryDocId.setTimestamp(2,date);
		  
		  ResultSet results = psQueryDocId.executeQuery();	
		  int id=-1;
		  if (results!=null) {
			  if (results.next()) {
				  id=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return id;	
	  }

	  /**
	   * Query doc id per code
	   * @param code version identifier
	   * @return id if exists; -1 otherwise
	   * @throws SQLException
	   */
	  public int selectDocIdPerCode(String code) throws SQLException {		
		  psQueryDocIdPerCode.setString(1,code);		 
		  
		  ResultSet results = psQueryDocIdPerCode.executeQuery();	
		  int id=-1;
		  if (results!=null) {
			  if (results.next()) {
				  id=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return id;	
	  }

	  /**
	   * Query query-doc id
	   * @return id if exists; -1 otherwise
	   * @throws SQLException
	   */
	  public int selectQueryDocId(int query, int doc) throws SQLException {	
		  psQueryQueryDocId.setInt(1,query);		 
		  psQueryQueryDocId.setInt(2,doc);
		  
		  ResultSet results = psQueryQueryDocId.executeQuery();		    			  							  		 
		  int id=-1;
		  if (results!=null) {
			  if (results.next()) {
				  id=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return id;	
	  }
	  		  	 
	  /**
	   * Query query-doc id with less assessments
	   * @throws SQLException
	   */
      public QueryDoc selectQueryDocWithLessAssessments(String userId, int maxAssessments, int offset) throws SQLException {	
    	  System.out.println( "Query before " + psQueryQueryDocWithLessAssessments.toString( ) );
    	  psQueryQueryDocWithLessAssessments.setString(1,userId);		 
		  psQueryQueryDocWithLessAssessments.setInt(2,maxAssessments);
		  psQueryQueryDocWithLessAssessments.setInt(3,offset);
		  System.out.println( "Query after " + psQueryQueryDocWithLessAssessments.toString( ) );
		  QueryDoc ret=null;
		  try{
			  ResultSet results = psQueryQueryDocWithLessAssessments.executeQuery();
			  System.out.println( "Execute Query" );
			  if (results!=null) {
				  System.out.println( "results != null" );
				  if (results.next()) {
					  ret=new QueryDoc(results.getInt(1),results.getInt(2),results.getInt(3),results.getString(4),results.getString(5),results.getString(6),results.getString(7),results.getString(8),results.getString(9),new Date(results.getTimestamp(10).getTime()),results.getInt(11));				  
					  System.out.println( "ret = " + ret.getQuery( ) );
				  }
				  results.close();  		      
			  }
			  return ret;
		  } catch ( SQLException e  ) {
			  System.out.println("selectQueryDocWithLessAssessments e ");
			  e.printStackTrace();
			  return null;
		  }
	  }	  	
      
      /**
	   * Query query-doc features
	   * @return id query-doc id
	   * @throws SQLException
	   */
	  public String selectQueryDocFeatures(int id) throws SQLException {	
		  psQueryQueryDocFeatures.setInt(1,id);		 
		  
		  ResultSet results = psQueryQueryDocFeatures.executeQuery();		    			  							  		 
		  String features=null;
		  if (results!=null) {
			  if (results.next()) {
				  features=results.getString(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return features;	
	  }
	  
	  /**
	   * Query query count
	   * @throws SQLException
	   */
	  public int selectQueryCount() throws SQLException {			  
		  ResultSet results = psQueryQueryCount.executeQuery();
		  int total=-1;
		  if (results!=null) {
			  if (results.next()) {
				  total=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return total;		  
	  }
	  
	  /**
	   * Query doc count
	   * @throws SQLException
	   */
	  public int selectDocCount() throws SQLException {						  
		  ResultSet results = psQueryDocCount.executeQuery();		    			  							  		 
		  int total=-1;
		  if (results!=null) {
			  if (results.next()) {
				  total=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return total;	
	  }
	  
	  /**
	   * Query doc maximum id
	   * @throws SQLException
	   */
	  public int selectDocMax() throws SQLException {						  
		  ResultSet results = psQueryDocMax.executeQuery();		    			  							  		 
		  int total=-1;
		  if (results!=null) {
			  if (results.next()) {
				  total=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return total;	
	  }
	  	  
	  /**
	   * Query query-doc count
	   * @throws SQLException
	   */
	  public int selectQueryDocCount() throws SQLException {	
		  ResultSet results = psQueryQueryDocCount.executeQuery();		    			  							  		 
		  int total=-1;
		  if (results!=null) {
			  if (results.next()) {
				  total=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return total;	
	  }
	  
	  /**
	   * Query query-doc maximum id
	   * @throws SQLException
	   */
	  public int selectQueryDocMax() throws SQLException {	
		  ResultSet results = psQueryQueryDocMax.executeQuery();		    			  							  		 
		  int total=-1;
		  if (results!=null) {
			  if (results.next()) {
				  total=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return total;	
	  }

	  /**
	   * Query number of assessments made
	   * @throws SQLException
	   */
	  public int selectQueryAssessmentsCount() throws SQLException {	
		  ResultSet results = psQueryAssessmentsCount.executeQuery();		    			  							  		 
		  int total=-1;
		  if (results!=null) {
			  if (results.next()) {
				  total=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return total;	
	  }

	  /**
	   * Query number of assessments that a user made 
	   * @throws SQLException
	   */
	  public int selectQueryAssessmentsUserCount(String userId) throws SQLException {	
		  psQueryAssessmentsUserCount.setString(1,userId);		 
		  ResultSet results = psQueryAssessmentsUserCount.executeQuery();		    			  							  		 
		  int total=-1;
		  if (results!=null) {
			  if (results.next()) {
				  total=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return total;	
	  }

	  /**
	   * Query number of assessments that a user can make 
	   * @throws SQLException
	   */
      public int selectQueryAssessmentsRemainingUserCount(String userId, int maxAssessments) throws SQLException {	
		  psQueryAssessmentsRemainingUserCount.setString(1,userId);		 
		  psQueryAssessmentsRemainingUserCount.setInt(2,maxAssessments);		 
		  ResultSet results = psQueryAssessmentsRemainingUserCount.executeQuery();		    			  							  		 
		  int total=-1;
		  if (results!=null) {
			  if (results.next()) {
				  total=results.getInt(1);
			  }
			  results.close();  		      
		  } 		  			  							  		  
		  return total;	
	  }
         
         
      /**
   	   * Query number of assessments that can be made 
   	   * @throws SQLException
   	   */
      public int selectQueryAssessmentsRemainingCount(int maxAssessments) throws SQLException {	   		  		
   		  psQueryAssessmentsRemainingCount.setInt(1,maxAssessments);		 
   		  ResultSet results = psQueryAssessmentsRemainingCount.executeQuery();		    			  							  		 
   		  int total=-1;
   		  if (results!=null) {
   			  if (results.next()) {
   				  total=results.getInt(1);
   			  }
   			  results.close();  		      
   		  } 		  			  							  		  
   		  return total;	
   	  }
                      
      /**
	   * Query number of assessments per querydoc and rate 
	   * @throws SQLException
	   */
	  public ResultSet selectQueryAssessmentsQuerydocRate() throws SQLException {			  		
		  ResultSet results = psQueryAssessmentsQuerydocRate.executeQuery();		    			  							  		 		 		  							  		 
		  return results;	
	  }
	  
	  /**
	   * Query assessments per querydoc that are divergent 
	   * @throws SQLException
	   */
	  public ResultSet selectQueryAssessmentsDivergent() throws SQLException {			  		
		  ResultSet results = psQueryAssessmentsDivergent.executeQuery();		    			  							  		 		 		  							  		 
		  return results;	
	  }
                       
      /**
   	   * Query document URLs collected for a query 
   	   * @throws SQLException
   	   */
      public Vector<String> selectQueryDocs(String query) throws SQLException {	       	  
   		  psQueryDocs.setString(1,query);		 
   		  ResultSet results = psQueryDocs.executeQuery();
   		  Vector<String> urls=new Vector<String>();    		  
  		  if (results!=null) {
  			  while (results.next()) {
  				  urls.add(results.getString(1));
  			  }
  			  results.close();  		      
  		  } 		  			  							  		  
  		  return urls;	
   	  }
                      
      /**
   	   * Query assessments collected for a document given a user id and assessment type
   	   * @param userId user identifier
   	   * @param assessmentType assessment type 
   	   * @throws SQLException
   	   */      
      public Vector<Assessment> selectQueryAssessments(String userId) throws SQLException {	           	      	     
   		  psQueryAssessments.setString(1,userId);	 
   		  ResultSet results = psQueryAssessments.executeQuery();
   		  Vector<Assessment> assessments=new Vector<Assessment>();    		  
  		  if (results!=null) {
  			  while (results.next()) {
  				  assessments.add(new Assessment(results.getString(1),results.getString(2),results.getInt(3),results.getString(4),results.getString(5),results.getString(6),results.getInt(7),results.getInt(8),results.getInt(9)));  				
  			  }
  			  results.close();  		      
  		  } 		  			  							  		  
  		  return assessments;	
   	  }
      
      /**
   	   * Query assessments collected for a document given a user id and assessment type
   	   * @param userId user identifier
   	   * @param assessmentType assessment type 
   	   * @throws SQLException
   	   */      
      public Vector<Assessment> selectQueryAssessments(String userId, int assessmentType) throws SQLException {	           	      	     
   		  psQueryAssessmentsOfType.setString(1,userId);
   		  psQueryAssessmentsOfType.setInt(2,assessmentType);		 
   		  ResultSet results = psQueryAssessmentsOfType.executeQuery();
   		  Vector<Assessment> assessments=new Vector<Assessment>();    		  
  		  if (results!=null) {
  			  while (results.next()) {
  				  assessments.add(new Assessment(results.getString(1),results.getString(2),results.getInt(3),results.getString(4),results.getString(5),results.getString(6),results.getInt(7),results.getInt(8),results.getInt(9)));  				
  			  }
  			  results.close();  		      
  		  } 		  			  							  		  
  		  return assessments;	
   	  }
      
      /**   	   
   	   * Query assessments collected for a document given a query id and user id
   	   * @param userId user identifier
   	   * @param queryId query identifier
   	   * @throws SQLException
   	   */       
      public Vector<Assessment> selectQueryAssessmentsForQuery(String userId, int queryId) throws SQLException {	       	  
   		  psQueryAssessmentsUserQuery.setString(1,userId);		 
   		  psQueryAssessmentsUserQuery.setInt(2,queryId);
   		  ResultSet results = psQueryAssessmentsUserQuery.executeQuery();
   		  Vector<Assessment> assessments=new Vector<Assessment>();    		  
  		  if (results!=null) {
  			  while (results.next()) {
  				  assessments.add(new Assessment(results.getString(1),results.getDate(2),results.getString(3),results.getInt(4),results.getInt(5)));
  			  }
  			  results.close();  		      
  		  } 		  			  							  		  
  		  return assessments;	
   	  }      
      
      
      	  	  	
	  /**
	   * Insert query
	   * @throws SQLException
	   */
	  public void insertQuery(int id, String query, String periodStart, String periodEnd, String description, int queryType) throws SQLException {				  
		  psInsertQuery.setInt(1,id);
		  psInsertQuery.setString(2,query);
		  psInsertQuery.setString(3,periodStart);
		  psInsertQuery.setString(4,periodEnd);
		  psInsertQuery.setString(5,description);			  
		  psInsertQuery.setInt(6,queryType);
		  
		  int res = psInsertQuery.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 queries inserted.");		      
		  } 		  			  							  		  
	  }
	  
	  /**
	   * Insert document	  
	   * @throws SQLException
	   */
	  public void insertDoc(int id, String url, Timestamp date, String urlarchived, String code) throws SQLException {				  
		  psInsertDoc.setInt(1,id);		
		  psInsertDoc.setString(2,url);		 
		  psInsertDoc.setTimestamp(3,date);
		  psInsertDoc.setString(4,urlarchived);
		  psInsertDoc.setString(5,code);
		  
		  int res = psInsertDoc.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 documents inserted.");		      
		  } 		  			  							  		  
	  }	  
	  
	  /**
	   * Insert query-document
	   * @throws SQLException
	   */
	  public void insertQueryDoc(int id, int query, int doc, String features) throws SQLException {				  
		  psInsertQueryDoc.setInt(1,id);	
		  psInsertQueryDoc.setInt(2,query);		 
		  psInsertQueryDoc.setInt(3,doc);
		  psInsertQueryDoc.setString(4,features);
		  
		  int res = psInsertQueryDoc.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 query-document pairs inserted.");		      
		  } 		  			  							  		  
	  }	  
	  
	  /**
	   * Insert assessment
	   * @param querydoc id of queriesdocs table
	   * @param userid user identifier
	   * @param rate assessment rate
	   * @param comment assessment comment
	   * @param type assessment type: 0 manual, 1 automatic
	   * @throws SQLException
	   */	  
	  public void insertAssessment(int querydoc, String userid, int rate, String comment, int type) throws SQLException {				  
		  psInsertAssessment.setInt(1,querydoc);	
		  psInsertAssessment.setString(2,userid);		 
		  psInsertAssessment.setInt(3,rate);
		  psInsertAssessment.setString(4,comment);
		  psInsertAssessment.setInt(5,type);
		  
		  int res = psInsertAssessment.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 assessments inserted.");		      
		  } 		  			  							  		  
	  }
	  	  
	  /**
	   * Update assessment
	   * @param type session type
	   * @param sessionKey session key
	   * @throws SQLException
	   */
	  public void updateAssessment(int rate, int querydocId, String userId) throws SQLException {
		  psUpdateAssessment.setInt(1,rate);
		  psUpdateAssessment.setInt(2,querydocId);
		  psUpdateAssessment.setString(3,userId);
		  int res = psUpdateAssessment.executeUpdate();		  		  
		  if (res==0) {
			  throw new SQLException("0 assessment entries updated.");		      
		  } 		  			  				  
	  }
}
