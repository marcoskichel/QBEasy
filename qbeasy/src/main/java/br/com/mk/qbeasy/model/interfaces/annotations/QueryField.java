package br.com.mk.qbeasy.model.interfaces.annotations;

import br.com.mk.qbeasy.model.enumerated.MatchingMode;
import br.com.mk.qbeasy.model.enumerated.OperationType;
import br.com.mk.qbeasy.model.enumerated.QueryConjuction;

public @interface QueryField {
	
	MatchingMode matchingMode() default MatchingMode.ANYWHERE;
	OperationType operationType();
	QueryConjuction conjuction() default QueryConjuction.AND;
	
	String before() default "";
	String after() default "";
	
	boolean isNullValid() default false;
	boolean isEmptyValid() default false;
}
