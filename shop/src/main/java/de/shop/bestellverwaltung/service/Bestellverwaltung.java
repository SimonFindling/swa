package de.shop.bestellverwaltung.service;

import static de.shop.util.AbstractDao.QueryParameter.with;
import static de.shop.util.Constants.KEINE_ID;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.shop.artikelverwaltung.dao.ArtikelDao;
import de.shop.bestellverwaltung.dao.BestellungDao;
import de.shop.bestellverwaltung.dao.LieferungDao;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.kundenverwaltung.dao.KundeDao;
import de.shop.kundenverwaltung.dao.KundeDao.FetchType;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.Kundenverwaltung;
import de.shop.util.Log;
import de.shop.util.ValidationService;

@Log
public class Bestellverwaltung  implements Serializable {
	private static final long serialVersionUID = -5816249017416603515L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private Kundenverwaltung kv;
	
	@Inject
	private BestellungDao dao;
	
	@Inject
	private KundeDao kundeDao;
	
	@Inject
	private ArtikelDao artikelDao;
	
	@Inject
	private LieferungDao lieferungDao;
	
	@Inject
	private ValidationService validationService;
	
	@Inject
	@NeueBestellung
	private transient Event<Bestellung> event;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
	}
	
	
	public Bestellung findBestellungById(Long id) {
		final Bestellung bestellung = dao.find(id);
		return bestellung;
	}


	
	public Bestellung findBestellungenByIdFetchLieferungen(Long id) {
		final Bestellung bestellung = dao.findSingle(Bestellung.FIND_BESTELLUNG_BY_ID_FETCH_LIEFERUNGEN,
				                                     with(Bestellung.PARAM_ID, id).build());
		return bestellung;
	}

	
	public Kunde findKundeById(Long id) {
		final Kunde kunde = kundeDao.findSingle(Bestellung.FIND_KUNDE_BY_ID,
                                                   with(Bestellung.PARAM_ID, id).build());
		return kunde;
	}


	public List<Bestellung> findBestellungenByKunde(Long id) {
		final List<Bestellung> bestellungen = dao.find(Bestellung.FIND_BESTELLUNGEN_BY_KUNDE,
                                                       with(Bestellung.PARAM_KUNDE_ID, id).build());
		return bestellungen;
	}
	
	
	public List<Bestellung> findBestellungenByArtikel(Long aid) {
		final List<Bestellung> bestellungen = dao.find(Bestellung.FIND_BESTELLUNGEN_BY_ARTIKEL_ID,
                                                       with(Bestellung.PARAM_ARTIKEL_ID, aid).build());
		return bestellungen;
	}


	
	public Bestellung createBestellung(Bestellung bestellung,
			                           Kunde kunde,
			                           Locale locale) {
		if (bestellung == null) {
			return null;
		}
		
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			LOGGER.log(FINEST, "Bestellposition: {0}", bp);				
		}

		kunde = kv.findKundeById(kunde.getId(), FetchType.MIT_BESTELLUNGEN);
		kunde.addBestellung(bestellung);
		bestellung.setKunde(kunde);
		

		bestellung.setId(KEINE_ID);
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			bp.setId(KEINE_ID);
		}
		
		validateBestellung(bestellung, locale, Default.class);
		dao.create(bestellung);
		event.fire(bestellung);

		return bestellung;
	}
	
	public Bestellung updateBestellung(Bestellung bestellung, Locale locale) {
		if (bestellung == null) {
			return null;
		}
		
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			LOGGER.log(FINEST, "Bestellposition: {0}", bp);				
		}
		
		validateBestellung(bestellung, locale);
		dao.update(bestellung);
		
		return bestellung;
	}
	
	private void validateBestellung(Bestellung bestellung, Locale locale, Class<?>... groups) {
		final Validator validator = validationService.getValidator(locale);
		
		final Set<ConstraintViolation<Bestellung>> violations = validator.validate(bestellung);
		if (violations != null && !violations.isEmpty()) {
			LOGGER.exiting("Bestellverwaltung", "createBestellung", violations);
			throw new BestellungValidationException(bestellung, violations);
		}
	}



	public List<Lieferung> findLieferungen(String nr) {
		final List<Lieferung> lieferungen =
				              lieferungDao.find(Lieferung.FIND_LIEFERUNGEN_BY_LIEFERNR_FETCH_BESTELLUNGEN,
                                                with(Lieferung.PARAM_LIEFERNR, nr).build());
		
		return lieferungen;
	}

	
	public Lieferung createLieferung(Lieferung lieferung, List<Bestellung> bestellungen) {
		if (lieferung == null || bestellungen == null || bestellungen.isEmpty()) {
			return null;
		}
		
		final List<Long> ids = new ArrayList<>();
		for (Bestellung b : bestellungen) {
			ids.add(b.getId());
		}
		
		bestellungen = dao.findBestellungenByIdFetchLieferungen(ids);
		lieferung.setBestellungen(bestellungen);
		for (Bestellung bestellung : bestellungen) {
			bestellung.addLieferung(lieferung);
		}
		
		lieferung.setId(KEINE_ID);
		lieferung = lieferungDao.create(lieferung);
		
		return lieferung;
	}
}
