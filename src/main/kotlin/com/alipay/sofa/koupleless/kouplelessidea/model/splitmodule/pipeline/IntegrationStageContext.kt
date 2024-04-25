package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModifyContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext



class IntegrationStageContext(parent: SplitModuleContext) {
    val parentContext = parent

    val integrateContext = ModifyContext()

    private val configs = mutableMapOf<String,Any>()

    fun clear(){
        integrateContext.clear()
        configs.clear()
    }

    fun updateConfigs(map:Map<String, Any>){
        configs.putAll(map)
    }

    fun setConfig(key: String, value: Any){
        configs[key] = value
    }

    fun getConfig(key:String):Any?{
        return configs[key]
    }
}
