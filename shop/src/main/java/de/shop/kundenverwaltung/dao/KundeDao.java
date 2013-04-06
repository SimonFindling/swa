package de.shop.kundenverwaltung.dao;

import static de.shop.util.AbstractDao.QueryParameter.with;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Bestellung_;
import de.shop.kundenverwaltung.domain.Kunde;
import de.shop.kundenverwaltung.domain.Kunde_;
import de.shop.util.AbstractDao;
import de.shop.util.Log;

@Log
public class KundeDao extends AbstractDao<Kunde, Long> {
	private static final long serialVersionUID = 5988198558901424402L;

	public enum FetchType {
		NUR_KUNDE,
		MIT_BESTELLUNGEN,
		MIT_BESTELLUNGEN_UND_LIEFERUNGEN
	}
	
	public enum OrderType {
		KEINE,
		ID
	}
	
	public List<Kunde> findAllKunden(FetchType fetch, OrderType order) {
		List<Kunde> kunden = null;
		
		switch(fetch) {
			case NUR_KUNDE:
				kunden = OrderType.ID.equals(order)
						 ? find(Kunde.FIND_KUNDEN_ORDER_BY_ID)
						 : find(Kunde.FIND_KUNDEN);
				break;
				
			case MIT_BESTELLUNGEN:
				kunden = find(Kunde.FIND_KUNDEN_FETCH_BESTELLUNGEN);
				break;
			
			default:
				kunden = OrderType.ID.equals(order)
						 ? find(Kunde.FIND_KUNDEN_ORDER_BY_ID)
						 : find(Kunde.FIND_KUNDEN);
				break;
		}
		
		return kunden;
	}
	
	public List<Kunde> findKundenByNachname(String nachname, FetchType fetch) {
		List<Kunde> kunden = null;
		
		switch (fetch) {
			case NUR_KUNDE:
				kunden = find(Kunde.FIND_KUNDEN_BY_NACHNAME,
						 	with(Kunde.PARAM_KUNDE_NACHNAME, nachname).build());
				break;
				
			case MIT_BESTELLUNGEN:
				kunden = find(Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
							with(Kunde.PARAM_KUNDE_NACHNAME, nachname).build());
				break;
				
			case MIT_BESTELLUNGEN_UND_LIEFERUNGEN:
				kunden = find(Kunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
							with(Kunde.PARAM_KUNDE_NACHNAME, nachname).build());
				fetchLieferungen(kunden);
				break;
				
			default:
				kunden = find(Kunde.FIND_KUNDEN_BY_NACHNAME,
					 	with(Kunde.PARAM_KUNDE_NACHNAME, nachname).build());
				break;
		}
		
		return kunden;
	}
	
	public void fetchLieferungen(List<Kunde> kunden) {
		final EntityManager em = getEntityManager();
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Bestellung> criteriaQuery = builder.createQuery(Bestellung.class);
		final Root<Bestellung> b = criteriaQuery.from(Bestellung.class);
		b.fetch(Bestellung_.lieferungen, JoinType.LEFT);
		
		final Path<Long> idPath = b.get(Bestellung_.id);
		
		final List<Predicate> predList = new ArrayList<>();
		for (Kunde k : kunden) {
			final List<Bestellung> bestellungen = k.getBestellungen();
			for (Bestellung best : bestellungen) {
				final Predicate equal = builder.equal(idPath, best.getId());
				predList.add(equal);
			}
		}
		
		if (!predList.isEmpty()) {
			final Predicate[] predArray = new Predicate[predList.size()];
			final Predicate pred = builder.or(predList.toArray(predArray));
			criteriaQuery.where(pred).distinct(true);
			
			final TypedQuery<Bestellung> queryObj = em.createQuery(criteriaQuery);
			queryObj.getResultList();
		}
	}
	
	public Kunde findKundeById(Long id, FetchType fetch) {
		Kunde kunde = null;
		
		switch (fetch) {
			case NUR_KUNDE: 
				kunde = find(id);
				break;
				
			case MIT_BESTELLUNGEN:
				kunde = findSingle(Kunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN,
							with(Kunde.PARAM_KUNDE_ID, id).build());
				break;
				
			default:
				kunde = find(id);
				break;
		}
		
		return kunde;
	}
	
	public List<Kunde> findKundenByNachname(String nachname) {
		final EntityManager em = getEntityManager();
		
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Kunde> criteriaQuery = builder.createQuery(Kunde.class);
		final Root<Kunde> k = criteriaQuery.from(Kunde.class);

		final Path<String> nachnamePath = k.get(Kunde_.nachname);
		
		
		final Predicate pred = builder.equal(nachnamePath, nachname);
		criteriaQuery.where(pred);

		final TypedQuery<Kunde> query = em.createQuery(criteriaQuery);
		final List<Kunde> kunden = query.getResultList();
		return kunden;
	}
	
}