<%@ page 
  session="true"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"

  import="java.io.*"
  import="java.util.*"
  import="java.text.*"
  import="pt.arquivo.assessments.SqlOperations"
  import="pt.arquivo.assessments.QueryDoc"
%>

<%!
  public static final String SAVE = "        OK        ";
  public static final String SKIP = "Saltar avaliação";
  public static final String ASSESS = "Avaliar nova página";

  public static final int MAX_ASSESSMENTS_PER_QUERYDOC = 3;   
  
  private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
  
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Avaliação dos resultados de pesquisa do Arquivo da Web Portuguesa</title>
<link rel="stylesheet" type="text/css" href="css/assessments.css" />
<script src="javascript/validations.js" type="text/javascript"></script>
<body>

<%
String userid = request.getRemoteUser();
int ioffset=0;

String database=getServletConfig().getInitParameter("database");
String username=getServletConfig().getInitParameter("username");
String password=getServletConfig().getInitParameter("password");

System.out.println( "ASSESSMENTS JSP -> userid: " + userid ); //DEBUG

SqlOperations op = new SqlOperations();
op.connect(database,username,password); 	

int nAssessmentsUser;
String action = request.getParameter("save");


if (action!=null && action.equals(SAVE)) {
	
	String querydocid = request.getParameter("querydocid");	
	String relevance = request.getParameter("relevance");
	String comments = request.getParameter("comments");

	if ( comments != null ) {
		comments=new String( comments.getBytes( "iso-8859-1" ) ,"UTF-8" );
	}
	System.out.println( "Insert Assessment ["+Integer.parseInt(querydocid)+"] ["+userid+"] ["+Integer.parseInt(relevance)+"] ["+comments+"] " );
	op.insertAssessment(Integer.parseInt(querydocid), userid, Integer.parseInt(relevance), comments, 0);			
	System.out.println( " -> selectQueryAssessmentsCount" );
	int nAssessments=op.selectQueryAssessmentsCount();
	System.out.println( " -> nAssessments["+nAssessments+"] selectQueryDocCount" );
	int nAssessmentsRemaining=op.selectQueryDocCount()*MAX_ASSESSMENTS_PER_QUERYDOC - nAssessments;
	System.out.println( " -> nAssessmentsRemaining["+nAssessmentsRemaining+"] selectQueryAssessmentsUserCount " );
	nAssessmentsUser=op.selectQueryAssessmentsUserCount(userid);
	System.out.println( " -> nAssessmentsUser["+nAssessmentsUser+"] selectQueryAssessmentsRemainingUserCount  userid["+userid+"] MAX_ASSESSMENTS_PER_QUERYDOC["+MAX_ASSESSMENTS_PER_QUERYDOC+"]" );
	int nAssessmentsRemainingUser=op.selectQueryAssessmentsRemainingUserCount(userid,MAX_ASSESSMENTS_PER_QUERYDOC);
	System.out.println( " -> nAssessmentsRemainingUser["+nAssessmentsRemainingUser+"]  " );
	//int nAssessmentsRemaining=op.selectQueryAssessmentsRemainingCount(MAX_ASSESSMENTS_PER_QUERYDOC);	
%>
	<form name="form2" action="assessments" method="POST">
		<div class="message success" >
	        <h6><span id="alertboxHeader">A sua avaliação foi gravada com sucesso. Obrigado.</span></h6>
<!--    	<p><span id="alertboxMessage">Foram efectuadas um total de <b><%= nAssessments %></b> avaliações. Faltam <b><%= nAssessmentsRemaining %></b> avaliações.<span></p> -->   				
<%
		if ( nAssessmentsRemainingUser > 0 ) {
%>
	        <p><span id="alertboxMessage">Avaliou um total de <b><%= nAssessmentsUser %></b> página(s). Pode ainda avaliar <b><%= nAssessmentsRemainingUser %></b> página(s).</span></p>
<!-- 			<p><span id="alertboxMessage">Para efectar uma nova avaliação carregue no botão seguinte: <input type="submit" name="submit" value="<%=ASSESS%>" /></span></p> -->
<%
		} else {
%>
			<p><span id="alertboxMessage">Avaliou um total de <b><%= nAssessmentsUser %></b> página(s). Não existem mais páginas para avaliar.</span></p>
			<p><span id="alertboxMessage">Obrigado pela sua participação.<span></p>
<%
		}
%>
		</div>			

		<input type="hidden" name="offset" value="<%=ioffset%>" />
	</form>

	</body>
	</html>	
<%	
/*
	op.close();
	return;
*/	
}
else {
	System.out.println( "get nassessmentUser " );
	nAssessmentsUser=op.selectQueryAssessmentsUserCount( userid );
	System.out.println( " nAssessmentsUser = " + nAssessmentsUser );
}

