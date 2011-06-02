package com.jhexperiment.java.absence_calendar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.jhexperiment.java.absence_calendar.dao.Dao;
import com.jhexperiment.java.absence_calendar.model.Absence;


/**
 * Servlet to handle exporting absences to a csv file.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class ServletExportAbsences extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		if ( userService.isUserLoggedIn()){
			String employmentType = req.getParameter("employmentType");
			
			Dao dao = Dao.INSTANCE;
			List<Absence> absenceList = dao.listAbsences(employmentType, "name");
			String header = "action,id,employmentType,date,formSubmitted,name,hours,rn\n";
			String data = "";
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
			
			for (Absence absence : absenceList) {
				data += "update,"
					+ absence.getId() + ","
					+ '"' + absence.getEmploymentType() + "\","
					+ '"' + formatter.format(absence.getDate()) + "\","
					+ absence.getFormSubmitted() + ","
					+ '"' + absence.getName() + "\"," 
					+ absence.getHours() + ","
					+ absence.getRn() + "\n";
			}
			
			String filename = employmentType.replace(' ', '-').toLowerCase();
			resp.setHeader("Content-Type","application/vnd.ms-excel; name='excel'");
			resp.addHeader("Content-Disposition","filename=" + filename + ".csv");
			resp.addHeader("Pragma","no-cache");
			resp.addHeader("Expires","0");
			resp.getWriter().print(header + data);
		}
	}
	
}
