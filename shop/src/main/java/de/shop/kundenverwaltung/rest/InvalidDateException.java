package de.shop.kundenverwaltung.rest;

import de.shop.kundenverwaltung.service.AbstractKundenverwaltungException;

public class InvalidDateException extends AbstractKundenverwaltungException {
	private static final long serialVersionUID = 2113917506853352685L;
	
	private final String invalidDate;
	
	public InvalidDateException(String invalidDate) {
		super("Ungueltiges Datum: " + invalidDate);
		this.invalidDate = invalidDate;
	}
	
	public InvalidDateException(String invalidDate, Exception e) {
		super("Ungueltiges Datum: " + invalidDate, e);
		this.invalidDate = invalidDate;
	}
	
	public String getInvalidDate() {
		return invalidDate;
	}
}
