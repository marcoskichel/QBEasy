package br.com.boilerplate.qbeasy.util;

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
	
	private static Field[] getAllFields(List<Class<?>> classes) {
		Set<Field> fields = new HashSet<Field>();
		for (Class<?> clazz : classes) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		}
		return fields.toArray(new Field[fields.size()]);
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
}
