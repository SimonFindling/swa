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
	private static final Map<String, Class<? extends Kunde>> CLASS_MAP;
	
	private final KundeServiceBinder binder = new KundeServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	static {
		// 2 Eintraege in die HashMap mit 100% = 1.0 Fuellgrad
		CLASS_MAP = new HashMap<String, Class<? extends AbstractKunde>>(2, 1);
		CLASS_MAP.put("P", Privatkunde.class);
		CLASS_MAP.put("F", Firmenkunde.class);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class KundeServiceBinder extends Binder {
		
		public KundeService getService() {
			return KundeService.this;
		}
		
		private ProgressDialog progressDialog;
		private ProgressDialog showProgressDialog(Context ctx) {
			progressDialog = new ProgressDialog(ctx);
			progressDialog.setProgressStyle(STYLE_SPINNER);  // Kreis (oder horizontale Linie)
			progressDialog.setMessage(getString(R.string.s_bitte_warten));
			progressDialog.setCancelable(true);      // Abbruch durch Zuruecktaste
			progressDialog.setIndeterminate(true);   // Unbekannte Anzahl an Bytes werden vom Web Service geliefert
			progressDialog.show();
			return progressDialog;
		}
		
		/**
		 */
		public HttpResponse<AbstractKunde> sucheKundeById(Long id, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "AbstractKunde"
			final AsyncTask<Long, Void, HttpResponse<AbstractKunde>> sucheKundeByIdTask = new AsyncTask<Long, Void, HttpResponse<AbstractKunde>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<AbstractKunde> doInBackground(Long... ids) {
					final Long id = ids[0];
		    		final String path = KUNDEN_PATH + "/" + id;
		    		Log.v(LOG_TAG, "path = " + path);
		    		final HttpResponse<AbstractKunde> result = mock
		    				                                   ? Mock.sucheKundeById(id)
		    				                                   : WebServiceClient.getJsonSingle(path, TYPE, CLASS_MAP);

					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<AbstractKunde> unused) {
					progressDialog.dismiss();
	    		}
			};
			
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
			
			sucheKundeByIdTask.execute(id);
    		HttpResponse<AbstractKunde> result = null;
	    	try {
	    		result = sucheKundeByIdTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
	    	
    		if (result.responseCode != HTTP_OK) {
	    		return result;
		    }
    		
    		setBestellungenUri(result.resultObject);
		    return result;
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
		private void setBestellungenUri(AbstractKunde kunde) {
	    	// URLs der Bestellungen fuer Emulator anpassen
	    	final String bestellungenUri = kunde.bestellungenUri;
	    	if (!TextUtils.isEmpty(bestellungenUri)) {
			    kunde.bestellungenUri = bestellungenUri.replace(LOCALHOST, LOCALHOST_EMULATOR);
	    	}
		}
		
		/**
		 */
		public HttpResponse<Kunde> sucheKundenByNachname(String nachname, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "String", Resultat vom Typ "List<AbstractKunde>"
			final AsyncTask<String, Void, HttpResponse<Kunde>> sucheKundenByNameTask = new AsyncTask<String, Void, HttpResponse<AbstractKunde>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Kunde> doInBackground(String... nachnamen) {
					final String nachname = nachnamen[0];
					final String path = NACHNAME_PATH + nachname;
					Log.v(LOG_TAG, "path = " + path);
		    		final HttpResponse<Kunde> result = mock
		    				                                   ? Mock.sucheKundenByNachname(nachname)
		    				                                   : WebServiceClient.getJsonList(path, TYPE, CLASS_MAP);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<Kunde> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			sucheKundenByNameTask.execute(nachname);
			HttpResponse<Kunde> result = null;
			try {
				result = sucheKundenByNameTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}

	    	if (result.responseCode != HTTP_OK) {
	    		return result;
	    	}
	    	
	    	final ArrayList<AbstractKunde> kunden = result.resultList;
	    	// URLs fuer Emulator anpassen
	    	for (AbstractKunde k : kunden) {
	    		setBestellungenUri(k);
	    	}
			return result;
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
			public List<Long> sucheIds(String prefix) {
				final String path = KUNDEN_ID_PREFIX_PATH + "/" + prefix;
			    Log.v(LOG_TAG, "sucheIds: path = " + path);

	    		final List<Long> ids = mock
	   				                   ? Mock.sucheKundeIdsByPrefix(prefix)
	   				                   : WebServiceClient.getJsonLongList(path);

				Log.d(LOG_TAG, "sucheIds: " + ids.toString());
				return ids;
			}
			
			/**
			 * Annahme: wird ueber AutoCompleteTextView aufgerufen, wobei die dortige Methode
			 * performFiltering() schon einen neuen Worker-Thread startet, so dass AsyncTask hier
			 * ueberfluessig ist.
			 */
			public List<String> sucheNachnamen(String prefix) {
				final String path = NACHNAME_PREFIX_PATH +  "/" + prefix;
			    Log.v(LOG_TAG, "sucheNachnamen: path = " + path);

	    		final List<String> nachnamen = mock
	    				                       ? Mock.sucheNachnamenByPrefix(prefix)
	    				                       : WebServiceClient.getJsonStringList(path);
				Log.d(LOG_TAG, "sucheNachnamen: " + nachnamen);

				return nachnamen;
			}

			/**
			 */
			public HttpResponse<Kunde> createKunde(Kunde kunde, final Context ctx) {
				// (evtl. mehrere) Parameter vom Typ "AbstractKunde", Resultat vom Typ "void"
				final AsyncTask<Kunde, Void, HttpResponse<Kunde>> createKundeTask = new AsyncTask<Kunde, Void, HttpResponse<Kunde>>() {
					@Override
		    		protected void onPreExecute() {
						progressDialog = showProgressDialog(ctx);
					}
					
					@Override
					// Neuer Thread, damit der UI-Thread nicht blockiert wird
					protected HttpResponse<Kunde> doInBackground(Kunde... kunden) {
						final Kunde kunde = kunden[0];
			    		final String path = KUNDEN_PATH;
			    		Log.v(LOG_TAG, "path = " + path);

			    		final HttpResponse<Kunde> result = mock
	                                                               ? Mock.createKunde(kunde)
	                                                               : WebServiceClient.postJson(kunde, path);
			    		
						Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
						return result;
					}
					
					@Override
		    		protected void onPostExecute(HttpResponse<Kunde> unused) {
						progressDialog.dismiss();
		    		}
				};
				
				createKundeTask.execute(kunde);
				HttpResponse<Kunde> response = null; 
				try {
					response = createKundeTask.get(timeout, SECONDS);
				}
		    	catch (Exception e) {
		    		throw new InternalShopError(e.getMessage(), e);
				}
				
				kunde.id = Long.valueOf(response.content);
				final HttpResponse<Kunde> result = new HttpResponse<AbstractKunde>(response.responseCode, response.content, kunde);
				return result;
		    }
			
			/**
			 */
			public HttpResponse<Kunde> updateKunde(Kunde kunde, final Context ctx) {
				// (evtl. mehrere) Parameter vom Typ "AbstractKunde", Resultat vom Typ "void"
				final AsyncTask<Kunde, Void, HttpResponse<Kunde>> updateKundeTask = new AsyncTask<AbstractKunde, Void, HttpResponse<AbstractKunde>>() {
					@Override
		    		protected void onPreExecute() {
						progressDialog = showProgressDialog(ctx);
					}
					
					@Override
					// Neuer Thread, damit der UI-Thread nicht blockiert wird
					protected HttpResponse<Kunde> doInBackground(Kunde... kunden) {
						final Kunde kunde = kunden[0];
			    		final String path = KUNDEN_PATH;
			    		Log.v(LOG_TAG, "path = " + path);

			    		final HttpResponse<Kunde> result = mock
			    				                          ? Mock.updateKunde(kunde)
			    		                                  : WebServiceClient.putJson(kunde, path);
						Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
						return result;
					}
					
					@Override
		    		protected void onPostExecute(HttpResponse<Kunde> unused) {
						progressDialog.dismiss();
		    		}
				};
				
				updateKundeTask.execute(kunde);
				final HttpResponse<Kunde> result;
				try {
					result = updateKundeTask.get(timeout, SECONDS);
				}
		    	catch (Exception e) {
		    		throw new InternalShopError(e.getMessage(), e);
				}
				
				if (result.responseCode == HTTP_NO_CONTENT || result.responseCode == HTTP_OK) {
					kunde.updateVersion();  // kein konkurrierendes Update auf Serverseite
					result.resultObject = kunde;
				}
				
				return result;
		    }
			
			/**
			 */
			public HttpResponse<Void> deleteKunde(Long id, final Context ctx) {
				
				// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "AbstractKunde"
				final AsyncTask<Long, Void, HttpResponse<Void>> deleteKundeTask = new AsyncTask<Long, Void, HttpResponse<Void>>() {
					@Override
		    		protected void onPreExecute() {
						progressDialog = showProgressDialog(ctx);
					}
					
					@Override
					// Neuer Thread, damit der UI-Thread nicht blockiert wird
					protected HttpResponse<Void> doInBackground(Long... ids) {
						final Long kundeId = ids[0];
			    		final String path = KUNDEN_PATH + "/" + kundeId;
			    		Log.v(LOG_TAG, "path = " + path);

			    		final HttpResponse<Void> result = mock ? Mock.deleteKunde(kundeId) : WebServiceClient.delete(path);
				    	return result;
					}
					
					@Override
		    		protected void onPostExecute(HttpResponse<Void> unused) {
						progressDialog.dismiss();
		    		}
				};
				
				deleteKundeTask.execute(id);
				final HttpResponse<Void> result;
		    	try {
		    		result = deleteKundeTask.get(timeout, SECONDS);
				}
		    	catch (Exception e) {
		    		throw new InternalShopError(e.getMessage(), e);
				}
				
				return result;
	    }
	}
}
