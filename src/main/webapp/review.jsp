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
%>

<%!
  private static final String SAVE = "Alterar";  
    
  private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
  
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Revisão dos resultados de pesquisa do Arquivo da Web Portuguesa</title>
<link rel="stylesheet" type="text/css" href="css/assessments.css" />
<script src="javascript/validations.js" type="text/javascript"></script>
<body>

<%
String userid = request.getRemoteUser();
int ioffset=0;

String database=getServletConfig().getInitParameter("database");
String username=getServletConfig().getInitParameter("username");
String password=getServletConfig().getInitParameter("password");

SqlOperations op = new SqlOperations();
op.connect(database,username,password); 	

String action = request.getParameter("save");
if (action!=null && action.equals(SAVE)) {	
	int querydocid = Integer.parseInt(request.getParameter("querydocid"));		
	int relevance = Integer.parseInt(request.getParameter("relevance"));
	op.updateAssessment(relevance, querydocid, userid);	
}

String soffset = request.getParameter("offset");	
if (soffset!=null) {		
	ioffset=Integer.parseInt(soffset);
}
else {
	ioffset=0;
}

Vector< Query > queries=op.selectQueries( );
Query query = queries.get( ioffset );
Vector< Assessment > assessments = op.selectQueryAssessmentsForQuery( userid , query.getId( ) );	

op.close( );
%>

<h1>Revisão dos resultados de pesquisa do Arquivo da Web Portuguesa</h1>

<div style="height: 0px;"></div>
<h2>Pesquisa (nº <%= (ioffset+1) %>):</h2>


<form name="formAll" action="review" method="POST">
<ol start="1">
  <li>Imagine que para encontrar a página de: 
    <ul><b><%= query.getDescription() %></b></ul>
  </li>  
  <li>Faz a pesquisa: 
    <ul><b><%= query.getQuery() %></b>
    <% if (query.getPeriodStart()!=null){  %> restringida entre <b><%= query.getPeriodStart() %></b> e <b><%= query.getPeriodEnd() %></b> <% } %>    
    </ul>
  </li>  
  <li>Reveja os <%= assessments.size() %> resultados já avaliados:  
<%  
  for ( int i = 0 ; i < assessments.size( ) ; i++ ) {
	  Assessment assess=assessments.get(i);
%>	
    <form name="form<%=i%>" action="review" method="POST">
      <ul><a href="<%= assess.getDocUrlArchived() %>" target="_blank">página arquivada</a> no dia <b><%= formatter.format(assess.getDocDate()) %></b> que tinha o endereço <strong><%= assess.getDocUrl() %></strong>
      <input type="text" size="3" name="relevance" value="<%= assess.getRelevance() %>"></input>
      <input type="submit" name="save" value="<%= SAVE %>"/>
      </ul>
	  <input type="hidden" name="querydocid" value="<%= assess.getQuerydocId() %>" />
	  <input type="hidden" name="offset" value="<%= ioffset %>" />	  
	</form>
<%         
  }
%>
  </li>
 </ol>  
 </form>
 
<%
  int before=(ioffset<=0) ? 0 : ioffset-1;
  int after=(ioffset>=queries.size()-1) ? queries.size()-1 : ioffset+1; 
%>
 
<table border="0">
<tr>
<td>
  <form name="formBefore" action="review" method="POST">
    <input type="hidden" name="offset" value="<%= before %>" />
    <input type="submit" name="next" value="Anterior"/>	
    &nbsp; &nbsp;
  </form>
</td>
<td>
  <form name="formAfter" action="review" method="POST">
    <input type="hidden" name="offset" value="<%= after %>" />
    <input type="submit" name="next" value="Próximo"/>	
  </form>
</td>
</tr>
</table>
 
</body>
</html>
