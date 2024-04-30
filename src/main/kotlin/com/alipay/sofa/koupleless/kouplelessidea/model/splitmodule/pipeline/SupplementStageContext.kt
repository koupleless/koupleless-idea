package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModifyContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext


class SupplementStageContext(parent: SplitModuleContext) {
    val parentContext = parent

    val modifyContext: ModifyContext = ModifyContext()

    private val configs = mutableMapOf<String,Any>()

    fun clear(){
        modifyContext.clear()
        configs.clear()
    }

    fun updateConfig(map:Map<String, Any>){
        configs.putAll(map)
    }

    fun getConfig(key:String):Any?{
        return configs[key]
    }

    fun setConfig(key: String,value:Any){
        configs[key] = value
    }
}
