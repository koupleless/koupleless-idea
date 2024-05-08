package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml

import com.alipay.sofa.koupleless.kouplelessidea.parser.util.IgnoreDTDXMLConfiguration
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine
import org.apache.commons.io.FileUtils
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 14:40
 */
class ReferByXmlVisitorTest {

    @Test
    fun testDoParse(){
        val configs = Configurations()
        val config = configs.fileBased(IgnoreDTDXMLConfiguration().javaClass,getXMLFile())
        config.expressionEngine = XPathExpressionEngine()

        val projContext = MockKUtil.mockProjectContext()
        val classInfo = MockKUtil.spyClassInfo("com.mock.MyClass","mockMyClassPath")
        val interfaceInfo = MockKUtil.spyClassInfo("com.mock.MyInterface","mockMyInterfacePath")
        projContext.classInfoContext.addClassInfo(classInfo)
        projContext.classInfoContext.addClassInfo(interfaceInfo)

        ReferByXmlVisitor.doParse("mockFilePath",config,projContext)
        assertEquals(1,classInfo.referByXML.size)
        assertEquals(2,interfaceInfo.referByXML.size)

        val referByBeanXML = classInfo.referByXML[0]
        assertEquals("//bean[@class='com.mock.MyClass']",referByBeanXML.nodeXPath)
        assertEquals("class",referByBeanXML.propertyName)
        assertEquals("mockFilePath",referByBeanXML.filePath)

        val referBySofaServiceXML = interfaceInfo.referByXML[0]
        assertEquals("//sofa:service[@interface='com.mock.MyInterface']",referBySofaServiceXML.nodeXPath)
        assertEquals("interface",referBySofaServiceXML.propertyName)
        assertEquals("mockFilePath",referBySofaServiceXML.filePath)

        val referBySofaReferenceXML = interfaceInfo.referByXML[1]
        assertEquals("//sofa:reference[@interface='com.mock.MyInterface']",referBySofaReferenceXML.nodeXPath)
        assertEquals("interface",referBySofaReferenceXML.propertyName)
        assertEquals("mockFilePath",referBySofaReferenceXML.filePath)
    }

    private fun getXMLFile(): File {
        val file = File("mockFile")
        val inputStream = ByteArrayInputStream("""
            <?xml version="1.0" encoding="UTF-8"?>

            <beans xmlns="http://www.springframework.org/schema/beans"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:sofa="http://schema.alipay.com/sofa/schema/service" xmlns:context="http://www.springframework.org/schema/context"
                   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                                http://schema.alipay.com/sofa/schema/service http://schema.alipay.com/sofa/sofa-service-4-0-0.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd"
                   default-autowire="byName">
                   
                <bean id="myClass" class="com.mock.MyClass">
                </bean>
                
                <sofa:service ref="myClass" interface="com.mock.MyInterface"></sofa:service>
                
                <sofa:reference id="myNewClass" interface="com.mock.MyInterface"/>
                
            </beans>
        """.trimIndent().toByteArray())

        inputStream.use {
            FileUtils.copyInputStreamToFile(inputStream, file)
        }

        return file
    }
}
