<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.io.FileNotFoundException" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.InputStream"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Properties"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList"%>
<%@ page import="javax.servlet.ServletConfig"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.jhexperiment.java.absence_calendar.model.Absence" %>
<%@ page import="com.jhexperiment.java.absence_calendar.dao.Dao" %>
<%@ page import="com.jhexperiment.java.absence_calendar.google.GoogleWrapper" %>

<!DOCTYPE html>

<html>
	<head>
		<title>Absences</title>
		<script type="text/javascript" src="/js/jquery/jquery.js"></script>
		<script type="text/javascript" src="/js/jquery/jquery-ui.js"></script>
		<script type="text/javascript" src="/js/jquery/plugins/jquery.form.js"></script>
		<script type="text/javascript" src="/js/jquery/plugins/jquery.tools.min.js"></script>
		<script type="text/javascript" src="/js/jquery/plugins/jquery.dateFormat.js"></script>
		<script type="text/javascript" src="/js/jquery/plugins/hoverIntent.js"></script>
		<script type="text/javascript" src="/js/common.js"></script>
		<script type="text/javascript" src="/js/main.js"></script>
		<link rel="stylesheet" type="text/css" href="css/jquery/jquery-ui.css"/>
		<link rel="stylesheet" type="text/css" href="css/main.css"/>
		<meta charset="utf-8"> 
	</head>
	<body class="teacherBody">
		
<%
// Get logged in user.
UserService userService = UserServiceFactory.getUserService();
User user = userService.getCurrentUser();



ArrayList<String> userList = new ArrayList<String>();
Properties appProps = new Properties();
Properties googleProps = new Properties();
try {
	String path = this.getServletContext().getRealPath("/WEB-INF");
	
	// Get google properties
	FileInputStream googlePropFile = new FileInputStream(path + "/google.properties");
	googleProps.load(googlePropFile);        
	
	// Get genral app properties
	FileInputStream appPropFile = new FileInputStream(path + "/app.properties");
	appProps.load(appPropFile);        
	
	String authorizedGroup = googleProps.getProperty("authorizedGroup");
	// Get list of users authorized to access app
	userList = GoogleWrapper.INSTANCE.getUserList(authorizedGroup, googleProps);
}
catch (Exception e) {
	if (e instanceof FileNotFoundException) {
		// no google.properties file found.
		response.sendRedirect("404.html"); 
	}
	if (e instanceof IOException) {
		response.sendRedirect("404.html");
	}
}

// Check that user has logged in.
String url = userService.createLoginURL(request.getRequestURI());
if ( userService.isUserLoggedIn() ){
	// user IS logged in, genertate logout url
	url = userService.createLogoutURL(request.getRequestURI()); // logout url
	if (! userList.contains(user.getEmail())) {
		// redirect to logout url (log user out) if not authorized to view app
		response.sendRedirect(url); 
	}
}
else {
	// user is NOT logged in, redirect to login screen
	response.sendRedirect(url);  
}

// Passed all validations, load app.
Dao dao = Dao.INSTANCE;
List<Absence> absenceList = new ArrayList<Absence>();
absenceList = dao.listAbsences("Teachers");
List<String> tmpList = new ArrayList<String>();

%>
	<input type="hidden" id="importRecordLimit" value="<%= appProps.getProperty("importRecordLimit") %>">
	<div id="appNavBar">
		<div class="appNavLeftItem">&nbsp;</div>
		<div id="absencesNavItem" class="currentNavItem appNavLeftItem">
			<a href="/">Absences</a>
		</div>
		<div class="appNavLeftItem">&nbsp;</div>
		<div id="moreNavItem" class="appNavLeftItem">
			<div style="float:left;">Extras</div> 
			<span class="ui-corner-all">
				<span class="ui-icon ui-icon-triangle-1-s"></span>
			</span>
		</div>
		
		<div class="appNavRightItem">&nbsp;</div>
		<div id="userNavItem" class="appNavRightItem">
			<input class="logoutUrl" type="hidden" value="<%=url%>">
			<%=user.getEmail() %>
			<span class="ui-corner-all">
				<span class="ui-icon ui-icon-triangle-1-s"></span>
			</span>
		</div>
	</div>
	<div id="header">
		<div id="logo"><img alt="logo" src="/images/maili_banner_09.jpg"/></div>
		<div id="searchBar">
			<input id="searchQuery" type="text"/>
			<button id="searchButton" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button" aria-disabled="false">
				<span class="">Search</span>
			</button>
			<img src="images/loader-small.gif" style="display:none;">
		</div>
		<!-- 
		<div id="categorySelector" class="circleWidget">
			<div class="outerRing">
				<div class="innerRing">
					<div class="oneThirdItem"></div>
					<div class="oneThirdItem"></div>
					<div class="oneThirdItem"></div>
				</div>
			</div>
		</div>
		 -->
	</div>
	
	<div id="searchResultsContainer">
		<div class="label ui-widget ui-widget-header">
				&nbsp;
				<span class="text">Search Results:</span>
				<div class="title">
					<div class="text teacherView">Teachers</div>
					<span class="ui-corner-all">
						<span class="ui-icon ui-icon-triangle-1-s"></span>
					</span>
				</div>
				<span id="icon" class="ui-state-default ui-corner-all">
					<span class="ui-icon ui-icon-arrowthickstop-1-s"></span>
				</span>
		</div>
		<div class="tableContainer ui-widget ui-widget-content">
			<table id="searchResults">
				
			</table>
		</div>
	</div>
		
	<div id="calendarHeader">
		<div class="header ui-widget-header">
			<span class="text">Paste copy from budget:</span>
			<button id="importButton" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button" aria-disabled="false">
				<span class="">Import</span>
			</button>
			<div class="title">Master Calendar</div>
			<div id="calendarSelect">
				<select>
					<option value="">Master Calendar</option>
