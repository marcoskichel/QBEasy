package br.com.mk.qbeasy.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import br.com.mk.qbeasy.model.enumerated.MatchingMode;
import br.com.mk.qbeasy.model.enumerated.QueryConjuction;
import br.com.mk.qbeasy.model.interfaces.annotations.DefaultBooleanQueryValue;
import br.com.mk.qbeasy.model.interfaces.annotations.QueryField;
import br.com.mk.qbeasy.util.ReflectionUtil;

public class Example {
	
	final HashSet<String> excludeFields;
	final Object filter;
	
	MatchingMode generalMatchingMode;
	final HashMap<String, MatchingMode> matching4Field;

	boolean generalIgnoreCase;
	final HashMap<String, Boolean> ignoreCase4Field;

	Boolean joinAll;
	final HashMap<String, Boolean> joins;

	QueryConjuction generalQueryConjunction;
	
	private Map<String, Object> params;
	final List<Field> fields;
	
	String orderBy, groupBy;

	String layer;
	boolean printHql;
	String extraRestrictions;
	
	public Example(Object filter) throws IllegalArgumentException, IllegalAccessException {
		super();
		this.excludeFields = new HashSet<String>();
		this.matching4Field = new HashMap<String, MatchingMode>();
		this.joins = new HashMap<String, Boolean>();
		this.ignoreCase4Field = new HashMap<String, Boolean>();
		this.generalMatchingMode = MatchingMode.EXACT;
		this.generalIgnoreCase = true; 
		generalQueryConjunction = QueryConjuction.AND;
		this.filter = filter;
		this.fields = ReflectionUtil.getAllFields(filter.getClass());
		this.params = new HashMap<String, Object>();
		extraRestrictions = new String();
		layer = "";
		
		
		if (filter.getClass().isAnnotationPresent(Entity.class))
			excludeInvalidFields(filter);
	}

	private void excludeInvalidFields(Object bean) throws IllegalArgumentException, IllegalAccessException {
		Object value = null;
		List<Field> beanFields = ReflectionUtil.getAllFields(bean.getClass());
		for (Field f : beanFields) {
			value = ReflectionUtil.getValue(f, bean);
			
			if(value == null || f.isAnnotationPresent(Transient.class) || Modifier.isStatic(f.getModifiers())
					|| value instanceof Collection<?> && ((Collection<?>)value).isEmpty()) {
				prepareAndExclude(f);
				continue;
			}
			
			if(QBEasy.isInnerEntity(value)) {
				layerDown(f.getName());
				if (filter.getClass().isAnnotationPresent(Entity.class))
					excludeInvalidFields(value);
				layerUp();
			}
		}
	}

	/**
	 * Define a matching mode for an specific field
	 * @param fieldName
	 * @param matchingMode
	 */
	public void setMatchingMode4Field(String fieldName, MatchingMode matchingMode) {
		matching4Field.put(fieldName, matchingMode);
	}
	
	/**
	 * define an matching mode for general fields
	 */
	public void setMatchingMode(MatchingMode matchingMode) {
		generalMatchingMode = matchingMode;
	}
	
	/**
	 * Exclude all the null and empty fields
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public void excludeZeroes() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		excludeZeroes(fields, filter);
	}
	
	private void excludeZeroes(List<Field> fields, Object bean) throws IllegalArgumentException, IllegalAccessException {
		Object value = null;
		for (Field f : fields) {
			if(isExcluded(f))
				continue;
			
			value = ReflectionUtil.getValue(f, bean);
			
			if(QBEasy.isInnerEntity(value)) {
				
				layerDown(f.getName());
				excludeZeroes(ReflectionUtil.getAllFields(value.getClass()), value);
				layerUp();
				continue;
			
			}
			
			QueryField qf = f.getAnnotation(QueryField.class);
			boolean defaultQueryValue = !isDefaultQueryValue(f);
			if(value.toString().isEmpty() && (qf == null || !qf.isEmptyValid()) && !defaultQueryValue) {
				prepareAndExclude(f);
			}
		}
	}

	private boolean isDefaultQueryValue(Field f) {
		return f.isAnnotationPresent(DefaultBooleanQueryValue.class);
	}

	private boolean isExcluded(Field f) {
		String excludeName = layer.isEmpty() ? f.getName() : layer + '.' + f.getName();
		return excludeFields.contains(excludeName);
	}

	private void prepareAndExclude(Field f) {
		String excludeName = layer.isEmpty() ? f.getName() : layer + '.' + f.getName();
		excludeFieldByName(excludeName);
	}
	
	public void excludeFieldByName(String fieldName) {
		excludeFields.add(fieldName);
	}
	
	public void ignoreCase(boolean value) {
		generalIgnoreCase = value;
	}
	
	public void ignoreCase4Field(String fieldName, Boolean ignoreCase) {
		ignoreCase4Field.put(fieldName, ignoreCase);
	}
	
	private void layerUp() {
		String[] layers = layer.split("\\.");
		if(layers.length == 1) {
			layer = "";
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < layers.length - 1; i++) {
				sb.append(layers[i]);
				sb.append('.');
			}
			sb.deleteCharAt(sb.length()-1);
			layer = sb.toString();
		}
	}
	
	public void addRestrictions(String restrictions, Map<String, Object> params) {
		extraRestrictions = restrictions;
		params.putAll(params);
	}
	
	private void layerDown(String layerName) {
		layer = layer.isEmpty() ? layer + layerName : layer + '.' + layerName;
	}

	public void setPrintHql(boolean printHql) {
		this.printHql = printHql;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public QueryConjuction getGeneralQueryConjunction() {
		return generalQueryConjunction;
	}

	public void setGeneralQueryConjunction(QueryConjuction generalQueryConjunction) {
		this.generalQueryConjunction = generalQueryConjunction;
	}

	public Boolean getJoinAll() {
		return joinAll;
	}

	public void setJoinAll(Boolean joinAll) {
		this.joinAll = joinAll;
	}

	public HashMap<String, Boolean> getJoins() {
		return joins;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void orderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void groupBy(String groupBy) {
		this.groupBy = groupBy;
	}
}
