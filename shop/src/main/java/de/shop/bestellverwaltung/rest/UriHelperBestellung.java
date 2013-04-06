package de.shop.bestellverwaltung.rest;


import java.net.URI;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.artikelverwaltung.rest.UriHelperArtikel;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.rest.UriHelperKunde;
import de.shop.util.Log;


@ApplicationScoped
@Log
public class UriHelperBestellung {
	@Inject
	private UriHelperKunde uriHelperKunde;
	
	@Inject
	private UriHelperArtikel uriHelperArtikel;
	
	public void updateUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
		final Kunde kunde = bestellung.getKunde();
		if (kunde != null) {
			
			final URI kundeUri = uriHelperKunde.getUriKunde(bestellung.getKunde(), uriInfo);
			bestellung.setKundeUri(kundeUri);
		}
		
		final List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		if (bestellpositionen != null && !bestellpositionen.isEmpty()) {
			for (Bestellposition bp : bestellpositionen) {
				final URI artikelUri = uriHelperArtikel.getUriArtikel(bp.getArtikel(), uriInfo);
				bp.setArtikelUri(artikelUri);
			}
		}
		
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
                                     .path(BestellverwaltungResource.class)
                                     .path(BestellverwaltungResource.class, "findLieferungenByBestellungId");
		final URI uri = ub.build(bestellung.getId());
		bestellung.setLieferungenUri(uri);
	}

	public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
		                             .path(BestellverwaltungResource.class)
		                             .path(BestellverwaltungResource.class, "findBestellungById");
		final URI uri = ub.build(bestellung.getId());
		return uri;
	}
}
