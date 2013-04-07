package de.shop.bestellverwaltung.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.rest.UriHelperArtikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.bestellverwaltung.service.BestellungService.FetchType;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.rest.UriHelperKunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.LocaleHelper;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.Transactional;


@Path("/bestellungen")
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Transactional
@Log
public class BestellungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private HttpHeaders headers;
	
	@Inject
	private BestellungService bs;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private ArtikelService as;
	
	@Inject
	private UriHelperBestellung uriHelperBestellung;
	
	@Inject
	private UriHelperKunde uriHelperKunde;
	
	@Inject
	private UriHelperArtikel uriHelperArtikel;
	
	@Inject
	private LocaleHelper localeHelper;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Bestellung findBestellungById(@PathParam("id") Long id) {
		final Locale locale = localeHelper.getLocale(headers);
		final Bestellung bestellung = bs.findBestellungById(id, FetchType.NUR_BESTELLUNG, locale);
		if (bestellung == null) {
			final String msg = "Keine Bestellung gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}

		uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
		return bestellung;
	}
	

	@GET
	@Path("{id:[1-9][0-9]*}/lieferungen")
	public Collection<Lieferung> findLieferungenByBestellungId(@PathParam("id") Long id) {
		final Locale locale = localeHelper.getLocale(headers);
		final Bestellung bestellung = bs.findBestellungById(id, FetchType.MIT_LIEFERUNGEN, locale);
		if (bestellung == null) {
			final String msg = "Keine Bestellung gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}
		
		final Collection<Lieferung> lieferungen = bestellung.getLieferungen();
		if (lieferungen.isEmpty()) {
			final String msg = "Keine Lieferungen gefunden für die Bestellung mit der ID: " + id;
			throw new NotFoundException(msg);
		}
		
		uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
		return lieferungen;
	}

	

	@GET
	@Path("{id:[1-9][0-9]*}/kunde")
	public Kunde findKundeByBestellungId(@PathParam("id") Long id) {
		final Kunde kunde = bs.findKundeById(id);
		if (kunde == null) {
			final String msg = "Keine Bestellung gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}

		uriHelperKunde.updateUriKunde(kunde, uriInfo);
		return kunde;
	}

	
	@POST
	@Consumes(APPLICATION_JSON)
	@Produces
	public Response createBestellung(Bestellung bestellung) {
		final Locale locale = localeHelper.getLocale(headers);
		final String kundeUriStr = bestellung.getKundeUri().toString();
		int startPos = kundeUriStr.lastIndexOf('/') + 1;
		final String kundeIdStr = kundeUriStr.substring(startPos);
		Long kundeId = null;
		try {
			kundeId = Long.valueOf(kundeIdStr);
		}
		catch (NumberFormatException e) {
			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeIdStr, e);
		}
		
		final Kunde kunde = ks.findKundeById(kundeId, KundeService.FetchType.MIT_BESTELLUNGEN, locale);
		if (kunde == null) {
			throw new NotFoundException("Kein Kunde vorhanden mit der ID " + kundeId);
		}
		
		Collection<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		List<Long> artikelIds = new ArrayList<>(bestellpositionen.size());
		for (Bestellposition bp : bestellpositionen) {
			final String artikelUriStr = bp.getArtikelUri().toString();
			startPos = artikelUriStr.lastIndexOf('/') + 1;
			final String artikelIdStr = artikelUriStr.substring(startPos);
			Long artikelId = null;
			try {
				artikelId = Long.valueOf(artikelIdStr);
			}
			catch (NumberFormatException e) {
				continue;
			}
			artikelIds.add(artikelId);
		}
		
		if (artikelIds.isEmpty()) {
			final StringBuilder sb = new StringBuilder("Keine Artikel vorhanden mit den IDs: ");
			for (Bestellposition bp : bestellpositionen) {
				final String artikelUriStr = bp.getArtikelUri().toString();
				startPos = artikelUriStr.lastIndexOf('/') + 1;
				sb.append(artikelUriStr.substring(startPos));
				sb.append(" ");
			}
			throw new NotFoundException(sb.toString());
		}

		Collection<Artikel> gefundeneArtikel = as.findArtikelByIds(artikelIds);
		if (gefundeneArtikel.isEmpty()) {
			throw new NotFoundException("Keine Artikel vorhanden mit den IDs: " + artikelIds);
		}
		
		int i = 0;
		final List<Bestellposition> neueBestellpositionen = new ArrayList<>(bestellpositionen.size());
		for (Bestellposition bp : bestellpositionen) {
			final long artikelId = artikelIds.get(i++);
			
			for (Artikel artikel : gefundeneArtikel) {
				if (artikel.getId().longValue() == artikelId) {
					bp.setArtikel(artikel);
					neueBestellpositionen.add(bp);
					break;					
				}
			}
		}
		bestellung.setBestellpositionen(neueBestellpositionen);
		
		bestellung = bs.createBestellung(bestellung, kunde, locale);

		final URI bestellungUri = uriHelperBestellung.getUriBestellung(bestellung, uriInfo);
		final Response response = Response.created(bestellungUri).build();
		LOGGER.debugf(bestellungUri.toString());
		return response;
	}
	
	//TODO updateBestellung Fehler beseitigen
	@PUT
	@Consumes(APPLICATION_JSON)
	@Produces
	public void updateBestellung(Bestellung bestellung) {
		final Locale locale = localeHelper.getLocale(headers);
		Bestellung origBestellung = bs.findBestellungById(bestellung.getId(), FetchType.NUR_BESTELLUNG, locale);
		if (origBestellung == null) {
			final String msg = "Keine Bestellungen gefunden mit der ID " + bestellung.getId();
			throw new NotFoundException(msg);
		}
		LOGGER.debugf("Bestellung vorher: %s", origBestellung);
	
		origBestellung.setBestellpositionen(bestellung.getBestellpositionen());
		origBestellung.setKunde(bestellung.getKunde());
		origBestellung.setLieferungen(bestellung.getLieferungen());
		origBestellung.setStatus(bestellung.getStatus());
		for (Bestellposition bp : origBestellung.getBestellpositionen()) {
			URI artikelUri = uriHelperArtikel.getUriArtikel(bp.getArtikel(), uriInfo);
			bp.setArtikelUri(artikelUri);
		}
		uriHelperBestellung.updateUriBestellung(origBestellung, uriInfo);
		uriHelperKunde.updateUriKunde(origBestellung.getKunde(), uriInfo);
		
		LOGGER.debugf("Bestellung nachher: %s", origBestellung);
		
		bestellung = bs.updateBestellung(origBestellung, locale);
		if (bestellung == null) {
			final String msg = "Keine Bestellung gefunden mit der ID " + origBestellung.getId();
			throw new NotFoundException(msg);
		}
	} 
}
