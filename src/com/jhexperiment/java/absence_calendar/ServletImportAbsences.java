package com.jhexperiment.java.absence_calendar;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.bytecode.opencsv.CSVReader;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.jhexperiment.java.absence_calendar.dao.Dao;
import com.jhexperiment.java.absence_calendar.model.Absence;


/**
 * Servlet to handle importing csv files.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class ServletImportAbsences extends HttpServlet {
	private static final Logger log = Logger.getLogger(ServletImportAbsences.class.getName());
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		UserService userService = UserServiceFactory.getUserService();
		if ( userService.isUserLoggedIn()) {
			ArrayList<HashMap<String, Object>> absenceList = new ArrayList<HashMap<String, Object>>();
			
			try {
			      ServletFileUpload upload = new ServletFileUpload();

			      FileItemIterator iterator = upload.getItemIterator(req);
			      while (iterator.hasNext()) {
			        FileItemStream item = iterator.next();
			        InputStream stream = item.openStream();
			        

			        if (item.isFormField()) {
			          log.warning("Got a form field: " + item.getFieldName());
			        } else {
			        	log.warning("Got an uploaded file: " + item.getFieldName() + ", name = " + item.getName());

						CSVReader reader = new CSVReader(new InputStreamReader(stream));
						String [] absenceInfo;
						// read header line
						String [] headers = reader.readNext();;
						while ((absenceInfo = reader.readNext()) != null) {
							absenceList.add(this.processAbsence(absenceInfo));
						}
			  		
						String tmp = "";
						tmp = tmp + "s";
						  
						
			        }	
		      	}
		    } 
			catch (Exception ex) {
		      throw new ServletException(ex);
		    } 
			finally {
		    	Gson gson = new Gson();
				String json = gson.toJson(absenceList);
				
				resp.getWriter().print(json);
		    }
			
		}
	}
	private HashMap<String, Object> processAbsence(String[] absenceInfo){
		String action = absenceInfo[0];
		// ID = absenceInfo[1]
		
		HashMap<String, Object> absenceJson = new HashMap<String, Object>();
		
		if (action.equals("update")) {
			try {
				String employmentType = absenceInfo[2];
				Date date = new Date(absenceInfo[3]);
				Timestamp timestamp = new Timestamp(date.getTime());
				Calendar cal = Calendar.getInstance();
				cal.setTime(timestamp);
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				
				boolean formSubmitted = new Boolean(absenceInfo[4]);
				String name = absenceInfo[5];
				int hours = Integer.parseInt(absenceInfo[6]);
				int rn = Integer.parseInt(absenceInfo[7]);
				
				absenceJson.put("action", action);
				absenceJson.put("employmentType", employmentType);
				absenceJson.put("date", cal.getTimeInMillis());
				absenceJson.put("rn", rn);
				absenceJson.put("name", name);
				absenceJson.put("hours", hours);
				absenceJson.put("formSubmitted", formSubmitted);
				
				Long id = new Long(absenceInfo[1]);
				absenceJson.put("id", id);
				Absence absence = new Absence(id, employmentType, cal.getTimeInMillis(), rn, name, hours, formSubmitted);
				Dao.INSTANCE.update(absence);
			}
			catch (Exception e) {
				if (e instanceof NumberFormatException) {
					absenceJson.put("error", "ID required when updating. No changes made for this absence.");
				}
				else if (e instanceof AbsenceException) {
					absenceJson.put("error", e.getMessage());
				}
			}
		}
		else if (action.equals("add")) {
			try {
				String employmentType = absenceInfo[2];
				Date date = new Date(absenceInfo[3]);
				Timestamp timestamp = new Timestamp(date.getTime());
				Calendar cal = Calendar.getInstance();
				cal.setTime(timestamp);
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				
				boolean formSubmitted = new Boolean(absenceInfo[4]);
				String name = absenceInfo[5];
				int hours = Integer.parseInt(absenceInfo[6]);
				int rn = Integer.parseInt(absenceInfo[7]);
				
				absenceJson.put("action", action);
				absenceJson.put("employmentType", employmentType);
				absenceJson.put("date", cal.getTimeInMillis());
				absenceJson.put("rn", rn);
				absenceJson.put("name", name);
				absenceJson.put("hours", hours);
				absenceJson.put("formSubmitted", formSubmitted);
				
				if (! absenceInfo[1].equals("")) {
					throw new AbsenceException("ID must be blank when adding. No changes made to this absence.");
				}
				
				Absence absence = new Absence(employmentType, cal.getTimeInMillis(), rn, name, hours, formSubmitted);
				Dao.INSTANCE.add(absence);
			}
			catch (Exception e) {
				if (e instanceof DuplicateAbsenceException) {
					absenceJson.put("error", e.getMessage());
					absenceJson.put("duplicate", true);
				}
				else if (e instanceof AbsenceException) {
					absenceJson.put("error", e.getMessage());
				}
			}
		}
		else if (action.equals("remove")) {
			try {
				Long id = new Long(absenceInfo[1]);
				absenceJson.put("id", id);
				Absence absence = Dao.INSTANCE.remove(id);
				absenceJson.put("action", action);
				absenceJson.put("employmentType", absence.getEmploymentType());
				absenceJson.put("date", absence.getDate());
				absenceJson.put("rn", absence.getRn());
				absenceJson.put("name", absence.getName());
				absenceJson.put("hours", absence.getHours());
				absenceJson.put("formSubmitted", absence.getFormSubmitted());
			}
			catch (Exception e) {
				absenceJson.put("action", action);
				absenceJson.put("date", null);
				absenceJson.put("name", "");
				
				if (e instanceof NumberFormatException) {
					absenceJson.put("error", "ID required when removing. No changes made for this absence.");
				}
				else if (e instanceof AbsenceException) {
					absenceJson.put("error", "ID: " + absenceInfo[1] + ". " + e.getMessage());
				}
			}
		}
		
		return absenceJson;
	}
	
}
