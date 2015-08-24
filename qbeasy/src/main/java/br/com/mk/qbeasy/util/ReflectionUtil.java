package br.com.mk.qbeasy.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReflectionUtil {

//	public static List<Field> getAllJpaMappedFields(Class<? extends IdentifiableBySerial> clazz) {
//		List<Field> fields =  new ArrayList<Field>();
//		fields.addAll(Arrays.asList(getAllFields(clazz)));
//		
//		List<Field> notJpaMapped = new ArrayList<Field>();
//		for (Field f : fields) 
//			if()
//				notJpaMapped.add(f);
//		
//		fields.removeAll(notJpaMapped);
//		return fields;
//	}
	
	public static List<Field> getAllFields(Class<?> clazz) {
		List<Class<?>> classes = getAllSuperclasses(clazz);
		classes.add(clazz);
		return Arrays.asList(getAllFields(classes));
	}
	
	/**
	 * Return the value of the field on the bean object
	 * @param f
	 * @param bean
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object getValue(Field f, Object bean) throws IllegalArgumentException, IllegalAccessException {
		f.setAccessible(true);
		return f.get(bean);
	}
	
	public static void setValue(Field f, Object fieldValue, Object bean) throws IllegalArgumentException, IllegalAccessException {
		f.setAccessible(true);
		f.set(bean, fieldValue);
	}
	
	public static Field getFieldByName(String name, Object bean) throws NoSuchFieldException {
		List<Field> fields = getAllFields(bean.getClass());
		for (Field field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		throw new NoSuchFieldException();
	}
	
	public static boolean containsFieldWithSameName(String fieldName, Object bean) {
		List<Field> fields = getAllFields(bean.getClass());
		for (Field f : fields) {
			if (f.getName().equals(fieldName)) {
				return true;
			}
		}
		return false;
	}
	
	private static Field[] getAllFields(List<Class<?>> classes) {
		Set<Field> fields = new HashSet<Field>();
		for (Class<?> clazz : classes) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		}
		return fields.toArray(new Field[fields.size()]);
	}

	public static Field getField(String fieldName, Class<?> clazz) throws NoSuchFieldException {
		List<Field> fields = getAllFields(clazz);
		for (Field field : fields) {
			if(field.getName().equals(fieldName))
				return field;
		}
		throw new NoSuchFieldException();
	}
	
	public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		Class<?> superclass = clazz.getSuperclass();
		while (superclass != null) {
			classes.add(superclass);
			superclass = superclass.getSuperclass();
		}
		return classes;
	}
	
	public static <X> boolean equalLists(List<X> one, List<X> two){     
	    if (one == null && two == null){
	        return true;
	    }

	    if((one == null && two != null) 
	      || one != null && two == null
	      || one.size() != two.size()) {
	        return false;
	    }
	    
	    return one.containsAll(two) && two.containsAll(one);
	}
	
	public static boolean isPrimitiveOrPrimitiveWrapperOrString(Class<?> type) {
	    return (type.isPrimitive() && type != void.class) ||
	        type == Double.class || type == Float.class || type == Long.class ||
	        type == Integer.class || type == Short.class || type == Character.class ||
	        type == Byte.class || type == Boolean.class || type == String.class;
	}
}
