package de.shop.kundenverwaltung.service;

import static de.shop.util.AbstractDao.QueryParameter.with;
import static de.shop.util.Constants.KEINE_ID;
import static java.util.logging.Level.FINER;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.shop.kundenverwaltung.dao.KundeDao;
import de.shop.kundenverwaltung.dao.KundeDao.FetchType;
import de.shop.kundenverwaltung.dao.KundeDao.OrderType;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.util.IdGroup;
import de.shop.util.Log;
import de.shop.util.ValidationService;

@Log
public class Kundenverwaltung implements Serializable {
	private static final long serialVersionUID = 3692819050477194655L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private KundeDao dao;
	
	@Inject
	private ValidationService validationService;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-fähiges Bean {0} wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-fähiges Bean {0} wurde erzeugt", this);
	}
	
	public List<Kunde> findAllKunden(FetchType fetch, OrderType order) {
		final List<Kunde> kunden = dao.findAllKunden(fetch, order);
		return kunden;
	}
	
	public Kunde findKundeById(Long id, FetchType fetch) {
		final Kunde kunde = dao.findKundeById(id, fetch);
		return kunde;
	}
	
	public Kunde findKundeByEmail(String email) {
		final Kunde kunde = dao.findSingle(Kunde.FIND_KUNDE_BY_EMAIL, 
						with(Kunde.PARAM_KUNDE_EMAIL, email).build());
		return kunde;
	}
	
	public List<Kunde> findKundenByNachname(String nachname, FetchType fetch) {
		List<Kunde> kunden = dao.findKundenByNachname(nachname, fetch);
		return kunden;
	}

	public List<Kunde> findKundenByNachnameCriteria(String nachname) {
		final List<Kunde> kunden = dao.findKundenByNachname(nachname);
		return kunden;
	}

	public List<Kunde> findKundenByPlz(String plz) {
		final List<Kunde> kunden = dao.find(Kunde.FIND_KUNDEN_BY_PLZ, 
							with(Kunde.PARAM_KUNDE_ADRESSE_PLZ, plz).build());
		return kunden;
	}
	
	private void validateKunde(Kunde kunde, Locale locale, Class<?>... groups) {
		final Validator validator = validationService.getValidator(locale);
		
		final Set<ConstraintViolation<Kunde>> violations = validator.validate(kunde, groups);
		if (!violations.isEmpty()) {
			throw new KundeValidationException(kunde, violations);
		}
	}

	public Kunde createKunde(Kunde kunde, Locale locale) {
		if (kunde == null) {
			return kunde;
		}
		
		validateKunde(kunde, locale, Default.class, PasswordGroup.class);
		
		final Kunde vorhandenerKunde = findKundeByEmail(kunde.getEmail());
		
		if (vorhandenerKunde != null) {
			throw new EmailExistsException(kunde.getEmail());
		}
		LOGGER.finest("Email-Adresse existiert noch nicht");
		
		kunde.setId(KEINE_ID);
		kunde = dao.create(kunde);
		
		return kunde;
	}
	
	public Kunde updateKunde(Kunde kunde, Locale locale) {
		if (kunde == null) {
			return kunde;
		}	
		
		validateKunde(kunde, locale, Default.class, PasswordGroup.class, IdGroup.class);
		
		final Kunde vorhandenerKunde = findKundeByEmail(kunde.getEmail());
		if (vorhandenerKunde != null && vorhandenerKunde.getId().longValue() != kunde.getId().longValue()) {
			throw new EmailExistsException(kunde.getEmail());
		}
		LOGGER.finest("Email-Adresse existiert noch nicht");
		
		kunde = dao.update(kunde);
		return kunde;
	}
}