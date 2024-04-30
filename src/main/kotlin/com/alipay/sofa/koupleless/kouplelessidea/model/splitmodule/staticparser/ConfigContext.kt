package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/11 16:14
 */
class ConfigContext(parent: ProjectContext) {
    val dbContext: DBContext  = DBContext()

    val parentContext = parent

    fun clear(){
        dbContext.clear()
    }
}
