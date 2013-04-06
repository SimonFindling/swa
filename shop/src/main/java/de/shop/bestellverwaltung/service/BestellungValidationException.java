package de.shop.bestellverwaltung.service;

import java.util.Collection;
import java.util.Date;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.Kunde;


@ApplicationException(rollback = true)
public class BestellungValidationException extends AbstractBestellverwaltungException {
	private static final long serialVersionUID = -4634273195424451330L;
	
	private final Date erzeugt;
	private final Long kundeId;
	private final Collection<ConstraintViolation<Bestellung>> violations;
	
	public BestellungValidationException(Bestellung bestellung,
			                             Collection<ConstraintViolation<Bestellung>> violations) {
		super(violations.toString());
		
		if (bestellung == null) {
			this.erzeugt = null;
			this.kundeId = null;
		}
		else {
			this.erzeugt = bestellung.getErzeugt();
			final Kunde kunde = bestellung.getKunde();
			this.kundeId = kunde == null ? null : kunde.getId();
		}
		
		this.violations = violations;
	}
	
	public Date getErzeugt() {
		return erzeugt == null ? null : (Date) erzeugt.clone();
	}
	
	public Long getKundeId() {
		return kundeId;
	}
	
	public Collection<ConstraintViolation<Bestellung>> getViolations() {
		return violations;
	}
}
