/**
 * 
 */
package com.smartoa.service.datasource;

import org.apache.logging.log4j.core.config.Order;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author zl
 *
 */
@Aspect
@Order(-1) //spring order排序后执行顺序是从小到大，目的是确保在事务管理器执行前先执行
@Component
public class DynamicDataSourceAspect {
	@Before("@annotation(withDataSource)")
	public void setDataSourceType(JoinPoint point, WithDataSource withDataSource) throws Throwable {
		DatabaseContextHolder.setDatabaseType(withDataSource.value());
	}
	
	@After("@annotation(withDataSource)")
    public void restoreDataSource(JoinPoint point, WithDataSource withDataSource) {
		DatabaseContextHolder.clearDataSourceType();
    }

}
