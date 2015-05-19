package br.com.mk.qbeasy.model.interfaces.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.persistence.AttributeConverter;

@Target(FIELD) 
@Retention(RUNTIME)
public @interface WithAttributeConverter {
	
	@SuppressWarnings("rawtypes")
	Class<? extends AttributeConverter> value();

}
