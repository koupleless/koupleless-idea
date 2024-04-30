package com.alipay.sofa.koupleless.kouplelessidea.model

/**
 *
 * @author lipeng
 * @version : BaseData, v 0.1 2024-04-24 17:08 lipeng Exp $
 */
class BaseData {
    var app:String = ""

    fun app(app: String?): BaseData {
        this.app = app!!
        return this
    }
}
