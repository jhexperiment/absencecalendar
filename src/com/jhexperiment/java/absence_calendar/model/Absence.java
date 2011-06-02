package com.jhexperiment.java.absence_calendar.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Data model representing an employee's absence.
 * 
 * @author jhxmonkey
 *
 */
@Entity
public class Absence implements Comparable<Absence> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String employmentType;
	private Long date;
	private Integer rn;
	private Integer hours;
	private String name;
	private boolean formSubmitted;
	
	public Absence(String employmentType, Long date, Integer rn, String name, 
				Integer hours, boolean formSubmitted) {
		this.employmentType = employmentType;
		this.date = date;
		this.rn = rn;
		this.name = name;
		this.hours = hours;
		this.formSubmitted = formSubmitted;
	}
	
	public Absence(Long id, String employmentType, Long date, Integer rn, String name, 
			Integer hours, boolean formSubmitted) {
		this.id = id;
		this.employmentType = employmentType;
		this.date = date;
		this.rn = rn;
		this.name = name;
		this.hours = hours;
		this.formSubmitted = formSubmitted;
	}
	public int compareTo(Absence absence) {
		return this.name.compareTo(absence.getName());
	}

	public boolean equals(Object obj) {
	    if (!(obj instanceof Absence)) {
	      return false;
	    }
	    Absence absence = (Absence) obj;
	    return this.name.equals(absence.getName());
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getEmploymentType() {
		return this.employmentType;
	}
	public void setEmploymentType(String employmentType) {
		this.employmentType = employmentType;
	}
	
	public Long getDate() {
		return this.date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public Integer getRn() {
		return this.rn;
	}
	public void setRn(Integer rn) {
		this.rn = rn;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getHours() {
		return this.hours;
	}
	public void setHours(Integer hours) {
		this.hours = hours;
	}
	public boolean getFormSubmitted() {
		return this.formSubmitted;
	}
	public void setFormSubmitted(boolean formSubmitted) {
		this.formSubmitted = formSubmitted;
	}
}
