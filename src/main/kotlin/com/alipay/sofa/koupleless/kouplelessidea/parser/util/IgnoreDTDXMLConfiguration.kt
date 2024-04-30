package com.alipay.sofa.koupleless.kouplelessidea.parser.util

import org.apache.commons.configuration2.XMLConfiguration
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/20 20:30
 */
open class IgnoreDTDXMLConfiguration: XMLConfiguration(){
    init {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        builder.setEntityResolver(IgnoreDTDEntityResolver)
        documentBuilder = builder
    }

    object IgnoreDTDEntityResolver: EntityResolver {
        override fun resolveEntity(publicId: String?, systemId: String?): InputSource {
            return InputSource(ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".toByteArray()))
        }

    }
}
