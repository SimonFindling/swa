package de.shop.kundenverwaltung.controller;

import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.SUPPORTS;
import static javax.persistence.PersistenceContextType.EXTENDED;
import static de.shop.util.Messages.MessagesType.KUNDENVERWALTUNG;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.util.Client;
import de.shop.util.File;
import de.shop.util.FileHelper;
import de.shop.util.Log;
import de.shop.util.Messages;
import de.shop.util.Transactional;

@Named("kc")
@SessionScoped
@Log
@TransactionAttribute(SUPPORTS)
public class KundeController implements Serializable {
	private static final long serialVersionUID = 6440971162286076765L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final int MAX_AUTOCOMPLETE = 10;
	
	private static final String JSF_KUNDENVERWALTUNG = "/kundenverwaltung/";
	private static final String JSF_VIEW_KUNDE = JSF_KUNDENVERWALTUNG + "viewKunde"; 
	
	private static final String MSG_KEY_KUNDE_NOT_FOUND_BY_ID = "viewKunde.notFound";
	
	private static final String CLIENT_ID_KUNDEID = "form:kundeIdInput";
		
	@PersistenceContext(type = EXTENDED)
	private transient EntityManager em;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private transient HttpServletRequest request;
	
	@Inject
	@Client
	private Locale locale;
	
	@Inject
	private Messages messages;
	
	@Inject
	private FileHelper fileHelper;
	
	
	private Long kundeId;
	private Kunde kunde;
	private String nachname;
	private List<Kunde> kundenPrefix = Collections.emptyList();

	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}

	private String findKundeByIdErrorMsg(String id) {
		messages.error(KUNDENVERWALTUNG, MSG_KEY_KUNDE_NOT_FOUND_BY_ID, CLIENT_ID_KUNDEID, id);
		return null;
	}

	public Long getKundeId() {
		return kundeId;
	}

	public void setKundeId(Long kundeId) {
		this.kundeId = kundeId;
	}

	public Kunde getKunde() {
		return kunde;
	}

	public void setKunde(Kunde kunde) {
		this.kunde = kunde;
	}
	
	public List<Kunde> getKundenPrefix() {
		return kundenPrefix;
	}

	public void setKundenPrefix(List<Kunde> kundenPrefix) {
		this.kundenPrefix = kundenPrefix;
	}

	public String getNachname() {
		return nachname;
	}

	public Date getAktuellesDatum() {
		final Date datum = new Date();
		return datum;
	}
	
	public String getFilename(File file) {
		if (file == null) {
			return "";
		}
		
		fileHelper.store(file);
		return file.getFilename();
	}

	@TransactionAttribute(REQUIRED)
	public String findKundeById() {
		kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			return findKundeByIdErrorMsg(kundeId.toString());
		}
		kundeId = null;
		return JSF_VIEW_KUNDE;
	}
	
	@TransactionAttribute(REQUIRED)
	public void loadKundeById() {
		// Request-Parameter "kundeId" fuer ID des gesuchten Kunden
		final String idStr = request.getParameter("kundeId");
		Long id;
		try {
			id = Long.valueOf(idStr);
		}
		catch (NumberFormatException e) {
			return;
		}
		
		// Suche durch den Anwendungskern
		kunde = ks.findKundeById(id, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			return;
		}
	}
	
	@TransactionAttribute(REQUIRED)
	public String details(Kunde ausgewaehlterKunde) {
		if (ausgewaehlterKunde == null) {
			return null;
		}
		
		// Bestellungen nachladen
		this.kunde = ks.findKundeById(ausgewaehlterKunde.getId(), FetchType.MIT_BESTELLUNGEN, locale);
		this.kundeId = this.kunde.getId();
		
		return JSF_VIEW_KUNDE;
	}
	
	@TransactionAttribute(REQUIRED)
	public List<Long> findKundenByIdPrefix(String idPrefix) {
		Long id = null;
		try {
			id = Long.valueOf(idPrefix);
		}
		catch (NumberFormatException e) {
			findKundeByIdErrorMsg(idPrefix);
			return null;
		}
		
		kundenPrefix = ks.findKundenByIdPrefix(id);
		if (kundenPrefix == null || kundenPrefix.isEmpty()) {
			findKundeByIdErrorMsg(idPrefix);
			return null;
		}
		
		List<Long> ids = new ArrayList<>();
		for (Kunde k : kundenPrefix) {
			ids.add(k.getId());
		}
		
		if (kundenPrefix.size() > MAX_AUTOCOMPLETE) {
			return ids.subList(0, MAX_AUTOCOMPLETE);
		}
		return ids;
	}
}
