/**
 * 
 */
package com.smartoa.service.datasource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定数据源
 * 
 * @author zl
 *
 */
@Target({ ElementType.METHOD })   
@Retention(RetentionPolicy.RUNTIME)   
@Documented 
public @interface WithDataSource {
	DatabaseType value();   
}
