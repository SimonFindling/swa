package de.shop.ui.main;

import static de.shop.util.Constants.KUNDE_KEY;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import de.shop.R;
import de.shop.data.Kunde;
import de.shop.ui.kunde.KundeDetails;

public class Main extends Activity implements OnClickListener {
	private static final String LOG_TAG = Main.class.getSimpleName();
	
	private KundeServiceBinder kundeServiceBinder;
	private BestellungServiceBinder bestellungServiceBinder;
	
	// ServiceConnection ist ein Interface: anonyme Klasse verwenden, um ein Objekt davon zu erzeugen
		private ServiceConnection kundeServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
				kundeServiceBinder = (KundeServiceBinder) serviceBinder;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				kundeServiceBinder = null;
			}
		};
		
		private ServiceConnection bestellungServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
				bestellungServiceBinder = (BestellungServiceBinder) serviceBinder;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				bestellungServiceBinder = null;
			}
		};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
 // Gibt es Suchergebnisse durch SearchView in der ActionBar, z.B. Kunde ?
        
        Fragment detailsFragment = null;
        final Bundle extras = getIntent().getExtras();
        if (extras == null) {
        	// Keine Suchergebnisse o.ae. vorhanden
        	
        	detailsFragment = new Startseite();
        	
          // Preferences laden
          Prefs.init(this);
        }
        else {
	        final Kunde kunde = (Kunde) extras.get(KUNDE_KEY);
	        if (kunde != null) {
	        	Log.d(LOG_TAG, kunde.toString());
	        	
	    		final Bundle args = new Bundle(1);
	    		args.putSerializable(KUNDE_KEY, kunde);
	    		
	        	detailsFragment = new KundeDetails();
	        	detailsFragment.setArguments(args);
	        }
        }
		
		findViewById(R.id.btn_suchen).setOnClickListener(this);
		
		getFragmentManager().beginTransaction()
        .add(R.id.details, new Startseite())
        .commit();
		
		final Fragment navFragment = getFragmentManager().findFragmentById(R.id.nav);
        final Class<? extends Activity> mainActivity = navFragment == null || !navFragment.isInLayout()
        		                                       ? MainSmartphone.class
        		                                       : MainTablet.class;
        
		final Intent intent = new Intent(this, mainActivity);
		startActivity(intent);
    }
    
    @Override
	public void onStart() {
		super.onStart();

		Intent intent = new Intent(this, KundeService.class);
		bindService(intent, kundeServiceConnection, Context.BIND_AUTO_CREATE);
		
		intent = new Intent(this, BestellungService.class);
		bindService(intent, bestellungServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
	@Override
	public void onStop() {
		super.onStop();
		
		unbindService(kundeServiceConnection);
		unbindService(bestellungServiceConnection);
	}

	public KundeServiceBinder getKundeServiceBinder() {
		return kundeServiceBinder;
	}

	public BestellungServiceBinder getBestellungServiceBinder() {
		return bestellungServiceBinder;
	}
    
	@Override // OnClickListener
	public void onClick(View view) {
		final EditText kundeIdTxt = (EditText) findViewById(R.id.kunde_id);
		final String kundeId = kundeIdTxt.getText().toString();
		
		final Kunde kunde = getKunde(kundeId);
		
		// NICHT: new KundeDetails() !!!
		
		final Intent intent = new Intent(view.getContext(), KundeDetails.class);
		intent.putExtra(KUNDE_KEY, kunde);
		startActivity(intent);
	}
    
    private Kunde getKunde(String kundeIdStr) {
    	final Long kundeId = Long.valueOf(kundeIdStr);
    	final Kunde kunde = new Kunde(kundeId, "Name" + kundeIdStr);
    	Log.v(LOG_TAG, kunde.toString());
    	
    	return kunde;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// inflate = fuellen
        getMenuInflater().inflate(R.menu.main, menu);
    	return true;
    }
}
