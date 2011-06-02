package com.jhexperiment.java.absence_calendar.dao;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Service class to access the app engine persistence storage.
 * @author jhxmonkey
 *
 */
public class EMFService {
	private static final EntityManagerFactory emfInstance = Persistence
		.createEntityManagerFactory("transactions-optional");

	private EMFService() {
	}
	
	public static EntityManagerFactory get() {
		return emfInstance;
	}
}
