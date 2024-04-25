package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/7 16:50
 */
interface Pipeline {
    fun process(splitModuleContext: SplitModuleContext)
    fun getName():String
}
