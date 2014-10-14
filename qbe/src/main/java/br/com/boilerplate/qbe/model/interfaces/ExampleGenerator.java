package br.com.boilerplate.qbe.model.interfaces;

import br.com.boilerplate.qbe.core.Example;
import br.com.boilerplate.qbe.model.enumerated.MatchingMode;
import br.com.boilerplate.qbe.model.interfaces.IdentifiableBySerial;

public abstract class ExampleGenerator {
	public static final ExampleGenerator DEFAULT_GENERATOR = new ExampleGenerator() {
		@Override
		public Example generate(IdentifiableBySerial filter) {
			return new Example(filter);
		}
	};
	
	public static final ExampleGenerator ANYWHERE_MATCHING_GENERATOR = new ExampleGenerator() {
		@Override
		public Example generate(IdentifiableBySerial filter) {
			Example example = new Example(filter);
			example.setMatchingMode(MatchingMode.ANYWHERE);
			return example;
		}
	};
	
	public static final ExampleGenerator NO_ZEROES_GENERATOR = new ExampleGenerator() {
		@Override
		public Example generate(IdentifiableBySerial filter) {
			Example example = new Example(filter);
			try {
				example.excludeZeroes();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return example;
		}
	};
	
	public static final ExampleGenerator ANYWHERE_MATCHING_MODE_WITH_NO_ZEROES_GENERATOR = new ExampleGenerator() {
		@Override
		public Example generate(IdentifiableBySerial filter) {
			Example example = new Example(filter);
			try {
				example.setMatchingMode(MatchingMode.ANYWHERE);
				example.excludeZeroes();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return example;
		}
	};
	public abstract Example generate(IdentifiableBySerial filter);
}
