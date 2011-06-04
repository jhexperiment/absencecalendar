package com.jhexperiment.java.absence_calendar;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
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
			
			String employmentType = req.getParameter("employmentType");
			
			ArrayList<HashMap<String, Object>> absenceList = new ArrayList<HashMap<String, Object>>();
			
			Properties appProps = new Properties();
			String path = this.getServletContext().getRealPath("/WEB-INF");
			FileInputStream appPropFile = new FileInputStream(path + "/app.properties");
			appProps.load(appPropFile);        
			
			try {
				int importRecordLimit = Integer.parseInt(appProps.getProperty("importRecordLimit"));
				ServletFileUpload upload = new ServletFileUpload();

				FileItemIterator iterator = upload.getItemIterator(req);
				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					
					if (item.isFormField()) {
					  if ("employmentType".equals(item.getFieldName())) {
						  InputStream stream = item.openStream();
						  employmentType = Streams.asString(stream);;
			          }
			        } else {
			        	log.warning("Got an uploaded file: " + item.getFieldName() + ", name = " + item.getName());
			        	InputStream stream = item.openStream();
						CSVReader reader = new CSVReader(new InputStreamReader(stream));
						String [] absenceInfo;
						// read header line
						String [] headers = reader.readNext();;
						int count = 0;
						while ((absenceInfo = reader.readNext()) != null) {
							absenceList.add(this.processAbsence(absenceInfo));
							count++;
							if (count >= importRecordLimit) {
								String errMsg = "Import record limit (" + importRecordLimit + ") reached. ";
								HashMap<String, Object> absenceJson = new HashMap<String, Object>();
								absenceJson.put("action", "");
								absenceJson.put("id", 0);
								absenceJson.put("employmentType", "");
								absenceJson.put("date", new Date().getTime());
								absenceJson.put("name", "");
								absenceJson.put("formSubmitted", "");
								absenceJson.put("hours", 0);
								absenceJson.put("reason", 0);
								absenceJson.put("error", errMsg);
								
								absenceList.add(absenceJson);
								throw new AbsenceException(errMsg);
							}
						}
			        }	
		      	}
		    } 
			catch (Exception e) {
				if (e instanceof AbsenceException) {
					
				}
				else {
					throw new ServletException(e);
				}
		    } 
			finally {
				HashMap<String, Object> info = new HashMap<String, Object>();
				info.put("absenceList", absenceList);
				info.put("employeeList", Dao.INSTANCE.listEmployees(employmentType));
		    	Gson gson = new Gson();
				String json = gson.toJson(info);
				
				resp.getWriter().print(json);
		    }
			
		}
	}
	
	private HashMap<String, Object> processAbsence(String[] absenceInfo){
		String action = absenceInfo[0];
		// ID = absenceInfo[1]
		
		HashMap<String, Object> absenceJson = new HashMap<String, Object>();
		
		Absence absence = null;
		if (action.equals("update")) {
			try {
				absenceJson.put("action", action);
				absenceJson.put("id", absenceInfo[1]);
				absenceJson.put("employmentType", absenceInfo[2]);
				absenceJson.put("date", absenceInfo[3]);
				absenceJson.put("name", absenceInfo[5]);
				absenceJson.put("formSubmitted", absenceInfo[4]);
				absenceJson.put("hours", absenceInfo[6]);
				absenceJson.put("reason", absenceInfo[7]);
				
				absence = Dao.INSTANCE.isValidAbsence(action, absenceInfo[1], absenceInfo[2],
						absenceInfo[3], absenceInfo[7], absenceInfo[5], absenceInfo[6], absenceInfo[4]);
				absenceJson.put("date", absence.getDate());
				Dao.INSTANCE.update(absence);
			}
			catch (Exception e) {
				if (e instanceof AbsenceException) {
					absenceJson.put("error", e.getMessage());
				}
			}
		}
		else if (action.equals("add")) {
			try {
				absenceJson.put("action", action);
				absenceJson.put("id", absenceInfo[1]);
				absenceJson.put("employmentType", absenceInfo[2]);
				absenceJson.put("date", absenceInfo[3]);
				absenceJson.put("name", absenceInfo[5]);
				absenceJson.put("formSubmitted", absenceInfo[4]);
				absenceJson.put("hours", absenceInfo[6]);
				absenceJson.put("reason", absenceInfo[7]);
				
				absence = Dao.INSTANCE.isValidAbsence(action, absenceInfo[1], absenceInfo[2],
						absenceInfo[3], absenceInfo[7], absenceInfo[5], absenceInfo[6], absenceInfo[4]);
				
				Dao.INSTANCE.add(absence.getEmploymentType(), absence.getDate(), absence.getReason(), 
								absence.getHours(), absence.getName(), absence.getFormSubmitted());
				
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
				absence = Dao.INSTANCE.remove(id);
				absenceJson.put("action", action);
				absenceJson.put("employmentType", absence.getEmploymentType());
				absenceJson.put("date", absence.getDate());
				absenceJson.put("reason", absence.getReason());
				absenceJson.put("name", absence.getName());
				absenceJson.put("hours", absence.getHours());
				absenceJson.put("formSubmitted", absence.getFormSubmitted());				
			}
			catch (Exception e) {
				absenceJson.put("action", action);
				absenceJson.put("date", null);
				absenceJson.put("name", "");
				
				if (e instanceof NumberFormatException) {
					absenceJson.put("error", "Error: ID required when removing. ");
				}
				else if (e instanceof AbsenceException) {
					absenceJson.put("error", "Error: ID: " + absenceInfo[1] + ". " + e.getMessage());
				}
			}
		}
		else if ("".equals(action) || action == null) {
			absenceJson.put("action", action);
			absenceJson.put("id", absenceInfo[1]);
			absenceJson.put("employmentType", absenceInfo[2]);
			absenceJson.put("date", absenceInfo[3]);
			absenceJson.put("name", absenceInfo[5]);
			absenceJson.put("formSubmitted", absenceInfo[4]);
			absenceJson.put("hours", absenceInfo[6]);
			absenceJson.put("reason", absenceInfo[7]);
			
			absenceJson.put("error", "Error: An action is required. ");
		}
		else {
			absenceJson.put("action", action);
			absenceJson.put("id", absenceInfo[1]);
			absenceJson.put("employmentType", absenceInfo[2]);
			absenceJson.put("date", absenceInfo[3]);
			absenceJson.put("name", absenceInfo[5]);
			absenceJson.put("formSubmitted", absenceInfo[4]);
			absenceJson.put("hours", absenceInfo[6]);
			absenceJson.put("reason", absenceInfo[7]);
			
			absenceJson.put("error", "Error: Unknown action. ");
		}
		
		return absenceJson;
	}
	
}
