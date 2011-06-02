package com.jhexperiment.java.absence_calendar;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.*;
import com.jhexperiment.java.absence_calendar.dao.Dao;
import com.jhexperiment.java.absence_calendar.model.Absence;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle retrieving lists of absences.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class ServletGetAbsences extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		if ( userService.isUserLoggedIn()){
			String name = req.getParameter("name");
			String employmentType = req.getParameter("employmentType");
			
			List<Absence> absenceList = Dao.INSTANCE.getAbsences(employmentType, "name", "=", name);
			Gson gson = new Gson();
			String json = gson.toJson(absenceList);

			resp.getWriter().print(json);  
		}
	}
}
