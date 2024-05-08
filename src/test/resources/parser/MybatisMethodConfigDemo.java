/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.mock.config;

import javax.sql.DataSource;
import com.demo.builder.DataSourceBuilder;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.mybatis.spring.SqlSessionTemplate;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.xxx.mapper", sqlSessionFactoryRef = "mockSqlSessionFactory")
public class MybatisMethodConfigDemo {

    @Autowired
    private DataSource vehDataSource;

    @Bean(initMethod = "init")
    public DataSource vehDataSource() {
        DataSource zdalDataSource = DataSourceBuilder.create().build();
        return zdalDataSource;
    }

    @Bean("mockSqlSessionFactory")
    public MybatisSqlSessionFactoryBean setSqlSessionFactory() {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        // 设置数据源
        bean.setDataSource(vehDataSource);
        // 简化PO的引用
        bean.setTypeAliasesPackage("com.mock.entity");
        // 设置全局配置
        bean.setGlobalConfig(this.globalConfig());
        return bean;
    }

    @Bean("mockTransactionManager")
    public DataSourceTransactionManager sourceTransactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(vehDataSource);
        return dataSourceTransactionManager;
    }

    @Bean("mockTransactionTemplate")
    public TransactionTemplate defaultTransactionTemplate() {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(sourceTransactionManager());
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(MybatisSqlSessionFactoryBean sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
