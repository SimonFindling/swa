package de.shop.ui.bestellung;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.shop.R;

public class Bestellungen extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.bestellungen, container, false);
	}
}
