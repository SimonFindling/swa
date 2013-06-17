package de.shop.service;

import static de.shop.ui.main.Prefs.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.shop.data.Kunde;

public class KundeService extends Service {
	private static final String LOG_TAG = KundeService.class.getSimpleName();
	
	private final KundeServiceBinder binder = new KundeServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class KundeServiceBinder extends Binder {
		public Kunde sucheKundeById(Long id) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "Kunde"
			final AsyncTask<Long, Void, Kunde> sucheKundeByIdTask = new AsyncTask<Long, Void, Kunde>() {
				@Override
	    		protected void onPreExecute() {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread starten ...");
				}
				
				@Override
				// Neuer Thread (hier: Emulation des REST-Aufrufs), damit der UI-Thread nicht blockiert wird
				protected Kunde doInBackground(Long... ids) {
					final Long kundeId = ids[0];
			    	Kunde kunde;
			    	if (mock) {
			    		kunde = Mock.sucheKundeById(kundeId);
			    	}
			    	else {
			    		Log.e(LOG_TAG, "Suche nach Kundennummer ist nicht implementiert");
			    		return null;
			    	}
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + kunde);
					return kunde;
				}
				
				@Override
	    		protected void onPostExecute(Kunde kunde) {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread beenden ...");
	    		}
			};

			sucheKundeByIdTask.execute(id);
	    	Kunde kunde = null;
	    	try {
	    		kunde = sucheKundeByIdTask.get(3L, TimeUnit.SECONDS);
			}
	    	catch (Exception e) {
	    		Log.e(LOG_TAG, e.getMessage(), e);
			}
			return kunde;
		}
		
		
		
		public ArrayList<Kunde> sucheKundenByName(String name) {
			// (evtl. mehrere) Parameter vom Typ "String", Resultat vom Typ "ArrayList<Kunde>"
			final AsyncTask<String, Void, ArrayList<Kunde>> sucheKundenByNameTask = new AsyncTask<String, Void, ArrayList<Kunde>>() {
				@Override
	    		protected void onPreExecute() {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread starten ...");
				}
				
				@Override
				// Neuer Thread (hier: Emulation des REST-Aufrufs), damit der UI-Thread nicht blockiert wird
				protected ArrayList<Kunde> doInBackground(String... namen) {
					final String name = namen[0];
					ArrayList<Kunde> kunden;
			    	if (mock) {
			    		kunden = Mock.sucheKundenByName(name);
			    	}
			    	else {
			    		Log.e(LOG_TAG, "Suche nach Kundenname ist nicht implementiert");
			    		return null;
			    	}
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + kunden);
					return kunden;
				}
				
				@Override
	    		protected void onPostExecute(ArrayList<Kunde> kunden) {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread beenden ...");
	    		}
			};
			
			sucheKundenByNameTask.execute(name);
			ArrayList<Kunde> kunden = null;
			try {
				kunden = sucheKundenByNameTask.get(3L, TimeUnit.SECONDS);
			}
	    	catch (Exception e) {
	    		Log.e(LOG_TAG, e.getMessage(), e);
			}

			return kunden;
	    }	
	

		public List<Long> sucheBestellungenIdsByKundeId(Long id) {
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "List<Long>"
			final AsyncTask<Long, Void, List<Long>> sucheBestellungenIdsByKundeIdTask = new AsyncTask<Long, Void, List<Long>>() {
				@Override
	    		protected void onPreExecute() {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread starten ...");
				}
				
				@Override
				// Neuer Thread (hier: Emulation des REST-Aufrufs), damit der UI-Thread nicht blockiert wird
				protected List<Long> doInBackground(Long... ids) {
					final Long kundeId = ids[0];
			    	List<Long> bestellungIds;
			    	if (mock) {
			    		bestellungIds = Mock.sucheBestellungenIdsByKundeId(kundeId);
			    	}
			    	else {
			    		Log.e(LOG_TAG, "Suche nach Kundenname ist nicht implementiert");
			    		return null;
			    	}
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + bestellungIds);
					return bestellungIds;
				}
				
				@Override
	    		protected void onPostExecute(List<Long> ids) {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread beenden ...");
	    		}
			};
			
			sucheBestellungenIdsByKundeIdTask.execute(id);
			List<Long> bestellungIds = null;
			try {
				bestellungIds = sucheBestellungenIdsByKundeIdTask.get(3L, TimeUnit.SECONDS);
			}
	    	catch (Exception e) {
	    		Log.e(LOG_TAG, e.getMessage(), e);
			}
	
			return bestellungIds;
	    }
		
		public void updateKunde(Kunde kunde, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "Kunde", Resultat vom Typ "void"
			final AsyncTask<Kunde, Void, Void> updateKundeTask = new AsyncTask<Kunde, Void, Void>() {
				@Override
	    		protected void onPreExecute() {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread starten ...");
				}
				
				@Override
				// Neuer Thread (hier: Emulation des REST-Aufrufs), damit der UI-Thread nicht blockiert wird
				protected Void doInBackground(Kunde... kunden) {
					final Kunde kunde = kunden[0];
			    	if (mock) {
						Log.d(LOG_TAG, "mock fuer updateKunde: " + kunde);
			    	}
			    	else {
			    		Log.e(LOG_TAG, "Update von Kunden ist nicht implementiert");
			    		return null;
			    	}
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + kunde);
					return null;
				}
				
				@Override
	    		protected void onPostExecute(Void tmp) {
					Log.d(LOG_TAG, "... ProgressDialog im laufenden Thread beenden ...");
	    		}
			};
			
			updateKundeTask.execute(kunde);
			try {
				updateKundeTask.get(3L, TimeUnit.SECONDS);
			}
	    	catch (Exception e) {
	    		Log.e(LOG_TAG, e.getMessage(), e);
			}
	    }
	}
}
