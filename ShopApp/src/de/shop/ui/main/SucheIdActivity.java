package de.shop.ui.main;

import static android.app.SearchManager.QUERY;
import static android.content.Intent.ACTION_SEARCH;
import static de.shop.util.Constants.KUNDE_KEY;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import de.shop.data.Kunde;
import de.shop.service.KundeService;
import de.shop.service.KundeService.KundeServiceBinder;

public class SucheIdActivity extends Activity {
	private static final String LOG_TAG = SucheIdActivity.class.getSimpleName();
	
	private String query;
	private KundeServiceBinder kundeServiceBinder;
	
	// ServiceConnection ist ein Interface: anonyme Klasse verwenden, um ein Objekt davon zu erzeugen
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			kundeServiceBinder = (KundeServiceBinder) serviceBinder;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			kundeServiceBinder = null;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.search);

		Intent intent = getIntent();
		if (!ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
	
		query = intent.getStringExtra(QUERY);
		Log.d(LOG_TAG, query);
	}
	
	@Override
	public void onStart() {
		final Intent intent = new Intent(this, KundeService.class);
		bindService(intent, serviceConnection, BIND_AUTO_CREATE);
		// TODO kundeServiceBinder wird nicht gesetzt und bleibt null: WARUM ???
		
		suchen();

		super.onStart();
	}

	@Override
	public void onStop() {
		unbindService(serviceConnection);
		super.onStop();
	}
	
	private void suchen() {
		final Long kundeId = Long.valueOf(query);
		final Kunde kunde = kundeServiceBinder.sucheKundeById(kundeId);
		Log.d(LOG_TAG, kunde.toString());		
		
		final Intent intent = new Intent(this, Main.class);
		if (kunde != null) {
			intent.putExtra(KUNDE_KEY, kunde);
		}
		startActivity(intent);
	}
}
