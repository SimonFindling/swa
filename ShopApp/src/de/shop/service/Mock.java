package de.shop.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.shop.data.Bestellung;
import de.shop.data.Kunde;

final class Mock {
	
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
	
	private Mock() {}
}
