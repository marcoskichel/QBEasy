package br.com.boilerplate.qbe.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

public class QBE {
	private EntityManager em;
	
	private QBE(EntityManager em) {
		super();
		this.em = em;
	}
	
	public static QBE using(EntityManager em) {
		return new QBE(em);
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> query(Exemplo exemplo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return em.createQuery(new QueryFactory(exemplo).buildQueryString()).getResultList();
	}
	
	protected static boolean isInnerPojo(Object bean) {
		if (bean instanceof Number) 
			return false;
		if (bean instanceof Boolean) 
			return false;
		if (bean instanceof Enum)
			return false;
		if (bean instanceof String) 
			return false;
		if (bean instanceof Character)
			return false;
		if (bean instanceof Date) 
			return false;
		return true;
	}
}
