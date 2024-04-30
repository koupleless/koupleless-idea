package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import com.alipay.sofa.koupleless.kouplelessidea.util.CollectionUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.BeanInfoUtil


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/4 21:38
 */
class DBContext {
    /**
     * mybatis配置
     */
    val mybatisConfigs = mutableListOf<MybatisConfig>()

    /**
     * mybatis sqlSessionFactory 配置, key 为 beanName
     */
    val sqlSessionFactories = mutableMapOf<String,MutableList<SqlSessionFactoryInfo>>()

    /**
     * mybatis 数据源, key 为 beanName
     */
    val mybatisDataSources = mutableMapOf<String,MutableList<MybatisDataSource>>()

    /**
     * mybatis 事务模板
     */
    val transactionTemplates = mutableListOf <TransactionTemplate>()

    /**
     * mybatis 数据源事务管理器, key 为 beanName
     */
    val platformTransactionManagers = mutableMapOf<String,MutableList<PlatformTransactionManager>>()

    /**
     * session 模板
     */
    val sqlSessionTemplates = mutableListOf<SqlSessionTemplate>()

    /**
     * mapper 接口类，key 为接口名
     */
    val mapperInterfaces = mutableMapOf<String,ClassInfo>()

    fun clear(){
        mybatisConfigs.clear()
        sqlSessionFactories.clear()
        mybatisDataSources.clear()
        transactionTemplates.clear()
        platformTransactionManagers.clear()
        sqlSessionTemplates.clear()
        mapperInterfaces.clear()
    }

    fun registerMapperInterface(mapper: ClassInfo){
        mapperInterfaces[mapper.fullName] = mapper
    }

    fun registerMybatisConfig(mybatisConfig:MybatisConfig){
        mybatisConfigs.add(mybatisConfig)
    }
    fun registerSqlSessionFactory(sqlSessionFactoryInfo:SqlSessionFactoryInfo){
        val beanName = sqlSessionFactoryInfo.beanInfo.beanName!!
        CollectionUtil.addOrPutList(sqlSessionFactories,beanName,sqlSessionFactoryInfo)
    }

    fun registerDataSource(dataSource:MybatisDataSource){
        val beanName = dataSource.beanInfo.beanName!!
        CollectionUtil.addOrPutList(mybatisDataSources,beanName,dataSource)
    }

    fun registerPlatformTransactionManager(transactionManager:PlatformTransactionManager){
        val beanName = transactionManager.beanInfo.beanName!!
        CollectionUtil.addOrPutList(platformTransactionManagers,beanName,transactionManager)
    }

    fun registerTransactionTemplate(transactionTemplate:TransactionTemplate){
        transactionTemplate.transactionManagerBeanRef?.let {
            transactionTemplates.add(transactionTemplate)
        }
    }

    fun registerSqlSessionTemplate(sqlSessionTemplate:SqlSessionTemplate){
        sqlSessionTemplate.sqlSessionFactoryBeanRef.let {
            sqlSessionTemplates.add(sqlSessionTemplate)
        }
    }

    fun getSqlSessionFactoryInfoByRef(ref:String,modularName:String?=null):SqlSessionFactoryInfo?{
        return sqlSessionFactories[ref]?.firstOrNull { it.beanInfo.getModularName() == modularName }
    }

    fun getDataSourceByRef(ref:String?,modularName:String?=null):MybatisDataSource?{
        ref?:return null
        return mybatisDataSources[ref]?.firstOrNull { it.beanInfo.getModularName()==modularName }
    }

    /**
     * 不论 autowiredType 是什么，都按照以下规则获取数据源：
     * 1. 以 beanName 获取，如没找到，则执行 2
     * 2. 以 beanType 获取
     */
    fun getDataSourceByRef(ref:BeanRef?,modularName:String?=null):MybatisDataSource?{
        ref?:return null
        val byBeanName = getDataSourceByName(ref.beanNameToParse,modularName)
        if(byBeanName!=null){
            return byBeanName
        }
        return getDataSourceByType(ref.beanTypeToParse,modularName)
    }

    fun getDataSourceByName(beanName:String?, modularName:String?=null):MybatisDataSource?{
        beanName?:return null
        return mybatisDataSources[beanName]?.firstOrNull { it.beanInfo.getModularName()==modularName }
    }

    fun getDataSourceByType(type:String?, modularName:String?=null):MybatisDataSource?{
        mybatisDataSources.forEach {(_,dbSourceList)->
            dbSourceList.forEach {
                // xml node 的 beanRef 没有 type
                if(null==type && it.beanInfo.getModularName() == modularName){
                    return it
                }

                // java 文件中定义的 beanRef 按照 type 查找
                if((it.beanInfo.fullClassName == type || it.beanInfo.interfaceTypes.contains(type)) && it.beanInfo.getModularName() == modularName){
                    return it
                }
            }
        }
        return null
    }

