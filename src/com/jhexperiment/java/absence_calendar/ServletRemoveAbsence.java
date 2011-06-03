package com.jhexperiment.java.absence_calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
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
			String json = "";
			Gson gson = new Gson();
			try {
				String name =  req.getParameter("name");
				String employmentType =  req.getParameter("employmentType");
				String idString = req.getParameter("id");
				if ("".equals(idString) || idString == null) {
					json = gson.toJson(Dao.INSTANCE.remove(employmentType, name));
				}
				else {
					Long id = new Long(idString);
					json = gson.toJson(Dao.INSTANCE.remove(id));
				}
				
			}
			catch (NumberFormatException e) {
				json = gson.toJson("Missing id.");
			}
			catch (Exception e) {
				int tmp = 0;
				tmp++;
			}
			
			resp.getWriter().print(json);
		}
	}
}
