package de.shop.kundenverwaltung.rest;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.util.Log;


@ApplicationScoped
@Log
public class UriHelperKunde {
	public URI getUriKunde(Kunde kunde, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
		                             .path(KundenverwaltungResource.class)
		                             .path(KundenverwaltungResource.class, "findKundeById");
		final URI kundeUri = ub.build(kunde.getId());
		return kundeUri;
	}
	
	
	public void updateUriKunde(Kunde kunde, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
                                     .path(KundenverwaltungResource.class)
                                     .path(KundenverwaltungResource.class, "findBestellungenByKundeId");
		final URI bestellungenUri = ub.build(kunde.getId());
		kunde.setBestellungenUri(bestellungenUri);
	}
}
