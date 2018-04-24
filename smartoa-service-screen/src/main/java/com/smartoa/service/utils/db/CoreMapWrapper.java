package com.smartoa.service.utils.db;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import java.util.Map;

/**
 * <功能简述> 驼峰处理 返回Map类型时转换为Map中的key为驼峰式命名
 * 
 * @Title: MyMapWrapper.java
 * @author wb
 * @date 2017年6月26日下午9:54:40
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class CoreMapWrapper extends MapWrapper {

	/**
	 * 构造器 实际使用的是原始的转换器
	 * @Title:CoreMapWrapper
	 * @author: wb
	 * @date: 2017年6月26日 下午11:46:37
	 * @param metaObject
	 * @param map
	 */
	public CoreMapWrapper(MetaObject metaObject, Map<String, Object> map) {
		super(metaObject, map);
	}

	/**
	 * <功能简述>
	 * 重写此方法,替换Map中的Key值
	 * @Title: findProperty
	 * @author: wb
	 * @date: 2017年6月26日 下午11:47:33
	 * @param name
	 * @param useCamelCaseMapping
	 * @return
	 * @see org.apache.ibatis.reflection.wrapper.MapWrapper#findProperty(java.lang.String, boolean)
	 */
	@Override
	public String findProperty(String name, boolean useCamelCaseMapping) {
		if (useCamelCaseMapping && ((name.charAt(0) >= 'A' && name.charAt(0) <= 'Z') || name.indexOf("_") >= 0)) {
			return underlineToCamelhump(name);
		}
		return name;
	}

	/**
	 * 将下划线风格替换为驼峰风格
	 *
	 * @param inputString
	 * @return
	 */
	public String underlineToCamelhump(String inputString) {
		StringBuilder sb = new StringBuilder();

		boolean nextUpperCase = false;
		for (int i = 0; i < inputString.length(); i++) {
			char c = inputString.charAt(i);
			if (c == '_') {
				if (sb.length() > 0) {
					nextUpperCase = true;
				}
			} else {
				if (nextUpperCase) {
					sb.append(Character.toUpperCase(c));
					nextUpperCase = false;
				} else {
					sb.append(Character.toLowerCase(c));
				}
			}
		}
		return sb.toString();
	}
}