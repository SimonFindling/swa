package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDE_KEY;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import de.shop.R;
import de.shop.data.Kunde;

public class KundeDetails extends Activity {
	private static final String LOG_TAG = KundeDetails.class.getSimpleName();

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kunde_details);
        
        final Bundle extras = getIntent().getExtras();
        if (extras == null) {
        	return;
        }

        final Kunde kunde = (Kunde) extras.getSerializable(KUNDE_KEY);
        Log.d(LOG_TAG, kunde.toString());
        fillValues(kunde);
        
//      Entfaellt seit Android 4.1 bzw. API 16 durch <activity android:parentActivityName="..."> in AndroidManifest.xml
//		final ActionBar actionBar = getActionBar();
//		actionBar.setHomeButtonEnabled(true);
//		actionBar.setDisplayHomeAsUpEnabled(true);
    }
    
    private void fillValues(Kunde kunde) {
        final TextView txtId = (TextView) findViewById(R.id.kunde_id);
    	txtId.setText(kunde.id.toString());
    	
    	final TextView txtName = (TextView) findViewById(R.id.name);
    	txtName.setText(kunde.name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
public class KundeDetails extends Fragment {
private static final String LOG_TAG = KundeDetails.class.getSimpleName();
	
	private Kunde kunde;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        kunde = (Kunde) getArguments().get(KUNDE_KEY);
        Log.d(LOG_TAG, kunde.toString());
        
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.details_tabs, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		final Activity activity = getActivity();
		final ActionBar actionBar = activity.getActionBar();
		// (horizontale) Tabs; NAVIGATION_MODE_LIST fuer Dropdown Liste
		actionBar.setNavigationMode(NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);  // Titel der App ausblenden, um mehr Platz fuer die Tabs zu haben

	    final Bundle args = new Bundle(1);
    	args.putSerializable(KUNDE_KEY, kunde);
    	
	    Tab tab = actionBar.newTab()
	                       .setText(getString(R.string.k_stammdaten))
	                       .setTabListener(new TabListener<KundeStammdaten>(activity,
	                    		                                            KundeStammdaten.class,
	                    		                                            args));
	    actionBar.addTab(tab);
	    
	    tab = actionBar.newTab()
                       .setText(getString(R.string.k_bestellungen))
                       .setTabListener(new TabListener<KundeBestellungen>(activity,
                    		                                              KundeBestellungen.class,
                    		                                              args));
	    actionBar.addTab(tab);
	}
}