    /**
     * 不论 autowiredType 是什么，都按照以下规则获取数据源：
     * 1. 以 beanName 获取，如没找到，则执行 2
     * 2. 以 beanType 获取
     */
    fun getTransactionManagersByDataSourceBeanInfo(dataSourceBeanInfo: BeanInfo?, modularName:String?=null):List<PlatformTransactionManager>{
        dataSourceBeanInfo?:return emptyList()

        return platformTransactionManagers.flatMap {
            it.value.asIterable()
        }.filter {
            val refIsNull = it.dataSourceBeanRef==null
            val inDifferentModular = it.beanInfo.getModularName() != modularName
            if(refIsNull || inDifferentModular){
                return@filter false
            }

            val matchedByName = BeanInfoUtil.matchedByName(dataSourceBeanInfo,it.dataSourceBeanRef!!.beanNameToParse)
            val matchedByType = it.dataSourceBeanRef.beanTypeToParse!=null && BeanInfoUtil.matchedByType(dataSourceBeanInfo,it.dataSourceBeanRef.beanTypeToParse!!)
            val matchedByNoType = it.dataSourceBeanRef.beanTypeToParse==null
            (matchedByName || matchedByType|| matchedByNoType)
        }.toList()
    }

    fun getSqlSessionTemplatesBySqlSessionFactoryRef(sqlSessionFactoryBeanInfo: BeanInfo?, modularName:String? = null):List<SqlSessionTemplate>{
        sqlSessionFactoryBeanInfo?:return emptyList()
        return sqlSessionTemplates.filter {
            val refIsNull = it.sqlSessionFactoryBeanRef==null
            val inDifferentModular = it.beanInfo.getModularName() != modularName
            if(refIsNull || inDifferentModular){
                return@filter false
            }

            val matchedByName = BeanInfoUtil.matchedByName(sqlSessionFactoryBeanInfo,it.sqlSessionFactoryBeanRef!!.beanNameToParse)
            val matchedByType = it.sqlSessionFactoryBeanRef.beanTypeToParse!=null && BeanInfoUtil.matchedByType(sqlSessionFactoryBeanInfo,it.sqlSessionFactoryBeanRef.beanTypeToParse!!)
            val matchedByNoType = it.sqlSessionFactoryBeanRef.beanTypeToParse==null
            (matchedByName || matchedByType|| matchedByNoType)
        }.toList()
    }


    fun getTransactionTemplatesByTransactionManagerRef(transactionManagerBeanInfoList: List<BeanInfo>, modularName:String?=null):List<TransactionTemplate>{
        val res = mutableSetOf<TransactionTemplate>()
        transactionManagerBeanInfoList.forEach { transactionManagerRef->
            val templates = transactionTemplates.filter {
                val refIsNull = it.transactionManagerBeanRef==null
                val inDifferentModular = it.beanInfo.getModularName() != modularName
                if(refIsNull || inDifferentModular){
                    return@filter false
                }
                val matchedByName = BeanInfoUtil.matchedByName(transactionManagerRef,it.transactionManagerBeanRef!!.beanNameToParse)
                val matchedByType = it.transactionManagerBeanRef.beanTypeToParse!=null && BeanInfoUtil.matchedByType(transactionManagerRef,it.transactionManagerBeanRef.beanTypeToParse!!)
                val matchedByNoType = it.transactionManagerBeanRef.beanTypeToParse==null
                (matchedByName || matchedByType|| matchedByNoType)
            }.toList()
            res.addAll(templates)
        }

        return res.toList()
    }

    data class MybatisConfig(val beanInfo:BeanInfo, val basePackages:Set<String> = emptySet(), val sqlSessionFactoryRef:String?, val sqlSessionTemplateRef:String?=null){
        fun matchBasePackage(fullName:String):Boolean{
            basePackages.forEach {basePackage ->
                // 如果 basePackage 有通配符
                if(basePackage.contains("**")){
                    val regexStr = basePackage.replace(".**.",".(\\w+.)*")
                        .replace(".**","(.\\w+)*")
                        .replace(".","\\.")

                    if(fullName.matches(regexStr.toRegex())){
                        return true
                    }
                }else{ // 如果 basePackage 没有通配符
                    if(fullName.startsWith(basePackage)){
                        return true
                    }
                }
            }
            return false
        }

    }

    data class SqlSessionFactoryInfo(val beanInfo:BeanInfo, val dataSourceBeanRef:BeanRef?, val mapperLocationExists:Boolean, val configLocation:String?=null, val pluginBeanNames:List<String> = emptyList())

    data class MybatisDataSource(val beanInfo:BeanInfo)

    data class PlatformTransactionManager(val beanInfo:BeanInfo,val dataSourceBeanRef:BeanRef?)

    data class TransactionTemplate(val beanInfo: BeanInfo,val transactionManagerBeanRef:BeanRef?)

    data class SqlSessionTemplate(val beanInfo:BeanInfo,val sqlSessionFactoryBeanRef:BeanRef?)
}
