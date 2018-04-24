package com.smartoa.service.utils.db;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

import java.util.Map;

/**
 * <功能简述>
 *  Map 类型结果转驼峰
 * @Title: MapWrapperFactory.java
 * @author  wb
 * @date 2017年6月26日下午9:56:39
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapWrapperFactory implements ObjectWrapperFactory {

    @Override
    public boolean hasWrapperFor(Object object) {
        return object != null && object instanceof Map;
    }
    
	@Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        return new CoreMapWrapper(metaObject, (Map) object);
    }

}