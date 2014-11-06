package br.com.boilerplate.qbeasy.model.interfaces;

import java.lang.reflect.InvocationTargetException;

import br.com.boilerplate.qbeasy.core.Example;
import br.com.boilerplate.qbeasy.model.enumerated.MatchingMode;

public abstract class ExampleGenerator {
	
	/**
	 * Generates examples ignoring empty Strings, with ignore case triggered and using 
	 * MatchMode.Anywhere
	 */
	public static final ExampleGenerator DEFAULT_GENERATOR = new ExampleGenerator() {
		@Override
		public Example generate(IdentifiableBySerial filter) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			Example e = new Example(filter);
			e.setPrintHql(true);
			e.ignoreCase(true);
			e.setMatchingMode(MatchingMode.ANYWHERE);
			e.excludeZeroes();
			return e;
		}
	};
	
	public static final ExampleGenerator ANYWHERE_MATCHING_GENERATOR = new ExampleGenerator() {
		@Override
		public Example generate(IdentifiableBySerial filter) {
			Example example = null;
			try {
				example = new Example(filter);
				example.setMatchingMode(MatchingMode.ANYWHERE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return example;
		}
	};
	
	public static final ExampleGenerator NO_ZEROES_GENERATOR = new ExampleGenerator() {
		@Override
		public Example generate(IdentifiableBySerial filter) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			Example example = new Example(filter);
			example.excludeZeroes();
			return example;
		}
	};
	
	public static final ExampleGenerator ANYWHERE_MATCHING_MODE_WITH_NO_ZEROES_GENERATOR = new ExampleGenerator() {
		@Override
		public Example generate(IdentifiableBySerial filter) {
			Example example = null;
			try {
				example = new Example(filter);
				example.setMatchingMode(MatchingMode.ANYWHERE);
				example.excludeZeroes();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return example;
		}
	};
	public abstract Example generate(IdentifiableBySerial filter) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;
}
