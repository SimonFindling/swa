package de.shop.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.shop.data.Bestellung;
import de.shop.data.Kunde;

final class Mock {
private static final String LOG_TAG = Mock.class.getSimpleName();
	
	private static String read(int dateinameId) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(ShopApp.open(dateinameId)));
    	final StringBuilder sb = new StringBuilder();
    	try {
    		for (;;) {
				final String line = reader.readLine();
				if (line == null) {
					break;
				}
				sb.append(line);
			}
		}
    	catch (IOException e) {
    		throw new InternalShopError(e.getMessage(), e);
		}
    	finally {
    		if (reader != null) {
    			try {
					reader.close();
				}
    			catch (IOException e) {}
    		}
    	}
    	
    	final String jsonStr = sb.toString();
    	Log.v(LOG_TAG, "jsonStr = " + jsonStr);
		return jsonStr;
	}
	
	static HttpResponse<AbstractKunde> sucheKundeById(Long id) {
    	if (id <= 0 || id >= 1000) {
    		return new HttpResponse<AbstractKunde>(HTTP_NOT_FOUND, "Kein Kunde gefunden mit ID " + id);
    	}
    	
    	int dateinameId;
    	if (id % 3 == 0) {
    		dateinameId = R.raw.mock_firmenkunde;
    	}
    	else {
    		dateinameId = R.raw.mock_privatkunde;
    	}
    	
    	final String jsonStr = read(dateinameId);
    	JsonReader jsonReader = null;
    	JsonObject jsonObject;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(jsonStr));
    		jsonObject = jsonReader.readObject();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
    	final AbstractKunde kunde = jsonObject.getString("type").equals("P")
    	                            ? new Privatkunde()
    	                            : new Firmenkunde();

    	kunde.fromJsonObject(jsonObject);
    	kunde.id = id;
		
    	final HttpResponse<AbstractKunde> result = new HttpResponse<AbstractKunde>(HTTP_OK, jsonObject.toString(), kunde);
    	return result;
	}

	static HttpResponse<AbstractKunde> sucheKundenByNachname(String nachname) {
		if (nachname.startsWith("X")) {
			return new HttpResponse<AbstractKunde>(HTTP_NOT_FOUND, "Keine Kunde gefunden mit Nachname " + nachname);
		}
		
		final ArrayList<AbstractKunde> kunden = new ArrayList<AbstractKunde>();
		final String jsonStr = read(R.raw.mock_kunden);
		JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(jsonStr));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
		
    	final List<JsonObject> jsonObjectList = jsonArray.getValuesAs(JsonObject.class);
   		for (JsonObject jsonObject : jsonObjectList) {
           	final AbstractKunde kunde = jsonObject.getString("type").equals("P")
   					                    ? new Privatkunde()
   			                            : new Firmenkunde();
			kunde.fromJsonObject(jsonObject);
			kunde.nachname = nachname;
   			kunden.add(kunde);
   		}
    	
    	final HttpResponse<AbstractKunde> result = new HttpResponse<AbstractKunde>(HTTP_OK, jsonArray.toString(), kunden);
		return result;
    }

	
	static Kunde sucheKundeById(Long id) {
		return new Kunde(id, "Name" + id);
	}
	
	static ArrayList<Kunde> sucheKundenByName(String name) {
		final int anzahl = name.length() + 3;
		final ArrayList<Kunde> kunden = new ArrayList<Kunde>(anzahl);
		for (int i = 1; i <= anzahl; i++) {
			final Kunde k = new Kunde(Long.valueOf(i), name);
			kunden.add(k);
		}

		return kunden;
    }

	static List<Long> sucheBestellungenIdsByKundeId(Long id) {
		final int anzahl = (int) ((id % 3) + 3);  // 3 - 5 Bestellungen
		final List<Long> ids = new ArrayList<Long>(anzahl);
		
		// Bestellung IDs sind letzte Dezimalstelle, da 3-5 Bestellungen (s.o.)
		// Kunde-ID wird vorangestellt und deshalb mit 10 multipliziert
		for (int i = 0; i < anzahl; i++) {
			ids.add(Long.valueOf(id * 10 + 2 * i + 1));
		}
		return ids;
	}

	static Bestellung sucheBestellungById(Long id) {
		return new Bestellung(id, new Date());
	}
	static List<Long> sucheKundeIdsByPrefix(String kundeIdPrefix) {
		int dateinameId = -1;
    	if ("1".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_1;
    	}
    	else if ("10".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_10;
    	}
    	else if ("11".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_11;
    	}
    	else if ("2".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_2;
    	}
    	else if ("20".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_20;
    	}
    	else {
    		return Collections.emptyList();
    	}
    	
    	final String jsonStr = read(dateinameId);
		JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(jsonStr));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
    	final List<Long> result = new ArrayList<Long>(jsonArray.size());
    	final List<JsonNumber> jsonNumberList = jsonArray.getValuesAs(JsonNumber.class);
	    for (JsonNumber jsonNumber : jsonNumberList) {
	    	final Long id = Long.valueOf(jsonNumber.longValue());
	    	result.add(id);
    	}
    	
    	Log.d(LOG_TAG, "ids= " + result.toString());
    	
    	return result;
    }

    static List<String> sucheNachnamenByPrefix(String nachnamePrefix) {
    	if (TextUtils.isEmpty(nachnamePrefix)) {
    		return Collections.emptyList();
    	}
    	
		int dateinameNachnamen = -1;
		if (nachnamePrefix.startsWith("A")) {
    		dateinameNachnamen = R.raw.mock_nachnamen_a;
    	}
    	else if (nachnamePrefix.startsWith("D")) {
    		dateinameNachnamen = R.raw.mock_nachnamen_d;
    	}
    	else {
    		return Collections.emptyList();
    	}
    	
    	final String jsonStr = read(dateinameNachnamen);
		JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(jsonStr));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
    	final List<JsonString> jsonStringList = jsonArray.getValuesAs(JsonString.class);
    	final List<String> result = new ArrayList<String>(jsonArray.size());
	    for (JsonString jsonString : jsonStringList) {
	    	final String nachname = jsonString.getString();
	    	result.add(nachname);
	    }
		
    	Log.d(LOG_TAG, "nachnamen= " + result.toString());
    	return result;
    }
    
    static HttpResponse<AbstractKunde> createKunde(AbstractKunde kunde) {
    	kunde.id = Long.valueOf(kunde.nachname.length());  // Anzahl der Buchstaben des Nachnamens als emulierte neue ID
    	Log.d(LOG_TAG, "createKunde: " + kunde);
    	Log.d(LOG_TAG, "createKunde: " + kunde.toJsonObject());
    	final HttpResponse<AbstractKunde> result = new HttpResponse<AbstractKunde>(HTTP_CREATED, KUNDEN_PATH + "/1", kunde);
    	return result;
    }

    static HttpResponse<AbstractKunde> updateKunde(AbstractKunde kunde) {
    	Log.d(LOG_TAG, "updateKunde: " + kunde);
    	
    	if (TextUtils.isEmpty(username)) {
    		return new HttpResponse<Kunde>(HTTP_UNAUTHORIZED, null);
    	}
    	
    	if ("x".equals(username)) {
    		return new HttpResponse<Kunde>(HTTP_FORBIDDEN, null);
    	}
    	
    	if ("y".equals(username)) {
    		return new HttpResponse<Kunde>(HTTP_CONFLICT, "Die Email-Adresse existiert bereits");
    	}
    	
    	Log.d(LOG_TAG, "updateKunde: " + kunde.toJsonObject());
    	return new HttpResponse<Kunde>(HTTP_NO_CONTENT, null, kunde);
    }

    static HttpResponse<Void> deleteKunde(Long kundeId) {
    	Log.d(LOG_TAG, "deleteKunde: " + kundeId);
    	return new HttpResponse<Void>(HTTP_NO_CONTENT, null);
    }

    static HttpResponse<Bestellung> sucheBestellungById(Long id) {
		final Bestellung bestellung = new Bestellung(id, new Date());
		
		final JsonObject jsonObject = bestellung.toJsonObject();
		final HttpResponse<Bestellung> result = new HttpResponse<Bestellung>(HTTP_OK, jsonObject.toString(), bestellung);
		Log.d(LOG_TAG, result.resultObject.toString());
		return result;
	}
    
	
	private Mock() {}
}
