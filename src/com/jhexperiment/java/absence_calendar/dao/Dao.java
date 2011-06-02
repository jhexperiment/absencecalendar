package com.jhexperiment.java.absence_calendar.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.jhexperiment.java.absence_calendar.AbsenceException;
import com.jhexperiment.java.absence_calendar.DuplicateAbsenceException;
import com.jhexperiment.java.absence_calendar.model.Absence;

/**
 * Enum to facilitate access to the persistent store.
 * @author jhxmonkey
 *
 */
public enum Dao {
	INSTANCE;
	
	/**
	 * Get full list of absences.
	 * @param employmentType
	 * @return
	 */
	public List<Absence> listAbsences(String employmentType) {
		return this.listAbsences(employmentType, "name");
	}
	
	/**
	 * Get full list of absences.
	 * @param employmentType
	 * @param orderBy
	 * @return
	 */
	public List<Absence> listAbsences(String employmentType, String orderBy) {
		EntityManager em = EMFService.get().createEntityManager();
		if (orderBy.length() != 0) {
			orderBy = "name";
		}
		String gql = "SELECT a "
					+ "FROM Absence a "
					+ "WHERE a.employmentType = :employmentType "
					+ 	"ORDER BY a." + orderBy;
		gql += ( ! orderBy.equals("date") ) ? ",a.date" : "";
		Query q = em.createQuery(gql);
		q.setParameter("employmentType", employmentType);
		
		
		List<Absence> absenceList = q.getResultList();
		return absenceList;
	}
	
	/**
	 * Get a list of absences for a given name.
	 * @param employmentType
	 * @param name
	 * @return
	 */
	public List<Absence> getAbsences(String employmentType, String name) {
		
		return getAbsences(employmentType, "name", "=", name);
	}
	
	/**
	 * Get a list of absences based on given condition.
	 * @param employmentType
	 * @param field
	 * @param opt
	 * @param val
	 * @return
	 */
	public List<Absence> getAbsences(String employmentType, String field, String opt, Object val) {
		EntityManager em = EMFService.get().createEntityManager();
		String gql = "SELECT a "
			+ "FROM Absence a "
			+ "WHERE a.employmentType = :employmentType "
			+ 	"AND a." + field + " " + opt + " :val "
			+	"ORDER BY a.name, a.date";
		Query q = em.createQuery(gql);
		q.setParameter("val", val);
		q.setParameter("employmentType", employmentType);
		
		List<Absence> absenceList = q.getResultList();
		return absenceList;
	}
	
	/**
	 * Gets an absence for a given ID.
	 * @param id
	 * @return
	 * @throws AbsenceException
	 */
	public Absence getAbsences(Long id) throws AbsenceException {
		EntityManager em = EMFService.get().createEntityManager();
		String gql = "SELECT a "
					+ "FROM Absence a "
					+ "WHERE a.id = :id";
		Query q = em.createQuery(gql);
		q.setParameter("id", id);

		List<Absence> absenceList = q.getResultList();
		if (absenceList.isEmpty()) {
			throw new AbsenceException("Absence doesn't exist.");
		}
		return absenceList.get(0);
	}
	
	/**
	 * Tests for the existence of an absence.
	 * @param absence
	 * @return
	 */
	public boolean absenceExists(Absence absence) {
		if (absence.getId() != null) {
			return absenceExists(absence.getId());
		}
		
		EntityManager em = EMFService.get().createEntityManager();
		String gql = "SELECT a "
					+ "FROM Absence a "
					+ "WHERE a.name = :name "
					+ 	"AND a.date = :date "
					+ 	"AND a.rn = :rn "
					+ 	"AND a.employmentType = :employmentType ";
		Query q = em.createQuery(gql);
		q.setParameter("name", absence.getName());
		q.setParameter("date", absence.getDate());
		q.setParameter("rn", absence.getRn());
		q.setParameter("employmentType", absence.getEmploymentType());
		
		List<Absence> absenceList = q.getResultList();
		boolean empty = absenceList.isEmpty();
		if (! empty) {
			absence.setId(absenceList.get(0).getId());
		}
		
		// if absenceList empty then absence record does not exist
		return ! empty; 
	}
	
	/**
	 * Tests for the existence of an absence.
	 * @param id
	 * @return
	 */
	public boolean absenceExists(Long id) {
		boolean exists = false;
		Absence absence = null;
		EntityManager em = EMFService.get().createEntityManager();
		absence = em.find(Absence.class, id);
		
		return absence != null; 
	}
	
	/**
	 * Adds a new absence to the persistent storage.
	 * @param employmentType
	 * @param date
	 * @param rn
	 * @param hours
	 * @param name
	 * @param formSubmitted
	 * @throws DuplicateAbsenceException
	 */
	public void add(String employmentType, Long date, Integer rn, 
					Integer hours, String name, boolean formSubmitted) 
			throws DuplicateAbsenceException {
		
		synchronized (this) {
			Absence absence = new Absence(employmentType, date, rn, name, hours, formSubmitted);
			add(absence);
		}
	}
	
	/**
	 * Adds a new absence to the persistent storage.
	 * @param absence
	 * @throws DuplicateAbsenceException
	 */
	public void add(Absence absence) throws DuplicateAbsenceException {
		synchronized (this) {
			EntityManager em = EMFService.get().createEntityManager();
			if (! absenceExists(absence)) {
				em.persist(absence);
				em.refresh(absence);
				em.close();
			}
			else {
				em.close();
				// throw duplicate absence error
				throw new DuplicateAbsenceException("Absence not added.");
			}
		}
	}
	
	/**
	 * Update an absence.
	 * 
	 * @param absence
	 * @throws AbsenceException
	 */
	public void update(Absence absence) throws AbsenceException {
		synchronized (this) {
			if (absenceExists(absence)) {
				EntityManager em = EMFService.get().createEntityManager();
				em.persist(absence);
				em.refresh(absence);
				em.close();
			}
			else {
				throw new AbsenceException("Absence doesn't exist.");
			}
		}
	}
	
	/**
	 * Remove an absence given an ID
	 * @param id
	 * @return The absence that was removed.
	 * @throws AbsenceException
	 */
	public Absence remove(Long id) throws AbsenceException {
		Absence absence = null;
		if (absenceExists(id)) {
			EntityManager em = EMFService.get().createEntityManager();
			try {
				absence = em.find(Absence.class, id);
				em.remove(absence);
			} finally {
				em.close();
			}
		}
		else {
			throw new AbsenceException("Absence doesn't exist.");
		}
		return absence;
	}
	
	/**
	 * Complete an absence when paper form is submitted.
	 * @param id
	 * @param formSubmitted
	 */
	public void complete(Long id, boolean formSubmitted) {
		EntityManager em = EMFService.get().createEntityManager();
		try {
			Absence absence = em.find(Absence.class, id);
			absence.setFormSubmitted(formSubmitted);
			em.persist(absence);
		} finally {
			em.close();
		}
	}
}
