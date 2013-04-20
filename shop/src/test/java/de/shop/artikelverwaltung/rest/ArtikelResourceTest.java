package de.shop.artikelverwaltung.rest;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static de.shop.util.TestConstants.ARTIKEL_ID_PATH_PARAM;
import static de.shop.util.TestConstants.ARTIKEL_ID_PATH;


import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import com.jayway.restassured.response.Response;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class ArtikelResourceTest {
	
	private static final Long ARTIKEL_ID_NICHT_VORHANDEN = Long.valueOf(310);
	
	@Test
	public void findArtikelByIdNichtVorhanden() {
		
		final Long artikelId = ARTIKEL_ID_NICHT_VORHANDEN;
		
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				                         .pathParameter(ARTIKEL_ID_PATH_PARAM, artikelId)
                                         .get(ARTIKEL_ID_PATH);
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
	}
}