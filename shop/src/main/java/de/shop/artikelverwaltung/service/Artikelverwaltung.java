package de.shop.artikelverwaltung.service;

import static de.shop.util.AbstractDao.QueryParameter.with;
import static de.shop.util.Constants.KEINE_ID;
import static java.util.logging.Level.FINER;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;


import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import com.google.common.base.Strings;

import de.shop.artikelverwaltung.dao.ArtikelDao;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.Log;




@Log
public class Artikelverwaltung implements Serializable {
	private static final long serialVersionUID = 49275120333563509L;


	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	
	@Inject
	private ArtikelDao dao;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
	}
	
	
	public Artikel createArtikel(Artikel artikel) {
		if (artikel == null) {
			return artikel;
		}
		
		artikel.setId(KEINE_ID);
		artikel = dao.create(artikel);
		
		return artikel;
	}
	
	public Artikel updateArtikel(Artikel artikel) {
		if (artikel == null) {
			return artikel;
		}	
		
		final Artikel vorhandenerArtikel = findArtikelById(artikel.getId());
		if (vorhandenerArtikel.getId().longValue() != artikel.getId().longValue()) {
			throw new ArtikelDoesntExistException(artikel.getId());
		}
		
		
		artikel = dao.update(artikel);
		return artikel;
	}
	
	public List<Artikel> findVerfuegbareArtikel() {
		final List<Artikel> artikelListe = dao.find(Artikel.FIND_VERFUEGBARE_ARTIKEL);
		return artikelListe;
	}
	
	
	public Artikel findArtikelById(Long id) {
		final Artikel artikel = dao.find(id);
		return artikel;
	}
	
	
	public List<Artikel> findArtikelByIds(List<Long> ids) {
		final List<Artikel> artikel = dao.findArtikelByIds(ids);
		return artikel;
	}
	
	public List<Artikel> findArtikelByBezeichnung(String bezeichnung) {
		if (Strings.isNullOrEmpty(bezeichnung)) {
			final List<Artikel> artikelListe = findVerfuegbareArtikel();
			return artikelListe;
		}
		final List<Artikel> artikelListe = dao.find(Artikel.FIND_ARTIKEL_BY_BEZ,
                with(Artikel.PARAM_BEZEICHNUNG,
                	 "%" + bezeichnung + "%").build());

			return artikelListe;
}
	public List<Artikel> findArtikelByMaxPreis(double preis) {
		final List<Artikel> artikelListe = dao.find(Artikel.FIND_ARTIKEL_MAX_PREIS,
				                                    with(Artikel.PARAM_PREIS, preis).build());
		return artikelListe;
	}
}