package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/24 11:55
 */
class AnalyseConfig {
    private val customBeanAnnotation = mutableSetOf<String>()
    private val customDataSourceClass = mutableSetOf<String>()

    fun addCustomBeanAnnotations(annotations: Set<String>) {
        customBeanAnnotation.addAll(annotations)
    }

    fun getCustomBeanAnnotations(): Set<String> {
        return customBeanAnnotation
    }

    fun addCustomDataSourceClasses(classes: Set<String>) {
        customDataSourceClass.addAll(classes)
    }

    fun getCustomDataSourceClasses(): Set<String> {
        return customDataSourceClass
    }

    fun clear(){
        customBeanAnnotation.clear()
        customDataSourceClass.clear()
    }

    fun clone(config:AnalyseConfig){
        clear()

        customBeanAnnotation.addAll(config.customBeanAnnotation)
        customDataSourceClass.addAll(config.customDataSourceClass)
    }
}
