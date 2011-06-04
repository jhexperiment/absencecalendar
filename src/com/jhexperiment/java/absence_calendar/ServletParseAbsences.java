package com.jhexperiment.java.absence_calendar;

import java.io.IOException;
import java.util.logging.Logger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.jhexperiment.java.absence_calendar.dao.Dao;
import com.jhexperiment.java.absence_calendar.model.Absence;


/**
 * Servlet to handle parsing import from Budget program.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class ServletParseAbsences extends HttpServlet {
	private static final Logger log = Logger.getLogger(ServletParseAbsences.class.getName());

	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		if ( userService.isUserLoggedIn()){
			String username = userService.getCurrentUser().getEmail();
			String rawText = req.getParameter("rawText");
			int year = Integer.parseInt(req.getParameter("year"));
			String employmentType = req.getParameter("employmentType");
			
			String[] lines = rawText.split("\n");
			String regex = "^([_ 0-9]+) (\\w) (\\d{3}) "
						 + "(\\d{2}/\\d{2} \\d{2}:\\d{2}) (\\d{2}/\\d{2} \\d{2}:\\d{2}) "
						 + "(\\d{2}) (\\d{5,8})? (\\d*) (.*)$";
			Pattern pattern = Pattern.compile(regex);
			Calendar cal = Calendar.getInstance();
			DateFormat formatter = new SimpleDateFormat("M/d H:m");
			
			ArrayList<HashMap<String, Object>> jsonAbsenceList = new ArrayList<HashMap<String, Object>>();
			for (String line : lines) {
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					Long date = new Long(0);
					try {
						Timestamp timestamp = new Timestamp(formatter.parse(matcher.group(4)).getTime());
						cal.setTime(timestamp);
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.HOUR, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						
						date = cal.getTimeInMillis();
					} catch (ParseException e) {
					}
					
					int reason = Integer.parseInt(matcher.group(6));
					String name = checkNull(matcher.group(9).trim());
					
					Absence absence = new Absence(employmentType, date, reason, name, 7, false);
					HashMap<String, Object> absenceJson = new HashMap<String, Object>();
					absenceJson.put("action", "add");
					absenceJson.put("employmentType", employmentType);
					absenceJson.put("date", cal.getTimeInMillis());
					absenceJson.put("reason", reason);
					absenceJson.put("name", name);
					absenceJson.put("hours", 7);
					absenceJson.put("formSubmitted", false);
					try {
						Dao.INSTANCE.add(absence);
					}
					catch (DuplicateAbsenceException e) {
						absenceJson.put("error", e.getMessage());
						absenceJson.put("duplicate", true);
					}
					jsonAbsenceList.add(absenceJson);
							
					
				}
				
			}
			String json = "";
        	Gson gson = new Gson();
			json = gson.toJson(jsonAbsenceList);
	        
			resp.getWriter().print(json);
		}
		
	}
	
	private String checkNull(String s) {
		if (s == null) {
			return "";
		}
		return s;
	}

}
