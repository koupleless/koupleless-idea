package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModifyContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext


class ModifyStageContext(parent: SplitModuleContext) {
    val parentContext = parent

    val modifyContext: ModifyContext = ModifyContext()

    val refactorContext: ModifyContext = ModifyContext()

    fun clear(){
        modifyContext.clear()
        refactorContext.clear()
    }
}
