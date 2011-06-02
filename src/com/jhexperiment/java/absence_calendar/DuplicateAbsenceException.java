package com.jhexperiment.java.absence_calendar;

/**
 * Exception used to inform that the absence being performed on is a duplicate.
 * @author jhxmonkey
 *
 */
@SuppressWarnings("serial")
public class DuplicateAbsenceException extends Exception {
	
	public DuplicateAbsenceException() {
	    super("Duplicate absence.");
	}
	
	public DuplicateAbsenceException(String msg) {
	    super(msg);
	}
	
}
