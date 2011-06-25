package com.jhexperiment.java.absence_calendar;

import java.io.IOException;
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
 * Servlet to handle adding new absences.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class ServletAddAbsence extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		if ( userService.isUserLoggedIn()){
			Long date = new Long(req.getParameter("date"));
			int reason = Integer.parseInt(req.getParameter("reason"));
			String employmentType = req.getParameter("employmentType");
			String name = req.getParameter("name");
			float hours = Float.valueOf(req.getParameter("hours")).floatValue();
			
			
			Absence absence = new Absence(employmentType, date, reason, name, hours, false);
			
			try {
				Dao.INSTANCE.add(absence);
			}
			catch (DuplicateAbsenceException e) {
				
			}
			
			List<Absence> absenceList = Dao.INSTANCE.listAbsences(employmentType);
			
			Gson gson = new Gson();
			String json = gson.toJson(absenceList);

			resp.getWriter().print(json);  
			
		}
	}
}
