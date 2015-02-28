package br.com.mk.qbeasy.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.com.mk.qbeasy.model.QuerySimpleModel;
import br.com.mk.qbeasy.model.exception.QBEasyException;
import br.com.mk.qbeasy.model.interfaces.ExampleGenerator;
import br.com.mk.qbeasy.model.interfaces.annotations.QueryField;

public class QBEasy {
	
	private EntityManager em;
	private QueryStringBuilder qf;
	private static QBEasy instance;
	
	private Integer offSet, limit;
	
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
	
	static QueryField getQueryFieldDefinition(Object bean) {
		return bean.getClass().getAnnotation(QueryField.class);
	}
	
	static QueryField getQueryFieldDefinition(Field field) {
		return field.getAnnotation(QueryField.class);
	}
	
	/**
	 * Generate Query using <code>CodeGenerator.DEFAULT_GENERATOR</code> to configure the example
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T> List<T> getList(T entity) throws QBEasyException {
		try {
			Example e = ExampleGenerator.DEFAULT_GENERATOR.generate(entity);
			return getList(e);
		} catch (Exception exception) {
			throw new QBEasyException(exception);
		}
	}
	
	/**
	 * Generate Query using the example previously configured
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Example exemplo) throws QBEasyException {
		String queryString = generateQueryStringFromNewFactoryInstance(exemplo);
		Query q = em.createQuery(queryString);
		
		if (offSet != null) {
			q.setFirstResult(offSet);
			offSet = null;
		}
		
		if (limit != null) {
			q.setMaxResults(limit);
			limit = null;
		}
	
		setParameters(q, exemplo.getParams());
		return q.getResultList();
	}
	
	/**
	 * Generate Query using <code>CodeGenerator.DEFAULT_GENERATOR</code> to configure the example
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T> T getSingle(T entity) throws QBEasyException {
		try {
			Example e = ExampleGenerator.DEFAULT_GENERATOR.generate(entity);
			return getSingle(e);
		} catch (Exception exception) {
			throw new QBEasyException(exception);
		}
	}
	
	/**
	 * Generate Query using the example previously configured
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSingle(Example exemplo) throws QBEasyException {
		String queryString = generateQueryStringFromNewFactoryInstance(exemplo);
		Query q = em.createQuery(queryString);
		setParameters(q, exemplo.getParams());
		return (T) q.getSingleResult();
	}
	
	
	/**
	 * Return the number of matches in the data base to the given Example
	 * @param exemplo
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <T> Long total(T entity) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
		setParameters(q, exemplo.getParams());
		return (Long) q.getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T querySingleByField(String fieldName, Object fieldValue, Class<T> objectClazz) throws QBEasyException {
		try {
			Query q = prepareQuery(fieldName, fieldValue, objectClazz);
			return (T) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> queryByField(String fieldName, Object fieldValue, Class<T> objectClazz) throws QBEasyException {
		Query q = prepareQuery(fieldName, fieldValue, objectClazz);
		return (List<T>) q.getResultList();
	}
	
	private <T> Query prepareQuery(String fieldName, Object fieldValue, Class<T> objectClazz) {
		QuerySimpleModel model = QueryStringBuilder.getQueryStringByFieldName(fieldName, fieldValue, objectClazz);
		Query q = em.createQuery(model.getQueryString());
		setParameters(q, model.getParams());
		return q;
	}
	
	public static void main(String[] args) {
		Object o = new Object();
		System.out.println(o.getClass());
	}
	
	protected static boolean isInnerEntity(Object o) {
		Class<?> clazz = o.getClass();
		while (clazz != Object.class) {
			if (clazz.isAnnotationPresent(Entity.class)) {
				return true;
			}
			
			clazz = clazz.getSuperclass();
		}
		
		return false;
	}
	
	private static String generateQueryStringFromNewFactoryInstance(Example exemplo) throws QBEasyException {
		try {
			QueryStringBuilder factoryInstance = new QueryStringBuilder(exemplo);
			String queryString = factoryInstance.getQueryString();
			return queryString;
		} catch (Exception exception) {
			throw new QBEasyException(exception);
		}
	}
	
	
	private void setParameters(Query q, Map<String, Object> params) {
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			q.setParameter(entry.getKey(), entry.getValue());
		}
	}

	public QBEasy setFirstResult(Integer offSet) {
		this.offSet = offSet;
		return this;
	}

	public QBEasy setMaxResults(Integer limit) {
		this.limit = limit;
		return this;
	}
}
