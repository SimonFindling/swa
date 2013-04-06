package de.shop.util;

import static java.util.logging.Level.FINER;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;


/**
 * Generische Dao-Klasse mit den Typ-Parametern E = Entity-Klasse und I = Id-Klasse.
 * Prinzip: wenn nichts gefunden wird, dann wird entweder eine leere Liste oder
 * null (bei singulaeren Queries) zurueck geliefert
 */
public abstract class AbstractDao<E extends Serializable, I extends Serializable> implements Serializable {
	private static final long serialVersionUID = -653605487414682146L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private final Class<E> entityClass;
	
	@PersistenceContext
	private transient EntityManager em;
	
	@SuppressWarnings("unchecked")
	public AbstractDao() {
		// Beispiel: KundeDao<Kunde, Long> extends AbstractDao
		//   getClass() liefert KundeDao$Proxy$_$$_WeldSubclass
		//   getClass().getGenericSuperclass() liefert KundeDao
		//   getClass().getGenericSuperclass().getGenericSuperclass() liefert AbstractDao<Kunde, Long>
		//   getClass().getGenericSuperclass().getGenericSuperclass().getActualTypeArguments()[0] liefert Kunde
		
		// Vererbungshierarchie nach oben bis zum parametrisierten Typen "AbstractDao<E,I>"
		ParameterizedType abstractDaoClass = null;

		Type supertype = getClass().getGenericSuperclass();
		for (;;) {
			if (supertype instanceof ParameterizedType) {
				// Parametrisierter Typ, z.B. MyClass<P> oder AbstractDao<E,I>
				final ParameterizedType parameterizedType = (ParameterizedType) supertype;
				// Ist der eigentliche "raw" Typ = AbstractDao ?
				final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
				if (AbstractDao.class.equals(rawType)) {
					abstractDaoClass = parameterizedType;
					break;
				}
				supertype = rawType.getGenericSuperclass();
			}
			else {
				supertype =  ((Class<?>) supertype).getGenericSuperclass();
			}
		}
		
		entityClass = (Class<E>) abstractDaoClass.getActualTypeArguments()[0];
	}
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.log(FINER, "CDI-faehiges Bean {0} wird geloescht", this);
	}
	
	protected EntityManager getEntityManager() {
		return em;
	}
	
	/**
	 */
	public E find(I id) {
		final E result = em.find(entityClass, id);
		return result;
	}
	
	/**
	 */
	public List<E> find(String namedQuery) {
		return find(entityClass, namedQuery);
	}
	
	/**
	 */
	public List<E> find(String namedQuery, Map<String, Object> parameters) {
		return find(entityClass, namedQuery, parameters);
	}
	
	/**
	 */
	public List<E> find(String namedQuery, int resultLimit) {
		return find(entityClass, namedQuery, resultLimit);
	}
	
	/**
	 */
	public List<E> find(String namedQuery, Map<String, Object> parameters, int resultLimit) {
		return find(entityClass, namedQuery, parameters, resultLimit);
	}

	/**
	 */
	public E findSingle(String namedQuery) {
		return findSingle(entityClass, namedQuery);
	}
	
	/**
	 */
	public E findSingle(String namedQuery, Map<String, Object> parameters) {
		return findSingle(entityClass, namedQuery, parameters);
	}
	
	/**
	 */
	public <T> List<T> find(Class<T> clazz, String namedQuery, Map<String, Object> parameters) {
		final TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);

		// Map in Set konvertieren fuer for-Schleife
		final Set<Entry<String, Object>> paramSet = parameters.entrySet();
		for (Entry<String, Object> entry : paramSet) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		final List<T> result = query.getResultList();
		return result;
	}
	
	/**
	 */
	public <T> List<T> find(Class<T> clazz, String namedQuery) {
		final List<T> result = em.createNamedQuery(namedQuery, clazz)
		                         .getResultList();
		return result;
	}
	
	/**
	 */
	public <T> List<T> find(Class<T> clazz, String namedQuery, int resultLimit) {
		final List<T> result = em.createNamedQuery(namedQuery, clazz)
		                         .setMaxResults(resultLimit)
	                             .getResultList();
		return result;
	}
	
	/**
	 */
	public <T> List<T> find(Class<T> clazz, String namedQuery, Map<String, Object> parameters, int resultLimit) {
		final TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
		query.setMaxResults(resultLimit);

		// Map in Set konvertieren fuer for-Schleife
		final Set<Entry<String, Object>> paramSet = parameters.entrySet();
		for (Entry<String, Object> entry : paramSet) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		final List<T> result = query.getResultList();
		return result;
	}

	
	/**
	 */
	public <T> T findSingle(Class<T> clazz, String namedQuery) {
		try {
			return em.createNamedQuery(namedQuery, clazz)
			         .getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 */
	public <T> T findSingle(Class<T> clazz, String namedQuery, Map<String, Object> parameters) {
		final TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);

		// Map in Set konvertieren fuer for-Schleife
		final Set<Entry<String, Object>> paramSet = parameters.entrySet();
		for (Entry<String, Object> entry : paramSet) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		try {
			return query.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	
	/**
	 */
	public List<?> findUsingSQL(String sql, String resultSetMapping) {
		return em.createNativeQuery(sql, resultSetMapping)
		         .getResultList();
	}

	/**
	 */
	public E create(E obj) {
		em.persist(obj);
		return obj;
	}
	
	/**
	 */
	public E update(E obj) {
		return em.merge(obj);
	}

	/**
	 */
	public void delete(Object obj) {
		if (!em.contains(obj)) {
			final Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(obj);
			obj = em.find(entityClass, id);
		}
		em.remove(obj);
	}
	
	/**
	 */
	public void deleteUsingId(I id) {
		final Object obj = em.find(entityClass, id);
		em.remove(obj);
	}
	
	/**
	 * Beispiel:
	 * public List<Bestellung> findBestellungen(Long kundeId, Bestellung.class) {
	 *    return dao.find(Bestellung.FIND_BY_KUNDEID,
	 *                    with(Bestellung.PARAM_KUNDEID, kundeId).parameters(),
	 *                    Bestellung.class);
	 */
	public static final class QueryParameter {
		private final Map<String, Object> params;
			
		private QueryParameter(String name, Object value) {
			params = new HashMap<>();
			params.put(name, value);
		}
		
		public static QueryParameter with(String name, Object value) {
			return new QueryParameter(name, value);
		}
	
		public QueryParameter and(String name, Object value) {
			params.put(name, value);
			return this;
		}
		
		public Map<String, Object> build() {
			return params;
		}
	}
}