<% 
for (Absence absence : absenceList) {
	String name = absence.getName();
	if (! tmpList.contains(name)) {
		tmpList.add(name);
%>
					<option value="<%= name %>"><%= name %></option>
<%
	}
} 
%>
				</select>
			</div>
			<img src="images/loader-bar.gif" style="display:none;">
		</div>
		<textarea class="rawTextInput" name="rawText"></textarea>
		
	</div>
	<div id="content">
			
		<div id="calendar" class="ui-widget-content ui-corner-bottom">
			<input id="selectedMonth" type="hidden" value="">
			<input id="selectedDay" type="hidden" value="">
			<button id="removeCalanderButton" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button">
				<span class="">Remove Employee</span>
				<span class="ui-corner-all">
					<span class="ui-icon ui-icon-trash"></span>
				</span>
			</button>
			
<%
Calendar calendar = Calendar.getInstance();

String[] monthList = {"January", "February", "March", "April", "May", "June", "July",
											"August", "September", "October", "November", "December"};

int year = calendar.get(Calendar.YEAR);
int prevYear = year - 1;
int nextYear = year + 1;
%>			
			<div class="year ui-widget-header ui-corner-all">
				<span class="ui-corner-all">
					<span class="ui-icon ui-icon-circle-triangle-w"></span>
				</span>
				<span class="text"><%= year %></span>
				<span class="ui-corner-all">
					<span class="ui-icon ui-icon-circle-triangle-e"></span>
				</span>
			</div>
			<br/>
<% for (int i = 0; i < 12; i++) {
			calendar.set(year, i, 1);
			int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			int firstDay = calendar.get(Calendar.DAY_OF_WEEK);
%>
			<div class="month ui-widget-content ui-corner-all" id="month_<%= i %>">	
				<h1 class="ui-widget-header ui-corner-top"><%= monthList[i] %></h1>
				<div class="headerContainer">
					<div class="header">Su</div>
					<div class="header">Mo</div>
					<div class="header">Tu</div>
					<div class="header">We</div>
					<div class="header">Th</div>
					<div class="header">Fr</div>
					<div class="header">Sa</div>
				</div>
				<br/>
<% 		for (int j = 1; j < firstDay; j++) { %>
				<div class="day dayPlaceHolder"><div class="mask">&nbsp;</div></div>
<% 		} %>		
			
<% 		for (int j = 1; j <= lastDay; j++) { 
				calendar.set(year, i, j);
				int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
				String weekend = (weekDay == 7) || (weekDay == 1) ? "weekend" : "";
%>
			
					<div class="day dayOfMonth <%=weekend %>" id="day_<%= j %>">
						<input class="absenceId" type="hidden" value="">
						<div class="mask">
							<%= j %>
						</div>
						<div class="tooltip">
							<div class="tooltip-content"></div>
							<div class="tooltip-arrow-border"></div>
	  					<div class="tooltip-arrow"></div>
	  				</div>
					</div>
			
<% 		} %>		
				</div>
<% } %>
		</div>
	</div>

	<div id="footer">
		&copy; 2011 A <a href="http://www.jhexperiment.com">JH Experiment</a>&nbsp;&nbsp;&nbsp;&nbsp;
	</div>
	
	<div id="addAbsenceDialog" title="">
		<input class="year" type="hidden" value="">
		<input class="month" type="hidden" value="">
		<input class="day" type="hidden" value="">
		<table>
			<tr>
				<th>Date:</th>
				<td class="date">&nbsp;</td>
			</tr>
			<tr>
				<th>Type:</th>
				<td class="employmentType">&nbsp;</td>
			</tr>
			<tr>
				<th>Name:</th>
				<td id="nameField">
					<select></select>
					<input class="name" type="text">
					<span id="nameError" class="ui-corner-all ui-state-error" title="">
						<span class="ui-icon ui-icon-alert"></span>
					</span>
					<div class="tooltip">
						<div class="tooltip-content"></div>
						<div class="tooltip-arrow-border"></div>
  					<div class="tooltip-arrow"></div>
  				</div>
				</td>
			</tr>
			<tr>
				<th>RN:</th>
				<td id="rnField">
					<input class="rn" type="text">
					<span id="rnError" class="ui-corner-all ui-state-error" title="">
						<span class="ui-icon ui-icon-alert"></span>
					</span>
					<div class="tooltip">
						<div class="tooltip-content"></div>
						<div class="tooltip-arrow-border"></div>
  					<div class="tooltip-arrow"></div>
  				</div>
				</td>
			</tr>
			<tr>
				<th>Hours:</th>
				<td id="hoursField">
					<input class="hours" type="text">
					<span id="hoursError" class="ui-corner-all ui-state-error" title="">
						<span class="ui-icon ui-icon-alert"></span>
					</span>
					<div class="tooltip">
						<div class="tooltip-content"></div>
						<div class="tooltip-arrow-border"></div>
  					<div class="tooltip-arrow"></div>
  				</div>
				</td>
			</tr>
		</table>
	</div>
	
	
	
</body>
</html>