<%@ page 
  session="true"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"

  import="java.io.*"
  import="java.util.*"
  import="java.text.*"
  import="pt.arquivo.assessments.SqlOperations"
  import="pt.arquivo.assessments.QueryDoc"
  import="pt.arquivo.assessments.Query"
  import="pt.arquivo.assessments.Assessment"  
  import="java.sql.ResultSet"
%>

<%!
  private static final String SAVE = "Alterar";
      
  private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
  
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Consenso dos resultados de pesquisa do Arquivo da Web Portuguesa</title>
<link rel="stylesheet" type="text/css" href="css/assessments.css" />
<script src="javascript/validations.js" type="text/javascript"></script>
<body>

<%
String userid = Assessment.GROUND_TRUTH_USER;

String database=getServletConfig().getInitParameter("database");
String username=getServletConfig().getInitParameter("username");
String password=getServletConfig().getInitParameter("password");

SqlOperations op = new SqlOperations();
op.connect(database,username,password); 	

String action = request.getParameter("save");
if (action!=null && action.equals(SAVE)) {	
	int querydocid = Integer.parseInt(request.getParameter("querydocid"));		
	int relevance = Integer.parseInt(request.getParameter("relevance"));
	int pastRelevance = Integer.parseInt(request.getParameter("pastRelevance"));
	
	if (pastRelevance==-2) { // not exist		
		op.insertAssessment(querydocid, userid, relevance, null, 0);
	}
	else {
		op.updateAssessment(relevance, querydocid, userid);
	}
}

ResultSet results=op.selectQueryAssessmentsDivergent();

%>

<h1>Consenso dos resultados de pesquisa do Arquivo da Web Portuguesa</h1>
<ol start="1">

<%  
	int i=0;
	boolean ignore=false;
  	if (results!=null) {
		while (ignore || results.next()) {
			i++;
			ignore=false;
			
%>
	<li>	
    <form name="form<%=i%>" action="consensus" method="POST">
      <%= results.getString(2) %> <%= results.getString(3) %> <%= results.getString(4) %> 
      <a href="<%= results.getString(6) %>" target="_blank"><%= results.getString(5) %></a>  <%= formatter.format(results.getDate(7)) %>
<%
			int oldQuerydoc=results.getInt(1);
			int pastRelevance=-2;  
			int creatorRelevance=-2;
			int nDiffs=0;

			if (!results.getString(9).equals(Assessment.GROUND_TRUTH_USER)) {
				creatorRelevance=results.getInt(8);
%>				  				
				<b><%= results.getInt(8) %></b>
<%				
	  		}
			else {
				pastRelevance=results.getInt(8);     			      					      			      			
			}	
   	
		    while (!ignore && results.next()) { // remaininbg rates from other users        
        
		      	if (results.getInt(1)==oldQuerydoc) {
      				if (!results.getString(9).equals(Assessment.GROUND_TRUTH_USER)) {
      					if (creatorRelevance==-2) {
      						creatorRelevance=results.getInt(8);
      					}
      					else if (creatorRelevance!=results.getInt(8)) {
      						nDiffs++;
      					}
%>      			      		
      					<b><%= results.getInt(8) %></b>
<%      			
      				}
      				else {
      					pastRelevance=results.getInt(8);     			      					      			      			
      				}		
      			}
      			else { // it is other querydoc
					ignore=true;
				}
		    }
		    if (creatorRelevance!=pastRelevance) {
%>      		 			
				<input type="text" size="3" name="relevance" value="<%= pastRelevance %>" style="background-color:lightgrey;"></input>				
<%
		    }
			else if (nDiffs>1) {
%>      		 			
				<input type="text" size="3" name="relevance" value="<%= pastRelevance %>" style="background-color:red;"></input>
<%				
			}
			else {
%>				
				<input type="text" size="3" name="relevance" value="<%= pastRelevance %>"></input>     	
<%				
			}
%>		    
			<input type="submit" name="save" value="<%= SAVE %>"/>			            
			<input type="hidden" name="querydocid" value="<%= oldQuerydoc %>" />	  		
			<input type="hidden" name="pastRelevance" value="<%= pastRelevance %>" />	  
	</form>
	</li>
<%	   	
		}
		results.close();
	}	
  	op.close();
%>

</ol>
 
</body>
</html>
