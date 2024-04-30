package com.alipay.sofa.koupleless.kouplelessidea.parser.util

import org.apache.commons.configuration2.ex.ConfigurationException
import javax.xml.transform.Transformer


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/10 19:54
 */
class TransformerXMLConfiguration:IgnoreDTDXMLConfiguration() {
    private val extraOutputProperty:MutableMap<String,String> = mutableMapOf()

    fun addOutputProperty(map: Map<String,String>){
        extraOutputProperty.putAll(map)
    }

    @Throws(ConfigurationException::class)
    override fun createTransformer(): Transformer? {
        val transformer = super.createTransformer()
        extraOutputProperty.forEach { (k, v) ->
            transformer.setOutputProperty(k,v)
        }
        return transformer
    }
}