String soffset = request.getParameter("offset");	
if (soffset!=null) {		
	ioffset=Integer.parseInt(soffset);
}
System.out.println( "query selectQueryDocWithLessAssessments" );
QueryDoc qd=op.selectQueryDocWithLessAssessments(userid,MAX_ASSESSMENTS_PER_QUERYDOC,ioffset);
System.out.println( "query selectQueryDocWithLessAssessments result!" );
if (qd==null) {  
	System.out.println( "qd is null! ioffset = " + ioffset );
	if (ioffset>0) { // round robin
		ioffset=0;
		qd=op.selectQueryDocWithLessAssessments(userid,MAX_ASSESSMENTS_PER_QUERYDOC,ioffset);
	}
	System.out.println( "[2]qd is null? qd = " + qd );
	if (qd==null) {  
%>
		<div class="message success" >
    		    <h6><span id="alertboxHeader">Não existem mais páginas para avaliar.</span></h6>
		    <p><span id="alertboxMessage">Obrigado pela sua participação.<span></p>
		</div>
<% 	
		op.close();
		return;
	}
}

op.close( );
ioffset++;
System.out.println( "[3] ioffset = " + ioffset );
%>	
<h1>Avaliação dos resultados de pesquisa do Arquivo da Web Portuguesa</h1>
<div style="height: 0px;"></div>
<p>
O <a href="http://arquivo.pt">Arquivo da Web Portuguesa</a> permite pesquisar e aceder a páginas publicadas na Internet entre 1996 e 2010. <br />
Ajude a melhorar o Arquivo da Web Portuguesa avaliando a relevância dos seus resultados.
</p>
<p>
<b><a href="instructions.html">Ler instruções</a></b>
</p>

<div style="height: 0px;"></div>
<h2>Avaliação (nº <%= (nAssessmentsUser+1) %>):</h2>

<form name="form1" action="assessments" method="POST">
<ol start="1">
  <li>Imagine que para encontrar a página de: 
    <ul><b><%= qd.getDescription() %></b></ul>
  </li>  
  <li>Faz a pesquisa: 
    <ul><b><%= qd.getQuery() %></b>
    <% if (qd.getPeriodStart()!=null){  %> restringida entre <b><%= qd.getPeriodStart() %></b> e <b><%= qd.getPeriodEnd() %></b> <% } %>    
    </ul>
  </li>  
  <li>Obtém como resultado a:
    <ul><a href="<%= qd.getUrlArchived() %>" target="_blank">página arquivada</a> no dia <b><%= formatter.format(qd.getDate()) %></b> que tinha o endereço <strong><%= qd.getUrl() %></strong></ul>         
  </li>
  <li>Clique na página arquivada e avalie a sua relevância como: <br />
  <ul>
  <input type="radio" name="relevance" value="2"><b>Muito relevante:</b> é exactamente a página que pretendia encontrar.<br />
  <input type="radio" name="relevance" value="1"><b>Relevante:</b> é uma boa alternativa, mas não é a página que pretendia encontrar.<br />
  <input type="radio" name="relevance" value="0"><b>Não relevante:</b> não é a página que pretendia encontrar.<br />
  <input type="radio" name="relevance" value="-1"><b>Não sei / Não consigo responder.</b><br />
  </ul>
  </li>
  <li>
  Justifique a sua avaliação. Os seus comentários são valiosos para nós (opcional):
  <ul>
  <textarea name="comments" cols="100" rows="3">
  </textarea>
  </ul>
  </li>

<input type="submit" name="save" value="<%= SAVE %>" onClick="return validateForm(document.form1,'Por favor, escolha uma das opções de avaliação.')"/> &nbsp; &nbsp; &nbsp;
<input type="submit" name="skip" value="<%= SKIP %>"/>

<input type="hidden" name="querydocid" value="<%= qd.getId() %>" />
<input type="hidden" name="offset" value="<%= ioffset %>" />
</form>

</body>
</html>
