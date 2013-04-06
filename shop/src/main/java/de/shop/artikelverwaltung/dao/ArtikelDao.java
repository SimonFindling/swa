package de.shop.artikelverwaltung.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.AbstractDao;
import de.shop.util.Log;







@Log
public class ArtikelDao extends AbstractDao<Artikel, Long> {
	private static final long serialVersionUID = 8727455738240071072L;

	public List<Artikel> findArtikelByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
			final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
			final CriteriaQuery<Artikel> criteriaQuery = builder.createQuery(Artikel.class);
			final Root<Artikel> a = criteriaQuery.from(Artikel.class);
	
			final Path<Long> idPath = a.get("id");
	
			Predicate pred = null;
			if (ids.size() == 1) {
				pred = builder.equal(idPath, ids.get(0));
			}
			else {
				final Predicate[] equals = new Predicate[ids.size()];
				int i = 0;
				for (Long id : ids) {
					equals[i++] = builder.equal(idPath, id);
				}
				
				pred = builder.or(equals);
			}
			
			criteriaQuery.where(pred);
			
			final TypedQuery<Artikel> query = getEntityManager().createQuery(criteriaQuery);

			final List<Artikel> artikel = query.getResultList();
			return artikel;
	

	
  }
}