package br.com.mk.qbeasy.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import br.com.mk.qbeasy.model.QuerySimpleModel;
import br.com.mk.qbeasy.model.enumerated.JoinType;
import br.com.mk.qbeasy.model.enumerated.MatchingMode;
import br.com.mk.qbeasy.model.enumerated.OperationType;
import br.com.mk.qbeasy.model.enumerated.QueryConjuction;
import br.com.mk.qbeasy.model.interfaces.annotations.Exclude;
import br.com.mk.qbeasy.model.interfaces.annotations.PositionAtQuery;
import br.com.mk.qbeasy.model.interfaces.annotations.QueryField;
import br.com.mk.qbeasy.util.ReflectionUtil;

/**
 * Responsable for build jpql Strings 
 * @author Marcos
 *
 */
public class QueryStringBuilder {
	protected StringBuilder selectBuilder;
	protected StringBuilder fromBuilder;
	protected StringBuilder whereBuilder;
	protected final HashMap<Object, String> aliasMap;
	protected Example ex;
	private String queryLayer;
	private int paramNum;
	private boolean countQuery;
	private static final String PARAMETER_NAME_PREFIX = "param";
	
	public QueryStringBuilder(Example ex) {
		super();
		this.ex = ex;
		aliasMap = new HashMap<Object, String>();
	}
	
