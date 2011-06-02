package com.jhexperiment.java.absence_calendar;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.jhexperiment.java.absence_calendar.dao.Dao;
import com.jhexperiment.java.absence_calendar.model.Absence;


/**
 * Servlet to handle searching for lists of absences and returning a list.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class ServletSearchAbsences extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		if ( userService.isUserLoggedIn()){
			String searchBy = req.getParameter("searchBy");
			searchBy = (searchBy == null) ? "" : searchBy;
			String query = req.getParameter("query");
			String employmentType = req.getParameter("employmentType");
			
			Dao dao = Dao.INSTANCE;
			Gson gson = new Gson();
			
			String json = "";
			if (searchBy.equals("date")) {
				Date date = this.isValidDate(query);
				if (date != null) { // valid date found; 
					List<Absence> absenceList = dao.getAbsences(employmentType, "date", "=", date.getTime());
					json = gson.toJson(absenceList);
				}
			}
			else if (searchBy.equals("year")) {
				int year = Integer.parseInt(req.getParameter("year"));
				List<Absence> absenceList = dao.listAbsences(employmentType, "year");
				json = gson.toJson(absenceList);
				
			}
			else { // default, search by name
				List<Absence> absenceList = dao.listAbsences(employmentType, "name");
				List<Absence> searchMatchList = new ArrayList<Absence>();
				List<String> tmpList = new ArrayList<String>();
				for (Absence absence : absenceList) {
					String name = absence.getName();
					if (name.matches("(?i).*" + query + ".*") && ! tmpList.contains(name)) {
						tmpList.add(name);
						searchMatchList.add(absence);
					}
				}
				
				json = gson.toJson(searchMatchList);
			}
			
			resp.getWriter().print(json);
		}
	}
	
	/**
	 * Checks whether supplied date string is an acceptable format for this app.
	 * @param inDate
	 * @return
	 */
	private Date isValidDate(String inDate) {

	    if (inDate == null)
	      return null;

	    List<SimpleDateFormat> dateFormatList = new ArrayList<SimpleDateFormat>();
	    dateFormatList.add(new SimpleDateFormat("MM-dd-yyyy"));
	    dateFormatList.add(new SimpleDateFormat("MM.dd.yyyy"));
	    dateFormatList.add(new SimpleDateFormat("MM/dd/yyyy"));
	    dateFormatList.add(new SimpleDateFormat("MM\\dd\\yyyy"));
	    dateFormatList.add(new SimpleDateFormat("MM dd yyyy"));
	    dateFormatList.add(new SimpleDateFormat("MM-dd-yy"));
	    dateFormatList.add(new SimpleDateFormat("MM.dd.yy"));
	    dateFormatList.add(new SimpleDateFormat("MM/dd/yy"));
	    dateFormatList.add(new SimpleDateFormat("MM\\dd\\yy"));
	    dateFormatList.add(new SimpleDateFormat("MMM dd yy"));
	    dateFormatList.add(new SimpleDateFormat("MMM dd, yyyy"));
	    
	    SimpleDateFormat foundFormat;
	    for (SimpleDateFormat dateFormat : dateFormatList) {
	    	
	    	if (inDate.trim().length() != dateFormat.toPattern().length()) {
	    		
	    	}
	  	    
	  	    dateFormat.setLenient(false);
	  	    
	  	    try {
	  	      //parse the inDate parameter
	  	      Date date = dateFormat.parse(inDate.trim());
	  	      return date;
	  	    }
	  	    catch (ParseException pe) {
	  	      
	  	    }
	    }
	    
	    
	    return null;
	  }
}
