package de.shop.artikelverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.ARTIKEL_ID_PATH_PARAM;
import static de.shop.util.TestConstants.ARTIKEL_ID_PATH;
import static de.shop.util.TestConstants.ARTIKEL_PATH;
import static de.shop.util.TestConstants.LOCATION;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class ArtikelResourceTest extends AbstractResourceTest {
	
	private static final Logger LOGGER = 
			Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long ARTIKEL_ID_NICHT_VORHANDEN = Long.valueOf(11111);
	private static final Long ARTIKEL_ID_VORHANDEN = Long.valueOf(300);
	
	private static final String NEUE_BEZEICHNUNG = "True Religion Jeans";
	private static final double NEUER_PREIS = 500;
	private static final double INVALID_PREIS = -5;
	private static final boolean NEUE_VERFUEGBARKEIT = true;
	
	@Test
	public void findArtikelByIdNichtVorhanden() {
		LOGGER.finer("BEGINN");
		final Long artikelId = ARTIKEL_ID_NICHT_VORHANDEN;
		
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				                         .pathParameter(ARTIKEL_ID_PATH_PARAM, artikelId)
                                         .get(ARTIKEL_ID_PATH);
		assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
		LOGGER.finer("ENDE");
	}
	
	@Test
	public void findArtikelByIdVorhanden() {
		LOGGER.finer("BEGINN");
		final Long artikelId = ARTIKEL_ID_VORHANDEN;
		
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				                         .pathParameter(ARTIKEL_ID_PATH_PARAM, artikelId)
				                         .get(ARTIKEL_ID_PATH);
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		try (final JsonReader jsonReader =
		    getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(),
					   is(artikelId.longValue()));
		}

		LOGGER.finer("ENDE");
	}
	
	@Test
	public void createArtikel() {
		LOGGER.finer("BEGINN");
		
		final String bezeichnung = NEUE_BEZEICHNUNG;
		final double preis = NEUER_PREIS;
		final boolean verfuegbar = NEUE_VERFUEGBARKEIT;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
		             		          .add("bezeichnung", bezeichnung)
		             		          .add("preis", preis)
		             		          .add("verfuegbar", verfuegbar)
		                              .build();

		final Response response = given().contentType(APPLICATION_JSON)
				                         .body(jsonObject.toString())
                                         .auth()
                                         .basic(username, password)
                                         .post(ARTIKEL_PATH);
		
		assertThat(response.getStatusCode(), is(HTTP_CREATED));
		final String location = response.getHeader(LOCATION);
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id.longValue() > 0, is(true));

		LOGGER.finer("ENDE");
	}
	
	@Test
	public void createArtikelInvalid() {
		LOGGER.finer("BEGINN");
		
		final String bezeichnung = NEUE_BEZEICHNUNG;
		final double preis = INVALID_PREIS;
		final boolean verfuegbar = NEUE_VERFUEGBARKEIT;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
		             		          .add("bezeichnung", bezeichnung)
		             		          .add("preis", preis)
		             		          .add("verfuegbar", verfuegbar)
		                              .build();

		final Response response = given().contentType(APPLICATION_JSON)
				                         .body(jsonObject.toString())
                                         .auth()
                                         .basic(username, password)
                                         .post(ARTIKEL_PATH);
		
		assertThat(response.getStatusCode(), is(HTTP_INTERNAL_ERROR));
		assertThat(response.asString().isEmpty(), is(false));

		LOGGER.finer("ENDE");
	}
	
	@Test
	public void updateArtikel() {
		LOGGER.finer("BEGINN");
		
		final Long artikelId = ARTIKEL_ID_VORHANDEN;
		final String neueBezeichnung = NEUE_BEZEICHNUNG;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				                   .pathParameter(ARTIKEL_ID_PATH_PARAM, artikelId)
                                   .get(ARTIKEL_ID_PATH);
		
		JsonObject jsonObject;
		try (final JsonReader jsonReader =
	    getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}
    	assertThat(jsonObject.getJsonNumber("id").longValue(), 
    			is(artikelId.longValue()));
    	
    	// Aus den gelesenen JSON-Werten neues JSON-Objekt mit neuer Bezeichnung bauen
    	final JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
    	final Set<String> keys = jsonObject.keySet();
    	for (String key : keys) {
    		if ("bezeichnung".equals(key)) {
    			job.add("bezeichnung", neueBezeichnung);
    		}
    		else {
    			job.add(key, jsonObject.get(key));
    		}
    	}
    	jsonObject = job.build();
    	
		response = given().contentType(APPLICATION_JSON)
				          .body(jsonObject.toString())
                          .auth()
                          .basic(username, password)
                          .put(ARTIKEL_PATH);
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
		
		// Kontrolle, ob die Artikelbezeichnung wirklich veraendert wurde
		response = given().header(ACCEPT, APPLICATION_JSON)
										 .pathParameter(ARTIKEL_ID_PATH_PARAM, artikelId)
										 .get(ARTIKEL_ID_PATH);
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		try (final JsonReader jsonReader =
	        getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
		    jsonObject = jsonReader.readObject();
				  }
		assertThat(jsonObject.getJsonString("bezeichnung").toString(), 
				is(neueBezeichnung));
		
		LOGGER.finer("ENDE");
   	}
}