	protected String getQueryString() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		countQuery = false;
		return build();
	}
	
	protected String getQueryString(String additionalDefinitions) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		countQuery = false;
		return build(additionalDefinitions);
	}
	
	public static QuerySimpleModel getQueryStringByFieldName(String fieldName, Object fieldValue, Class<?> clazz) {
		StringBuilder sb = new StringBuilder("select x from ");
		sb.append(clazz.getSimpleName());
		OperationType op = decideOperation(fieldValue);
		
		switch (op) {
		case EQUAL:
			sb.append(" x where x." + fieldName + " = ");
			break;
		case LIKE:
			sb.append(" x where lower(x." + fieldName + ") like lower(:param1)");
			break;
		case IN:
			sb.append(" x where x." + fieldName +" in (:param1)");
			break;
		default:
			throw new UnsupportedOperationException("Dont use this method with a pojo as target");
		}
		
		QuerySimpleModel model = new QuerySimpleModel();
		model.getParams().put("param1", fieldValue);
		model.setQueryString(sb.toString());
		
		return model;
	}
	
	public String getPaginatedQueryString(String order, String orderFieldName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		countQuery = false;
		String orderby = " order by " + aliasMap.get(ex.filter) + '.' + orderFieldName + " " + order;
		return build() + orderby;
	}
	
	protected String getCountString() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		countQuery = true;
		return build();
	}

	private String build() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		resetBuilders();
		
		initialize(ex);
		iterateOverClass(ex.filter.getClass(), ex.filter);
		
		String queryString = selectBuilder.toString() + fromBuilder.toString() + whereBuilder.toString();
		if(ex.printHql) 
			System.out.println(queryString);
		
		return queryString;
	}
	
	private String build(String additionalDefinitions) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String queryString = build() + additionalDefinitions;
		return queryString;
	}
	
	private void resetBuilders() {
		selectBuilder = new StringBuilder("select ");
		fromBuilder = new StringBuilder(" from ");
		whereBuilder = new StringBuilder(" where 1=1 ");
	}

	private void iterateOverClass(Class<?> clazz, Object bean) throws IllegalAccessException,InvocationTargetException, NoSuchMethodException {
		List<Field> fields = ReflectionUtil.getAllFields(clazz);
		fields = getOrdered(fields);
		for (Field f : fields) {
			if(isExcludedByUser(f)) 
				continue;
			
			Object fieldValue = ReflectionUtil.getValue(f, bean);
			QueryField queryFieldAnnotation = f.getAnnotation(QueryField.class);
			
			if ( (fieldValue == null && queryFieldAnnotation == null) || (fieldValue == null && !queryFieldAnnotation.isNullValid()) ) 
				continue;
			
			OperationType operationType;
			if (queryFieldAnnotation != null) {
				operationType = queryFieldAnnotation.operationType();
			} else {
				operationType = getOperationTypeForValue(fieldValue,f);
			}
			
			appendToQueryStringAccordingToOperationType(bean, fieldValue, f, operationType); 
		}
	}

	private List<Field> getOrdered(List<Field> fields) {
		List<Field> ordered = new ArrayList<Field>();
		List<Field> positionDefined = new ArrayList<Field>();
		PositionAtQuery pos = null;
		
		for (Field field : fields) {
			pos = field.getAnnotation(PositionAtQuery.class);
			if (pos != null) {
				positionDefined.add(field);
			} else {
				ordered.add(field);
			}
		}
		
		for (Field field : positionDefined) {
			ordered.add(field);
		}
		
		Collections.sort(positionDefined, new Comparator<Field>() {

			public int compare(Field o1, Field o2) {
				Integer o1Pos = o1.getAnnotation(PositionAtQuery.class).position();
				Integer o2Pos = o2.getAnnotation(PositionAtQuery.class).position();
				return o1Pos.compareTo(o2Pos);
			}
		
		});
		
		int index; 
		for (Field field : positionDefined) {
			try {
				index = field.getAnnotation(PositionAtQuery.class).position();
				ordered.add(index, field);
			} catch (IndexOutOfBoundsException e) {
				ordered.add(field);
			}
		}
		
		return ordered;
	}
	
	/**
	 * Append restriction or join clause, in case of join, call <code>iterateOverClass</code> recursively 
	 * @param bean
	 * @param fieldValue
	 * @param f
	 * @param operationType
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	private void appendToQueryStringAccordingToOperationType(Object bean, Object fieldValue, Field f, OperationType operationType) 
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	
		if(operationType == OperationType.LIKE 
				|| operationType == OperationType.EQUAL 
				|| operationType == OperationType.IN
				|| operationType == OperationType.AFTER_OR_AND_BEFORE) {
		
			restrict(bean, fieldValue, f.getName(), operationType);
	
		} else {
			join(f, fieldValue, bean);
			layerDown(f.getName());
			iterateOverClass(fieldValue.getClass(), fieldValue);
			layerUp();
		}
	}

	private OperationType getOperationTypeForValue(Object fieldValue, Field f) {
		OperationType operationType = null;
		if(f.getType().isPrimitive()) {
			operationType = OperationType.EQUAL;
		} else {
			operationType = decideOperation(fieldValue);
		}
		return operationType;
	}
	
	private static OperationType decideOperation(Object fieldValue) {
		if (fieldValue instanceof Collection<?>) {
			return OperationType.IN;
		}
		if (fieldValue instanceof Enum || fieldValue instanceof String) {
			return OperationType.LIKE;
		}
		if (QBEasy.isInnerEntity(fieldValue)) {
			return OperationType.JOIN;
		} else {
			QueryField qf = QBEasy.getQueryFieldDefinition(fieldValue);
			
			if (!qf.after().isEmpty() || !qf.after().isEmpty()) {
				return OperationType.AFTER_OR_AND_BEFORE;
			}
			
			return OperationType.EQUAL;
		}
	}
	
	private boolean isExcludedByUser(Field f) {
		String excludeName = queryLayer.isEmpty() ? f.getName() : queryLayer + '.' +f.getName();
		if (f.isAnnotationPresent(Exclude.class) || ex.excludeFields.contains(excludeName))
			return true;
		else 
			return false;
	}
	
	private String getNextParamName() {
		String name = PARAMETER_NAME_PREFIX + paramNum;
		paramNum++;
		return name;
	}

	private void restrict(Object bean, Object fieldValue, String fieldName, OperationType operationType) {
		QueryField qf = QBEasy.getQueryFieldDefinition(bean);
		QueryConjuction qc;
		if (qf != null) {
			qc = qf.conjuction();
		} else {
			qc = QueryConjuction.AND;
		}
		
		whereBuilder.append(qc);
		
		if (operationType == OperationType.LIKE) {
			appendLikeRestriction(bean, fieldValue, fieldName);
		} else if(operationType == OperationType.EQUAL){
			appendEqualsRestriction(bean, fieldValue, fieldName);
		} else if (operationType == OperationType.AFTER_OR_AND_BEFORE) {
			appendDateRestrictions(qf.before(), qf.after(), bean, fieldValue, fieldName);
		} else {
			appendInRestriction(bean, fieldValue, fieldName);
		}
	}
	
	private void appendDateRestrictions(String before, String after, Object bean, Object fieldValue, String fieldName) {
		restrictFieldName(bean, fieldName);
		boolean between = !before.isEmpty() && after.isEmpty();
		if (between) {
			String paramName1 = getNextParamName();
			String paramName2 = getNextParamName();
			whereBuilder.append(" between :" + paramName1 + QueryConjuction.AND + ':' +paramName2);
			ex.getParams().put(paramName1, before);
			ex.getParams().put(paramName2, after);
		} else {
			String paramName = getNextParamName();
			if (!before.isEmpty()) {
				whereBuilder.append(" <= :" + paramName);
				ex.getParams().put(paramName, before);
			} else {
				whereBuilder.append(" >= :" + paramName);
				ex.getParams().put(paramName, after);
			}
		}
	}

	private void appendInRestriction(Object bean, Object fieldValue, String fieldName) {
		Collection<?> collection = (Collection<?>) fieldValue;
		restrictFieldName(bean, fieldName);

		String paramName = getNextParamName();
		whereBuilder.append(" in (:");
		whereBuilder.append(paramName);
		whereBuilder.append(") ");
		ex.getParams().put(paramName, collection);
	}

	private void appendEqualsRestriction(Object bean, Object fieldValue, String fieldName) {
		restrictFieldName(bean, fieldName);

		whereBuilder.append(" = :");
		String paramName = getNextParamName();
		whereBuilder.append(paramName);
		ex.getParams().put(paramName, fieldValue);
	}

	private void appendLikeRestriction(Object bean, Object fieldValue, String fieldName) {
		Boolean ignoreCase = ex.ignoreCase4Field.containsKey(fieldName) ? ex.ignoreCase4Field.get(fieldName) : ex.generalIgnoreCase;
		MatchingMode mode = ex.matching4Field.containsKey(fieldName) ? ex.matching4Field.get(fieldName) : ex.generalMatchingMode;
		String value = ignoreCase ? fieldValue.toString().toLowerCase() : fieldValue.toString();
		
		whereBuilder.append("lower(");
		restrictFieldName(bean, fieldName);
		whereBuilder.append(") like "); 
		
		String likeString;
		if(fieldValue instanceof Enum && value.substring(0, 1).equals("'") && value.substring(value.length()-1).equals("'"))
			likeString = value;
		else 
			likeString = createLikeString(mode, value);
		
		whereBuilder.append(likeString);
	}
	
	private void restrictFieldName(Object bean, String fieldName) {
		whereBuilder.append(aliasMap.get(bean));
		whereBuilder.append('.');
		whereBuilder.append(fieldName);
	}

	private void join(Field field, Object fieldValue, Object bean) {
		String alias = decideAlias(field);
		aliasMap.put(fieldValue, alias);
		
		fromBuilder.append(JoinType.INNER);
		fromBuilder.append(aliasMap.get(bean));
		fromBuilder.append('.');
		fromBuilder.append(field.getName());
		fromBuilder.append(" as ");
		fromBuilder.append(alias);
	}
	
	private String decideAlias(Field field) {
		String rawAlias = field.getName();
		if(!aliasMap.containsValue(rawAlias))
			return rawAlias;
		int comp = 0;
		while(true) {
			if(!aliasMap.containsValue(rawAlias + comp))
				return rawAlias + comp;
			comp++;
		}
	}
	
	private String createLikeString(MatchingMode mode, String value) {
		if(mode == MatchingMode.ANYWHERE)
			return "'%" + value + "%'";
		else if (mode == MatchingMode.END)
			return "'" + value + "%'";
		else if(mode == MatchingMode.START)
			return "'%" + value + "'";
		else 
			return "'" + value + "'";
	}
	
	private void initialize(Example ex) {
		String clazzName = ex.filter.getClass().getSimpleName();
		String alias =  clazzName.toLowerCase();
		
		aliasMap.put(ex.filter, alias);
		fromBuilder.append(clazzName + " " + alias);

		String appendValue = countQuery ? ("count(" + alias + ')'): alias;
		selectBuilder.append(appendValue);
		
		queryLayer = ""; 
	}
	
	private void layerUp() {
		String[] layers = queryLayer.split("\\.");
		if(layers.length == 1) {
			queryLayer = "";
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < layers.length - 1; i++) {
				sb.append(layers[i]);
				sb.append('.');
			}
			sb.deleteCharAt(sb.length()-1);
			queryLayer = sb.toString();
		}
	}
	
	private void layerDown(String layerName) {
		queryLayer = queryLayer.isEmpty() ? queryLayer + layerName : queryLayer + '.' + layerName;
	}

	public Example getEx() {
		return ex;
	}

	public void setEx(Example ex) {
		this.ex = ex;
	}
}
