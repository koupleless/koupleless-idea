package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/2 14:48
 */
object BeanInfoUtil {
    fun matchedByName(beanInfo: BeanInfo, name:String?):Boolean{
        name?:return false
        return beanInfo.beanName == name
    }

    fun matchedByType(beanInfo: BeanInfo, type:String):Boolean{
        return beanInfo.fullClassName == type || beanInfo.interfaceTypes.contains(type)
    }
}
