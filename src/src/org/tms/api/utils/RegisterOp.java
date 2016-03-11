package org.tms.api.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterOp 
{
	public String token() default "";
	public boolean exclude() default false;
	public boolean async() default false;
	public String[] categories() default {};
}
