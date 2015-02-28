package br.com.mk.qbeasy.model.interfaces;

import java.io.Serializable;

import br.com.mk.qbeasy.core.Example;
import br.com.mk.qbeasy.model.enumerated.MatchingMode;
import br.com.mk.qbeasy.model.exception.QBEasyException;

public interface ExampleGenerator extends Serializable {
	
	Example generate(Object filter) throws QBEasyException;
	
	/**
	 * Generates examples ignoring empty Strings, with ignore case triggered and using 
	 * MatchMode.Anywhere
	 */
	public static final ExampleGenerator DEFAULT_GENERATOR = new ExampleGenerator() {
		
		private static final long serialVersionUID = 6527466141299869551L;

		public Example generate(Object filter) throws QBEasyException {
			try {
				Example e = new Example(filter);
				e.setPrintHql(true);
				e.ignoreCase(true);
				e.setMatchingMode(MatchingMode.ANYWHERE);
				e.excludeZeroes();
				return e;
			} catch (Exception e) {
				throw new QBEasyException(e);
			}
		}
	};
	
	public static final ExampleGenerator ANYWHERE_MATCHING_GENERATOR = new ExampleGenerator() {
	
		private static final long serialVersionUID = -1622181857945846465L;

		public Example generate(Object filter) {
			try {
				Example example = new Example(filter);
				example.setMatchingMode(MatchingMode.ANYWHERE);
				return example;
			} catch (Exception e) {
				throw new QBEasyException(e);
			}
		}
	};
	
	public static final ExampleGenerator NO_ZEROES_GENERATOR = new ExampleGenerator() {
		
		private static final long serialVersionUID = 7734803153797469595L;

		public Example generate(Object filter) throws QBEasyException {
			try {
				Example example = new Example(filter);
				example.excludeZeroes();
				return example;
			} catch (Exception e) {
				throw new QBEasyException(e);
			}
		}
	};
	
	public static final ExampleGenerator ANYWHERE_MATCHING_MODE_WITH_NO_ZEROES_GENERATOR = new ExampleGenerator() {
		
		private static final long serialVersionUID = 1319744303677846376L;

		public Example generate(Object filter) {
			try {
				Example example = new Example(filter);
				example.setMatchingMode(MatchingMode.ANYWHERE);
				example.excludeZeroes();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	};
}
