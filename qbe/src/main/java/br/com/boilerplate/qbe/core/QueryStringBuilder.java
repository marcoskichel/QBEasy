package br.com.boilerplate.qbe.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;

import br.com.boilerplate.qbe.model.enumerated.MatchingMode;
import br.com.boilerplate.qbe.model.enumerated.OperationType;
import br.com.boilerplate.qbe.model.interfaces.ExcludeFromQBE;
import br.com.boilerplate.qbe.model.interfaces.IdentifiableBySerial;
import br.com.boilerplate.qbe.util.ReflectionUtil;

/**
 * Responsable for build hql Strings 
 * @author Marcos
 *
 */
class QueryStringBuilder {
	protected StringBuilder selectBuilder;
	protected StringBuilder fromBuilder;
	protected StringBuilder whereBuilder;
	protected final HashMap<Object, String> aliasMap;
	protected Example ex;
	private String queryLayer;
	private int paramNum;
	private boolean countQuery;
	private static final String PARAMETER_NAME_PREFIX = "param";
	
	protected QueryStringBuilder(Example ex) {
		super();
		this.ex = ex;
		aliasMap = new HashMap<Object, String>();
	}
	
	protected String getQueryString() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		countQuery = false;
		return build();
	}
	
	protected String getPaginatedQueryString(String order, String orderFieldName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
		return selectBuilder.toString() + fromBuilder.toString() + whereBuilder.toString();
	}
	
	private void resetBuilders() {
		selectBuilder = new StringBuilder("select ");
		fromBuilder = new StringBuilder(" from ");
		whereBuilder = new StringBuilder(" where 1=1 ");
	}

	private void iterateOverClass(Class<?> clazz, Object bean) throws IllegalAccessException,InvocationTargetException, NoSuchMethodException {
		Field[] fields = ReflectionUtil.getAllFields(clazz);
		for (Field f : fields) {
			if(isExcludedByUser(f)) 
				continue;
			
			Object fieldValue = ReflectionUtil.getValue(f, bean);;	
			if(isNullOrEmpty(fieldValue)) 
				continue;
			
			OperationType operationType = getOperationTypeForValue(fieldValue,f);
			appendToQueryStringAccordingToOperationType(bean, fieldValue, f, operationType); 
		}
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
		if(operationType == OperationType.LIKE || operationType == OperationType.EQUAL || operationType == OperationType.IN) {
			restrict(bean, fieldValue, f.getName(), operationType);
		} else {
			join(f, fieldValue, bean);
			layerDown(f.getName());
			iterateOverClass(fieldValue.getClass(), fieldValue);
			layerUp();
		}
	}

	private OperationType getOperationTypeForValue(Object fieldValue, Field f) {
		OperationType	 operationType = null;
		if(f.getType().isPrimitive()) {
			operationType = OperationType.EQUAL;
		} else {
			operationType = decideOperation(fieldValue);
		}
		return operationType;
	}
	
	private OperationType decideOperation(Object bean) {
		if(bean instanceof IdentifiableBySerial)
			return OperationType.JOIN;
		if(bean instanceof Collection<?>)
			return OperationType.IN;
		if (bean instanceof Number) 
			return OperationType.EQUAL;
		if (bean instanceof Boolean) 
			return OperationType.EQUAL;
		if (bean instanceof Enum)
			return OperationType.LIKE;
		if (bean instanceof String) 
			return OperationType.LIKE;
		if (bean instanceof Character)
			return OperationType.EQUAL;
	
		return OperationType.JOIN;
	}

	private boolean isNullOrEmpty(Object fieldValue) throws IllegalAccessException {
		if (fieldValue == null || (fieldValue instanceof Collection<?> && ((Collection<?>)fieldValue).isEmpty()))
			return true;
		else
			return false;
	}

	private boolean isExcludedByUser(Field f) {
		String excludeName = queryLayer.isEmpty() ? f.getName() : queryLayer + '.' +f.getName();
		if (f.isAnnotationPresent(ExcludeFromQBE.class) || ex.excludeFields.contains(excludeName))
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
		whereBuilder.append(" and ");
		
		if (operationType == OperationType.LIKE) {
			appendLikeRestriction(bean, fieldValue, fieldName);
		} else if(operationType == OperationType.EQUAL){
			appendEqualsRestriction(bean, fieldValue, fieldName);
		} else {
			appendInRestriction(bean, fieldValue, fieldName);
		}
	}

	private void appendInRestriction(Object bean, Object fieldValue,
			String fieldName) {
		Collection<?> collection = (Collection<?>) fieldValue;
		restrictFieldName(bean, fieldName);

		String paramName = getNextParamName();
		whereBuilder.append(" in (:");
		whereBuilder.append(paramName);
		whereBuilder.append(") ");
		ex.params.put(paramName, collection);
	}

	private void appendEqualsRestriction(Object bean, Object fieldValue,
			String fieldName) {
		restrictFieldName(bean, fieldName);
		whereBuilder.append(" = ");
		whereBuilder.append(fieldValue);
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
		
		fromBuilder.append(" join ");
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
			for (String layer : layers) {
				sb.append(layer);
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
