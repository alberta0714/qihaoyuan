package com.smartoa.service.utils.page;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

/**
 * @Author hbb
 * @Date 2016/12/9 16:30
 */
@Intercepts({ @org.apache.ibatis.plugin.Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class,Integer.class }) })
public class PageInterceptor  implements Interceptor {
    private   static final  ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static   final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory REFLECTORFACTORY=new DefaultReflectorFactory();
    @SuppressWarnings({ "rawtypes", "unused" })
	public Object intercept(Invocation invocation)
            throws Throwable
    {
        try
        {
            StatementHandler statementHandler = (StatementHandler)invocation.getTarget();
            //MetaObject metaStatementHandler=SystemMetaObject.forObject(statementHandler);
            MetaObject metaStatementHandler = MetaObject.forObject(statementHandler,DEFAULT_OBJECT_FACTORY,DEFAULT_OBJECT_WRAPPER_FACTORY,REFLECTORFACTORY);

            StatementHandler originalStatementHandler = (StatementHandler)metaStatementHandler.getOriginalObject();
            ParameterHandler originalParameterHandler = originalStatementHandler.getParameterHandler();
            Object parameterHandler = originalParameterHandler.getParameterObject();
            Configuration configuration = (Configuration)metaStatementHandler.getValue("delegate.configuration");
            MappedStatement mappedStatement = (MappedStatement)metaStatementHandler.getValue("delegate.mappedStatement");

            String pageSqlId = configuration.getVariables().getProperty("pagesqlid");
            if ((pageSqlId != null) && (pageSqlId != ""))
            {
                String id = mappedStatement.getId();
                String mappedStatementId = id.substring(id.lastIndexOf(".") + 1);
                if (!(mappedStatementId.matches(pageSqlId))) {
                    Object localObject1 = invocation.proceed();
                    return invocation.proceed();
                }
            }
            Connection connection = (Connection)invocation.getArgs()[0];
            int total = getTotalRecord(parameterHandler, mappedStatement, connection);
            Object hashMapparameterHandler;
            if (parameterHandler instanceof HashMap)
            {
                hashMapparameterHandler = (HashMap)parameterHandler;
                Iterator iter = ((Map)hashMapparameterHandler).entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    if (entry.getValue() instanceof PageParam)
                    {
                        PageParam parameter = (PageParam)entry.getValue();
                        SystemContext.setOffset(parameter.getStart().intValue());
                        SystemContext.setPagesize(parameter.getLimit().intValue());
                        parameter.setTotal(Integer.valueOf(total));
                        break;
                    }
                }
            }
            if ((SystemContext.getOffset() == 0) && (SystemContext.getPagesize() == 2147483647)) {
                hashMapparameterHandler = invocation.proceed();
                return invocation.proceed();
            }
            while (metaStatementHandler.hasGetter("h")) {
                Object object = metaStatementHandler.getValue("h");
                metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY,REFLECTORFACTORY);
            }

            while (metaStatementHandler.hasGetter("target")) {
                Object object = metaStatementHandler.getValue("target");
                metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY,REFLECTORFACTORY);
            }
            Dialect.Type databaseType = null;
            try {
                databaseType = Dialect.Type.valueOf(configuration.getVariables().getProperty("dialect").toUpperCase());
                if (databaseType == null)
                    databaseType = Dialect.Type.MYSQL;
            }
            catch (Exception e) {
                throw new RuntimeException("the value of the dialect property in configuration.xml is not defined " + e.getMessage());
            }

            if (databaseType == null) {
                throw new RuntimeException("the value of the dialect property in configuration.xml is not defined : " + configuration.getVariables().getProperty("dialect"));
            }

            Dialect dialect = null;
            switch (databaseType.ordinal())
            {
                case 1:
                    dialect = new MySqlDialect();
                    break;
                case 2:
                    dialect = new OracleDialect();
                    break;
                default:
                    dialect = new OracleDialect();
            }

            String originalSql = (String)metaStatementHandler.getValue("delegate.boundSql.sql");

            metaStatementHandler.setValue("delegate.boundSql.sql", dialect.getLimitString(originalSql, SystemContext.getOffset(), SystemContext.getPagesize()));

            metaStatementHandler.setValue("delegate.rowBounds.offset", Integer.valueOf(0));
            metaStatementHandler.setValue("delegate.rowBounds.limit", Integer.valueOf(2147483647));

            return invocation.proceed();
        }
        catch (Exception ex)
        {
            throw ex;
        } finally {
        }
    }

    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    public void setProperties(Properties properties) {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private int getTotalRecord(Object page, MappedStatement mappedStatement,
                               Connection connection) {
        int total = 0;

        BoundSql boundSql = mappedStatement.getBoundSql(page);

        String sql = boundSql.getSql();

        String countSql = getCountSql(sql);

        List parameterMappings = boundSql.getParameterMappings();

        BoundSql countBoundSql = new BoundSql(
                mappedStatement.getConfiguration(), countSql,
                parameterMappings, page);

        ParameterHandler parameterHandler = new DefaultParameterHandler(
                mappedStatement, page, countBoundSql);

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(countSql);

            parameterHandler.setParameters(pstmt);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }

            return total;
        } catch (SQLException e) {
            e.printStackTrace();

            return total;
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCountSql(String sql) {
        int index = sql.toLowerCase().indexOf("from");
        return "select count(*) " + sql.substring(index);
    }
}
