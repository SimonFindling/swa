package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDE_KEY;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import de.shop.R;
import de.shop.data.Kunde;
import de.shop.ui.main.Main;
import de.shop.ui.main.Prefs;

public class KundeEdit extends Fragment {
	private static final String LOG_TAG = KundeEdit.class.getSimpleName();
	
	private Kunde kunde;
	private EditText txtName;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		kunde = (Kunde) getArguments().get(KUNDE_KEY);
		Log.d(LOG_TAG, kunde.toString());
        
		// Voraussetzung fuer onOptionsItemSelected()
		setHasOptionsMenu(true);
		
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.kunde_edit, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
    	final TextView txtId = (TextView) view.findViewById(R.id.kunde_id);
    	txtId.setText(String.valueOf(kunde.id));
    	
    	txtName = (EditText) view.findViewById(R.id.name_edt);
    	txtName.setText(kunde.name);
    }
    
	@Override
	// Nur aufgerufen, falls setHasOptionsMenu(true) in onCreateView() aufgerufen wird
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.kunde_edit_options, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.speichern:
				setKunde();
				final Main mainActivity = (Main) getActivity();
				mainActivity.getKundeServiceBinder().updateKunde(kunde, mainActivity);
				
				// Gibt es in der Navigationsleiste eine KundenListe? Wenn ja: Refresh mit geaendertem Kunde-Objekt
				final Fragment fragment = getFragmentManager().findFragmentById(R.id.kunden_liste_nav);
				if (fragment != null) {
					final KundenListeNav kundenListeFragment = (KundenListeNav) fragment;
					kundenListeFragment.refresh(kunde);
				}
				
				final Bundle args = new Bundle(1);
				args.putSerializable(KUNDE_KEY, kunde);
				
				final Fragment neuesFragment = new KundeDetails();
				neuesFragment.setArguments(args);
				
				// Kein Name (null) fuer die Transaktion, da die Klasse BackStageEntry nicht verwendet wird
				getFragmentManager().beginTransaction()
				                    .replace(R.id.details, neuesFragment)
				                    .addToBackStack(null)  
				                    .commit();
				return true;
				
			case R.id.einstellungen:
				getFragmentManager().beginTransaction()
                                    .replace(R.id.details, new Prefs())
                                    .addToBackStack(null)
                                    .commit();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setKunde() {
		kunde.name = txtName.getText().toString();
		Log.d(LOG_TAG, kunde.toString());
	}
}
