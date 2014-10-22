package br.com.boilerplate.qbe.core;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import br.com.boilerplate.qbe.model.interfaces.IdentifiableBySerial;

public class QBE {
	private EntityManager em;
	private QueryStringBuilder qf;
	private static QBE instance;
	
	private QBE(EntityManager em) {
		super();
		this.em = em;
	}
	
	public static QBE using(EntityManager em) {
		if (instance == null || !em.equals(em))
			instance = new QBE(em);
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Example exemplo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String queryString = generateQueryStringFromNewFactoryInstance(exemplo);
		Query q = em.createQuery(queryString);
		setParameters(q, exemplo.params);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public <T> T getSingle(Example exemplo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NonUniqueResultException, NoResultException {
		String queryString = generateQueryStringFromNewFactoryInstance(exemplo);
		Query q = em.createQuery(queryString);
		setParameters(q, exemplo.params);
		return (T) q.getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getPaginatedList(Example exemplo, Integer first, Integer rowsPerPage, String sortOrder, String sortFieldName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		qf = new QueryStringBuilder(exemplo);
		Query q = em.createQuery(qf.getPaginatedQueryString(sortOrder, sortFieldName));
		q.setFirstResult(first);
		q.setMaxResults(rowsPerPage);
		setParameters(q, exemplo.params);
		return q.getResultList();
	}
	
	public Long total(Example exemplo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		qf = new QueryStringBuilder(exemplo);
		Query q = em.createQuery(qf.getCountString());
		setParameters(q, exemplo.params);
		return (Long) q.getSingleResult();
	}
	
	protected static boolean isInnerPojo(Object bean) {
		if(bean instanceof IdentifiableBySerial) 
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
