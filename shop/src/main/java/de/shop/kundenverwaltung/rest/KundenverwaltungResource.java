package de.shop.kundenverwaltung.rest;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.rest.UriHelperBestellung;
import de.shop.bestellverwaltung.service.Bestellverwaltung;
import de.shop.kundenverwaltung.dao.KundeDao.FetchType;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.service.Kundenverwaltung;
import de.shop.util.Log;
import de.shop.util.NotFoundException;


@Path("/kunden")
@Produces({ APPLICATION_XML, TEXT_XML, APPLICATION_JSON })
@Consumes
@RequestScoped
@Log
public class KundenverwaltungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String VERSION = "1.0";
	
	@Inject
	private Kundenverwaltung kv;
	
	@Inject
	private Bestellverwaltung bv;
	
	@Inject
	private UriHelperKunde uriHelperKunde;
	
	@Inject
	private UriHelperBestellung uriHelperBestellung;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
	}
	
	@GET
	@Produces(TEXT_PLAIN)
	@Path("version")
	public String getVersion() {
		return VERSION;
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}")
	@Formatted
	public Kunde findKundeById(@PathParam("id") Long id, @Context UriInfo uriInfo) {
		final Kunde kunde = kv.findKundeById(id, FetchType.NUR_KUNDE);
		if (kunde == null) {
			final String msg = "Kein Kunde gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}
	
		uriHelperKunde.updateUriKunde(kunde, uriInfo);
		return kunde;
	}
	
	@GET
	@Wrapped(element = "kunden")
	public Collection<Kunde> findKundenByNachname(@QueryParam("nachname") @DefaultValue("") String nachname,
			                                              @Context UriInfo uriInfo) {
		Collection<Kunde> kunden = null;
		if ("".equals(nachname)) {
			kunden = kv.findAllKunden(FetchType.NUR_KUNDE, null);
			if (kunden.isEmpty()) {
				final String msg = "Keine Kunden vorhanden";
				throw new NotFoundException(msg);
			}
		}
		else {
			kunden = kv.findKundenByNachname(nachname, FetchType.NUR_KUNDE);
			if (kunden.isEmpty()) {
				final String msg = "Kein Kunde gefunden mit Nachname " + nachname;
				throw new NotFoundException(msg);
			}
		}

		for (Kunde kunde : kunden) {
			uriHelperKunde.updateUriKunde(kunde, uriInfo);
		}	
		return kunden;
	}

	@GET
	@Path("{id:[1-9][0-9]*}/bestellungen")
	@Wrapped(element = "bestellungen")
	public Collection<Bestellung> findBestellungenByKundeId(@PathParam("id") Long kundeId,  @Context UriInfo uriInfo) {
		final Collection<Bestellung> bestellungen = bv.findBestellungenByKunde(kundeId);
		if (bestellungen.isEmpty()) {
			final String msg = "Kein Kunde gefunden mit der ID " + kundeId;
			throw new NotFoundException(msg);
		}
		
		for (Bestellung bestellung : bestellungen) {
			uriHelperBestellung.updateUriBestellung(bestellung, uriInfo);
		}
		
		return bestellungen;
	}
	
	@POST
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public Response createKunde(Kunde kunde, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final Adresse adresse = kunde.getAdresse();
		if (adresse != null) {
			adresse.setKunde(kunde);
		}
		
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		kunde = kv.createKunde(kunde, locale);
		LOGGER.log(FINEST, "Kunde: {0}", kunde);
		
		final URI kundeUri = uriHelperKunde.getUriKunde(kunde, uriInfo);
		return Response.created(kundeUri).build();
	}
	
	@PUT
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	public void updateKunde(Kunde kunde, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		Kunde origKunde = kv.findKundeById(kunde.getId(), FetchType.NUR_KUNDE);
		if (origKunde == null) {
			final String msg = "Kein Kunde gefunden mit der ID " + kunde.getId();
			throw new NotFoundException(msg);
		}
		LOGGER.log(FINEST, "Kunde vorher: %s", origKunde);
	
		origKunde.setValues(kunde);
		LOGGER.log(FINEST, "Kunde nachher: %s", origKunde);
		
		
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		kunde = kv.updateKunde(origKunde, locale);
		if (kunde == null) {
		
			final String msg = "Kein Kunde gefunden mit der ID " + origKunde.getId();
			throw new NotFoundException(msg);
		}
	}
}
