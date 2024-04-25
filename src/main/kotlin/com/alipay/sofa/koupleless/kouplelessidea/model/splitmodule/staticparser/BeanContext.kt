package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import cn.hutool.core.collection.ConcurrentHashSet
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.CollectionUtil.addOrPutList
import com.alipay.sofa.koupleless.kouplelessidea.util.CollectionUtil.addOrPutSet
import java.util.concurrent.ConcurrentHashMap


/**
 * @description: Bean 上下文，包括该上下文中的 bean信息 和 SofaService信息
 * @author lipeng
 * @date 2023/9/8 15:04
 */
class BeanContext(parent: ProjectContext) {
    // 存在bean：有className，但无id的情况；存在bean，有id，无 className 的情况，所以此处保存classNameToBeanInfo
    val classNameToBeanInfo: ConcurrentHashMap<String, BeanInfo> = ConcurrentHashMap()
    val beanNameToBeanInfo: ConcurrentHashMap<String, MutableList<BeanInfo>> = ConcurrentHashMap()
    val interfaceToBeanInfo: ConcurrentHashMap<String, MutableList<BeanInfo>> = ConcurrentHashMap()
    /**
     * 该上下文中所有的beanInfo。如果不同的beanName对应同一个bean，这里只会存储一次bean
     */
    val allBeanInfo:ConcurrentHashSet<BeanInfo> = ConcurrentHashSet()
    /**
     * 本上下文中的 Bean 将调用其它外部上下文的Bean，但外部Bean没有发布成 SofaService，可以将外部Bean发布为 SofaService，并在本Bean中以 SofaReference 引用
     */
    val missedOutsideBean = mutableSetOf<BeanInfo>()
    /**
     * 本上下文中的 Bean 将调用其它外部上下文的Bean，但外部Bean没有发布成 SofaService，不会自动将外部Bean发布为 SofaService，需要告知用户调用缺失情况
     */
    val missedOutsideBeanToReport = mutableSetOf<BeanInfo>()

    /**
     * 缺失的外部 SofaService，需要告知用户
     */
    val missedOutsideSofaServiceToReport = mutableSetOf<BeanInfo>()

    val parentContext = parent
    private fun putClassNameToBeanInfo(className:String, info: BeanInfo){
        info.parentContext = this
        classNameToBeanInfo[className] = info
        allBeanInfo.add(info)
    }

    fun putBeanNameToBeanInfo(beanName:String,info: BeanInfo){
        info.parentContext = this
        addOrPutList(beanNameToBeanInfo,beanName,info)
        allBeanInfo.add(info)
    }

    fun addBeanInfo(info: BeanInfo){
        val beanName = info.beanName
        if(StrUtil.isNotEmpty(beanName)){
            putBeanNameToBeanInfo(beanName!!,info)
        }

        val className = info.fullClassName
        if(StrUtil.isNotEmpty(className)){
            putClassNameToBeanInfo(className!!,info)
        }

        associateBeanInfoWithInterfaces(info.interfaceTypes,info)

        info.parentContext = this
    }

    fun putInterfaceToBeanInfo(interfaceType: String,info: BeanInfo){
        info.parentContext = this
        addOrPutList(interfaceToBeanInfo,interfaceType,info)
//        interfaceToBeanInfo[interfaceType] = info
        allBeanInfo.add(info)
    }

    fun associateBeanInfoWithInterfaces(interfaceTypes:Set<String>,beanInfo: BeanInfo){
        interfaceTypes.forEach {
            putInterfaceToBeanInfo(it,beanInfo)
        }
    }

    fun containsClassName(className: String):Boolean{
        return classNameToBeanInfo.containsKey(className)
    }

    fun containsInterface(interfaceType: String):Boolean{
        return interfaceToBeanInfo.containsKey(interfaceType)
    }

    fun containsBeanType(type:String):Boolean{
        return containsClassName(type)||containsInterface(type)
    }

    fun containsBeanName(beanName: String):Boolean{
        return beanNameToBeanInfo.containsKey(beanName)
    }

    fun getBeanInfoByClassName(className:String): BeanInfo?{
        return classNameToBeanInfo[className]
    }

    fun getBeanByName(beanName:String,modularName: String?=null): BeanInfo?{
        return beanNameToBeanInfo[beanName]?.firstOrNull { it.getModularName() == modularName }
    }

    fun getBeanByType(type:String,modularName:String?=null):BeanInfo?{
        return getBeanInfoByClassName(type)?:getBeanInfoByInterface(type,modularName)?.firstOrNull()
    }

    fun getBeanInfoByInterface(interfaceType: String,modularName: String?=null):List<BeanInfo>?{
        return interfaceToBeanInfo[interfaceType]?.filter { it.getModularName() == modularName }?.toList()
    }

    fun addMissedOutsideBean(key: BeanRef, value: BeanInfo){
        missedOutsideBean.add(key.parentBean)
        key.parentBean.addMissedOutsideBean(key,value)
    }

    fun addMissedOutsideBeanToReport(key: BeanRef, value: BeanInfo){
        missedOutsideBeanToReport.add(key.parentBean)
        key.parentBean.addMissedOutsideBeanToReport(key,value)
    }

    fun getBeansWithClassName():MutableCollection<BeanInfo>{
        return classNameToBeanInfo.values
    }

    fun clear() {
        classNameToBeanInfo.clear()
        beanNameToBeanInfo.clear()
        interfaceToBeanInfo.clear()
        missedOutsideBean.clear()
        missedOutsideBeanToReport.clear()
        missedOutsideSofaServiceToReport.clear()
        allBeanInfo.clear()
    }
}
