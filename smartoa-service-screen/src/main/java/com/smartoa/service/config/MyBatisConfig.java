package com.smartoa.service.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.pagehelper.PageHelper;
import com.smartoa.service.datasource.DatabaseType;
import com.smartoa.service.datasource.DynamicDataSource;
import com.smartoa.service.utils.db.MapWrapperFactory;

/**
 * MyBatis基础配置
 *
 */
@Configuration
@EnableTransactionManagement
public class MyBatisConfig {

    @Bean()
    @ConfigurationProperties(prefix = "spring.primary.datasource")
    public DataSource smartoaDataSource() {
    	return DataSourceBuilder.create().build();
    }

    @Bean()
    @Qualifier("datacenterDataSource")
    @ConfigurationProperties(prefix = "spring.secondary.datasource")
    public DataSource datacenterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DynamicDataSource dynmicDataSource(@Qualifier("smartoaDataSource") DataSource smartoaDataSource,
            @Qualifier("datacenterDataSource") DataSource datacenterDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DatabaseType.smartoa, smartoaDataSource);
        targetDataSources.put(DatabaseType.datacenter, datacenterDataSource);

        DynamicDataSource dataSource = new DynamicDataSource();
        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(smartoaDataSource);

        return dataSource;
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(DynamicDataSource ds) {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(ds);
        bean.setTypeAliasesPackage("com.smartoa.service.model");

        //分页插件
        Properties properties = new Properties();
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "check");
        properties.setProperty("params", "count=countSql");
        PageHelper pageHelper = new PageHelper();
        pageHelper.setProperties(properties);

        bean.setPlugins(new Interceptor[]{pageHelper});
       
        try {
            bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            /*返回Map时值为空也要把键值对放入到Map中*/
            configuration.setCallSettersOnNulls(true);
            /*使用列别名替换列名 select user as User*/
            configuration.setUseColumnLabel(true);
            /*当数据库查询出数据返回Map时自动转换KEY为驼峰式命名*/
            configuration.setMapUnderscoreToCamelCase(true);
            bean.setConfiguration(configuration);
            /*设置返回map是驼峰式转换*/
            bean.setObjectWrapperFactory(new MapWrapperFactory());
            
            return bean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Bean  
    public PlatformTransactionManager transactionManager(DataSource dataSource) {  
        return new DataSourceTransactionManager(dataSource);  
    }  
}
