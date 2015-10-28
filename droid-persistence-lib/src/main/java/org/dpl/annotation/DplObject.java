package org.dpl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DplObject {
	/**
	 * Used for inform if the object must be saved.
	 * 
	 * If true: When fillContentValue the internal object will be saved for get the id to relation with your object.
	 * If false: The internal object will not be saved, but id will be get to relation with your object.
	 * @return Flag if the object must be saved.
	 */
	boolean save() default true;
}
