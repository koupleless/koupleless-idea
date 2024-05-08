package com.mock.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.xxx.mapper", sqlSessionFactoryRef = "sqlSessionFactoryBeanForSingle")
public class MybatisConfiguration {

    private String location = "mock_location";

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBeanForSingle(@Qualifier(value = "singleDataSource") DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            sqlSessionFactoryBean.setMapperLocations(pathMatchingResourcePatternResolver.getResources(location));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        sqlSessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);
        sqlSessionFactoryBean.setTypeHandlersPackage(typeHandlersPackage);
        return sqlSessionFactoryBean;
    }
}
