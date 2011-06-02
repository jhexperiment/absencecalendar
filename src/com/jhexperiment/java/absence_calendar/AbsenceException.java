package com.jhexperiment.java.absence_calendar;

/**
 * Exception to manage general exceptions for this app.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class AbsenceException extends Exception {
	
	public AbsenceException() {
		super("Absence error.");
	}
	public AbsenceException(String msg) {
		super(msg);
	}
}
