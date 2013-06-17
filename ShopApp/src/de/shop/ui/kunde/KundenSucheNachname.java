package de.shop.ui.kunde;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.shop.R;

public class KundenSucheNachname extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.kunden_suche_nachname, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		nameTxt = (EditText) view.findViewById(R.id.name_edt);
    	
		// KundenSucheNachname (this) ist gleichzeitig der Listener, wenn der Suchen-Button angeklickt wird
		// und implementiert deshalb die Methode onClick() unten
		view.findViewById(R.id.btn_suchen).setOnClickListener(this);
		
	    // Evtl. vorhandene Tabs der ACTIVITY loeschen
    	final ActionBar actionBar = getActivity().getActionBar();
    	actionBar.setDisplayShowTitleEnabled(true);
    	actionBar.removeAllTabs();
    }
	
	@Override // OnClickListener
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_suchen:
				final String name = nameTxt.getText().toString();
				final Main mainActivity = (Main) getActivity();
				final ArrayList<Kunde> kunden = mainActivity.getKundeServiceBinder().sucheKundenByName(name);
		
				final Intent intent = new Intent(getActivity(), KundenListe.class);
				intent.putExtra(KUNDEN_KEY, kunden);
				startActivity(intent);
				break;
		
			default:
				break;
		}
    }
    
	@Override
	// Nur aufgerufen, falls setHasOptionsMenu(true) in onCreateView() aufgerufen wird
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
}
