package com.alipay.sofa.koupleless.kouplelessidea.parser.util

import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml.XmlVisitor
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.builder.fluent.Parameters
import org.apache.commons.configuration2.ex.ConfigurationException
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


/**
 * @description: Xml工具
 * @author lipeng
 * @date 2023/8/24 10:54
 */
object XmlUtil {
    private fun createDefaultXmlDocumentBuilder(): DocumentBuilder {
        // DocumentBuilderFactory 和 DocumentBuilder 非线程安全，需要每个线程都有自己的
        val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val ignoreDTDEntityResolver = IgnoreDTDXMLConfiguration.IgnoreDTDEntityResolver
        val builder = factory.newDocumentBuilder()
        builder.setEntityResolver(ignoreDTDEntityResolver)
        return builder
    }

    fun <A> parseDefaultXml(xmls:List<File>, visitorList:List<XmlVisitor<A>>, arg:A?){
        xmls.forEach {
            val configs = Configurations()

            try {
                val config = configs.fileBased(IgnoreDTDXMLConfiguration().javaClass,it)
                config.expressionEngine = XPathExpressionEngine()

                visitorList.forEach {visitor ->
                    visitor.parse(it.absolutePath.toString(),config,arg)
                }
            } catch (e : ConfigurationException) {
                // "不支持的解析类型"
            }
        }
    }

    fun <A> parseAndSave(file:File, visitorList:List<XmlVisitor<A>>, arg:A?, extraOutputProperty:Map<String,String> = emptyMap()){
        val encoding = parseXMLEncoding(file)

        val xmlParams = Parameters().xml().setEncoding(encoding).setFile(file)
        val config = FileBasedConfigurationBuilder(TransformerXMLConfiguration().javaClass).configure(xmlParams).configuration
        config.expressionEngine = XPathExpressionEngine()
        config.addOutputProperty(extraOutputProperty)

        visitorList.forEach {visitor ->
            visitor.parse(file.absolutePath.toString(),config,arg)
        }

        val fileWriter = FileWriter(file, Charset.forName(encoding),false)
        config.write(fileWriter)
    }

    fun save(file:File,extraOutputProperty:Map<String,String>){
        parseAndSave(file, emptyList(),null,extraOutputProperty)
    }

    fun parseXMLEncoding(file:File):String{
        val builder = createDefaultXmlDocumentBuilder()
        return file.inputStream().use {
            val document = builder.parse(it)
            document.xmlEncoding?:Charset.defaultCharset().name()
        }
    }
}
