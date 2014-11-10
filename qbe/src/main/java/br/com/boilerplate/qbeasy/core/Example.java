package br.com.boilerplate.qbeasy.core;

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

import br.com.boilerplate.qbeasy.model.enumerated.MatchingMode;
import br.com.boilerplate.qbeasy.model.interfaces.IdentifiableBySerial;
import br.com.boilerplate.qbeasy.util.ReflectionUtil;

public class Example {
	
	final HashSet<String> excludeFields;
	final IdentifiableBySerial filter;
	final HashMap<String, MatchingMode> matching4Field;
	final HashMap<String, Boolean> ignoreCase4Field;
	final List<Field> fields;
	Map<String, Object> params;
	MatchingMode generalMatchingMode;
	boolean generalIgnoreCase;
	String layer;
	boolean printHql;
	
	public Example(IdentifiableBySerial filter) throws IllegalArgumentException, IllegalAccessException {
		super();
		this.excludeFields = new HashSet<String>();
		this.matching4Field = new HashMap<String, MatchingMode>();
		this.ignoreCase4Field = new HashMap<String, Boolean>();
		this.generalMatchingMode = MatchingMode.EXACT;
		this.generalIgnoreCase = true; 
		this.filter = filter;
		this.fields = ReflectionUtil.getAllFields(filter.getClass());
		this.params = new HashMap<String, Object>();
		layer = "";
		
		if (filter.getClass().isAnnotationPresent(Entity.class))
			excludeInvalidFields(filter);
	}

	private void excludeInvalidFields(IdentifiableBySerial bean) throws IllegalArgumentException, IllegalAccessException {
		Object value = null;
		List<Field> beanFields = ReflectionUtil.getAllFields(bean.getClass());
		for (Field f : beanFields) {
			value = ReflectionUtil.getValue(f, bean);
			
			if(value == null || f.isAnnotationPresent(Transient.class) || Modifier.isStatic(f.getModifiers())
					|| value instanceof Collection<?> && ((Collection<?>)value).isEmpty()) {
				prepareAndExclude(f);
				continue;
			}
			
			if(QBEasy.isInnerIdentifiableBySerial(value)) {
				IdentifiableBySerial ibs = (IdentifiableBySerial) value;
				layerDown(f.getName());
				if (filter.getClass().isAnnotationPresent(Entity.class))
					excludeInvalidFields(ibs);
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
			
			if(QBEasy.isInnerIdentifiableBySerial(value)) {
				IdentifiableBySerial ibs = (IdentifiableBySerial) value;
				layerDown(f.getName());
				excludeZeroes(ReflectionUtil.getAllFields(ibs.getClass()), ibs);
				layerUp();
				continue;
			}
			
			if(value.toString().isEmpty()) {
				prepareAndExclude(f);
			}
		}
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
			for (String layer : layers) {
				sb.append(layer);
				sb.append('.');
			}
			sb.deleteCharAt(sb.length()-1);
			layer = sb.toString();
		}
	}
	
	private void layerDown(String layerName) {
		layer = layer.isEmpty() ? layer + layerName : layer + '.' + layerName;
	}

	public void setPrintHql(boolean printHql) {
		this.printHql = printHql;
	}
}
