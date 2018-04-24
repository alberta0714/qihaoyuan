package com.smartoa.service.utils.page;
import java.util.Properties;
import org.apache.ibatis.executor.statement.StatementHandler;
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
import org.apache.ibatis.session.Configuration;
/**
 * @Author hbb
 * @Date 2016/12/9 16:40
 */
@Intercepts({ @org.apache.ibatis.plugin.Signature(type = StatementHandler.class, method = "prepare", args = { java.sql.Connection.class }) })
public class PaginationInterceptor implements Interceptor {
    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory REFLECTORFACTORY=new DefaultReflectorFactory();

    public Object intercept(Invocation invocation)
            throws Throwable
    {
        StatementHandler statementHandler = (StatementHandler)invocation.getTarget();
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY,REFLECTORFACTORY);

        if ((SystemContext.getOffset() == 0) && (SystemContext.getPagesize() == 2147483647)) {
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

        Configuration configuration = (Configuration)metaStatementHandler.getValue("delegate.configuration");
        Dialect.Type databaseType = null;
        try
        {
            databaseType = Dialect.Type.valueOf(configuration.getVariables().getProperty("dialect").toUpperCase());
        }
        catch (Exception e) {
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

    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    public void setProperties(Properties properties) {
    }
}
