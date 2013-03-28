package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.Kunde;

@ApplicationException(rollback = true)
public class KundeValidationException extends AbstractKundenverwaltungException {
	private static final long serialVersionUID = 5901822410989604222L;

	private final Kunde kunde;
	private final Collection<ConstraintViolation<Kunde>> violations;
	
	public KundeValidationException(Kunde kunde, 
										Collection<ConstraintViolation<Kunde>> violations) {
		super("Ungueltiger Kunde: " + kunde + ", Violations: " + violations);
		this.kunde = kunde;
		this.violations = violations;
	}

	public Kunde getKunde() {
		return kunde;
	}

	public Collection<ConstraintViolation<Kunde>> getViolations() {
		return violations;
	}
}