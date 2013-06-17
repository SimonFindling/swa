package de.shop.service;

import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.shop.data.Bestellung;
import de.shop.ui.main.Prefs;

public class BestellungService extends Service {
	private static final String LOG_TAG = BestellungService.class.getSimpleName();

	private final BestellungServiceBinder binder = new BestellungServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	private ProgressDialog progressDialog;
	private ProgressDialog showProgressDialog(Context ctx) {
		progressDialog = new ProgressDialog(ctx);  // Objekt this der umschliessenden Klasse Startseite
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  // Kreis (oder horizontale Linie)
		progressDialog.setMessage(getString(R.string.s_bitte_warten));
		progressDialog.setCancelable(true);      // Abbruch durch Zuruecktaste 
		progressDialog.setIndeterminate(true);   // Unbekannte Anzahl an Bytes werden vom Web Service geliefert
		progressDialog.show();
		return progressDialog;
	}
	
	public class BestellungServiceBinder extends Binder {
		
		// Aufruf in einem eigenen Thread
		public Bestellung getBestellungById(Long id) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "Bestellung"
			final AsyncTask<Long, Void, Bestellung> getBestellungByIdTask = new AsyncTask<Long, Void, Bestellung>() {

				@Override
	    		protected void onPreExecute() {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread starten ...");
				}
				
				@Override
				// Neuer Thread (hier: Emulation des REST-Aufrufs), damit der UI-Thread nicht blockiert wird
				protected Bestellung doInBackground(Long... ids) {
					final Long bestellungId = ids[0];
			    	Bestellung bestellung;
			    	if (Prefs.mock) {
			    		bestellung = Mock.sucheBestellungById(bestellungId);
			    	}
			    	else {
			    		Log.e(LOG_TAG, "Suche nach Bestellnummer ist nicht implementiert");
			    		return null;
			    	}
					Log.d(LOG_TAG+ ".AsyncTask", "doInBackground: " + bestellung);
					return bestellung;
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Bestellung> doInBackground(Long... ids) {
					final Long bestellungId = ids[0];
		    		final String path = BESTELLUNGEN_PATH + "/" + bestellungId;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<Bestellung> result = mock
		    				                                ? Mock.sucheBestellungById(bestellungId)
		    				                                : WebServiceClient.getJsonSingle(path, Bestellung.class);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(Bestellung bestellung) {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread beenden ...");
	    		}
			};
			
			getBestellungByIdTask.execute(id);
	    	Bestellung bestellung = null;
	    	try {
	    		bestellung = getBestellungByIdTask.get(3L, TimeUnit.SECONDS);
			}
	    	catch (Exception e) {
	    		Log.e(LOG_TAG, e.getMessage(), e);
			}
	    	
	    	return bestellung;
	    	
	    	getBestellungByIdTask.execute(Long.valueOf(id));
			HttpResponse<Bestellung> result = null;
	    	try {
	    		result = getBestellungByIdTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
	    	
	    	return result;
		}
	}
}
