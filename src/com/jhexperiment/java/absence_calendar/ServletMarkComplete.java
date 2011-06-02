package com.jhexperiment.java.absence_calendar;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.jhexperiment.java.absence_calendar.dao.Dao;

/**
 * Servlet to mark absences as having their form submitted.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class ServletMarkComplete extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		if ( userService.isUserLoggedIn()){
		
			Long id = new Long(req.getParameter("id"));
			boolean formSubmitted = "true".equals(req.getParameter("formSubmitted"));
			
			Dao dao = Dao.INSTANCE;
			dao.complete(id, formSubmitted);
		}
	}
}
