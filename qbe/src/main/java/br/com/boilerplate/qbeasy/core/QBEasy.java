package br.com.boilerplate.qbeasy.core;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import br.com.boilerplate.qbeasy.model.interfaces.ExampleGenerator;
import br.com.boilerplate.qbeasy.model.interfaces.IdentifiableBySerial;

public class QBEasy {
	private EntityManager em;
	private QueryStringBuilder qf;
	private static QBEasy instance;
	
	private QBEasy(EntityManager em) {
		super();
		this.em = em;
	}
	
	/**
	 * Defines the entityManager to query the data base
	 * @param em
	 * @return An instance of the QBEasy object enabled to query the data base
	 */
	public static QBEasy managedBy(EntityManager em) {
		if (instance == null || !em.equals(em))
			instance = new QBEasy(em);
		return instance;
	}
	
	/**
	 * Generate Query using the example previously configured
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T extends IdentifiableBySerial> List<T> getList(ExampleGenerator generator, T entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Example e = generator.generate(entity);
		return getList(e);
	}
	
	/**
	 * Generate Query using <code>CodeGenerator.DEFAULT_GENERATOR</code> to configure the example
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T extends IdentifiableBySerial> List<T> getList(T entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Example e = ExampleGenerator.DEFAULT_GENERATOR.generate(entity);
		return getList(e);
	}
	
	/**
	 * Generate Query using the example previously configured
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public <T extends IdentifiableBySerial> List<T> getList(Example exemplo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String queryString = generateQueryStringFromNewFactoryInstance(exemplo);
		Query q = em.createQuery(queryString);
		setParameters(q, exemplo.params);
		return q.getResultList();
	}

	/**
	 * Generate Query using the example previously configured
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T extends IdentifiableBySerial> T getSingle(ExampleGenerator generator, T entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Example e = generator.generate(entity);
		return getSingle(e);
	}
	
	/**
	 * Generate Query using <code>CodeGenerator.DEFAULT_GENERATOR</code> to configure the example
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T extends IdentifiableBySerial> T getSingle(T entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Example e = ExampleGenerator.DEFAULT_GENERATOR.generate(entity);
		return getSingle(e);
	}
	
	/**
	 * Generate Query using the example previously configured
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public <T extends IdentifiableBySerial> T getSingle(Example exemplo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NonUniqueResultException, NoResultException {
		String queryString = generateQueryStringFromNewFactoryInstance(exemplo);
		Query q = em.createQuery(queryString);
		setParameters(q, exemplo.params);
		return (T) q.getSingleResult();
	}
	
	/**
	 * Query the data base lazily, designed to be used with <code>GenericLazyList</code> or an implementation of Primefaces LazyDataModel,
	 * uses the <code>ExampleGenerator.DEFAULT_GENERATOR</code> to configure the example.
	 * @param exemplo
	 * @param first
	 * @param rowsPerPage
	 * @param sortOrder
	 * @param sortFieldName
	 * @return An list limited by the first to first + rows number of results
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T extends IdentifiableBySerial> List<T> getPaginatedList(T entity, Integer first, Integer rowsPerPage, String sortOrder, String sortFieldName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Example e = ExampleGenerator.DEFAULT_GENERATOR.generate(entity);
		return getPaginatedList(e, first, rowsPerPage, sortOrder, sortFieldName);
	}
	
	/**
	 * Query the data base lazily, designed to be used with <code>GenericLazyList</code> or an implementation of Primefaces LazyDataModel
	 * @param exemplo
	 * @param first
	 * @param rowsPerPage
	 * @param sortOrder
	 * @param sortFieldName
	 * @return An list limited by the first to first + rows number of results
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public <T extends IdentifiableBySerial> List<T> getPaginatedList(Example exemplo, Integer first, Integer rowsPerPage, String sortOrder, String sortFieldName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		qf = new QueryStringBuilder(exemplo);
		Query q = em.createQuery(qf.getPaginatedQueryString(sortOrder, sortFieldName));
		q.setFirstResult(first);
		q.setMaxResults(rowsPerPage);
		setParameters(q, exemplo.params);
		return q.getResultList();
	}
	
	/**
	 * Return the number of matches in the data base to the given Example
	 * @param exemplo
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T extends IdentifiableBySerial> Long total(T entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Example e = ExampleGenerator.DEFAULT_GENERATOR.generate(entity);
		return total(e);
	}
	
	/**
	 * Return the number of matches in the data base using an example configured using <code>ExampleGenerator.DEFAULT_GENERATOR</code>
	 * @param exemplo
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public Long total(Example exemplo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		qf = new QueryStringBuilder(exemplo);
		Query q = em.createQuery(qf.getCountString());
		setParameters(q, exemplo.params);
		return (Long) q.getSingleResult();
	}
	
	protected static boolean isInnerIdentifiableBySerial(Object o) {
		if(o instanceof IdentifiableBySerial) 
			return true;
		else 
			return false;
	}
	
	private void setParameters(Query q, Map<String, Object> params) {
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			q.setParameter(entry.getKey(), entry.getValue());
		}
	}
	
	private String generateQueryStringFromNewFactoryInstance(Example exemplo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		QueryStringBuilder factoryInstance = new QueryStringBuilder(exemplo);
		String queryString = factoryInstance.getQueryString();
		return queryString;
	}
}
