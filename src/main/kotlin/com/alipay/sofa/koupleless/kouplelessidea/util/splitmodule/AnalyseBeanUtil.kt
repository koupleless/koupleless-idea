package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/8 14:24
 */
object AnalyseBeanUtil {
    fun analyseBeanRefInXML(beanRef: BeanRef, beanContext: BeanContext, otherBeanContext: BeanContext?){
        val beanNameDefinedInXML = beanRef.beanNameDefinedInXML?:return
        beanNameDefinedInXML.forEach {name ->
            analyseBeanRefByName(name,beanRef,beanContext,otherBeanContext)
        }
    }

    private fun analyseBeanRefByName(name:String, beanRef: BeanRef, beanContext: BeanContext, otherBeanContext: BeanContext?):Boolean{
        if(beanContext.containsBeanName(name)){
            val beanRefInfo = beanContext.getBeanByName(name)!!
            beanRef.parsedAs(beanRefInfo)
            return true
        }

        otherBeanContext?:return false
        if(otherBeanContext.containsBeanName(name)){
            val targetBeanInfo = otherBeanContext.getBeanByName(name)!!
            beanRef.parsedAs(targetBeanInfo)
            beanContext.addMissedOutsideBean(beanRef,targetBeanInfo)
            return true
        }
        return false
    }

    fun analyseBeanRefInJava(beanRef: BeanRef, beanContext: BeanContext, otherBeanContext: BeanContext?) {
        when(beanRef.autowire){
            BeanRef.AutowiredMode.BY_TYPE -> {
                analyseBeanRefByType(beanRef,beanContext,otherBeanContext)
            }

            BeanRef.AutowiredMode.BY_NAME -> {
                analyseBeanRefByName(beanRef,beanContext,otherBeanContext)
            }

            BeanRef.AutowiredMode.TYPE_FIRST -> {
                analyseBeanRefByType(beanRef,beanContext,otherBeanContext)
                analyseBeanRefByName(beanRef,beanContext,otherBeanContext)
            }

            BeanRef.AutowiredMode.NAME_FIRST -> {
                analyseBeanRefByName(beanRef,beanContext,otherBeanContext)
                analyseBeanRefByType(beanRef,beanContext,otherBeanContext)
            }

            else -> {
                // TODO:log
            }
        }
    }

    private fun analyseBeanRefByName(beanRef: BeanRef, beanContext: BeanContext, otherBeanContext: BeanContext?):Boolean{
        val name = beanRef.beanNameToParse?:return false
        return analyseBeanRefByName(name,beanRef,beanContext,otherBeanContext)
    }

    private fun analyseBeanRefByType(beanRef: BeanRef, beanContext: BeanContext, otherBeanContext: BeanContext?):Boolean{
        val type = beanRef.beanTypeToParse?:return false
        if(beanContext.containsBeanType(type)){
            val beanRefInfo = beanContext.getBeanByType(type)!!
            beanRef.parsedAs(beanRefInfo)
            return true
        }

        otherBeanContext?:return false
        if(otherBeanContext.containsBeanType(type)){
            val targetBeanInfo = otherBeanContext.getBeanByType(type)!!
            beanRef.parsedAs(targetBeanInfo)
            beanContext.addMissedOutsideBean(beanRef,targetBeanInfo)
            return true
        }
        return false
    }
}
