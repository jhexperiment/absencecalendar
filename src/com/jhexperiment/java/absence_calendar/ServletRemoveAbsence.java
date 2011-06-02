package com.jhexperiment.java.absence_calendar;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.jhexperiment.java.absence_calendar.dao.Dao;

/**
 * Servlet to handle removing absences.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class ServletRemoveAbsence extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		if ( userService.isUserLoggedIn()){
			try {
				Long id = Long.parseLong(req.getParameter("id"));
				Dao.INSTANCE.remove(id);
			}
			catch (Exception e) {
				
			}
		}
	}
}
