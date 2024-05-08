package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.IgnoreDTDXMLConfiguration
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine
import org.apache.commons.io.FileUtils
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 10:51
 */
class BeanVisitorTest {

    @Test
    fun testDoParse(){
        val configs = Configurations()
        val config = configs.fileBased(IgnoreDTDXMLConfiguration().javaClass,getBeanXMLFile())
        config.expressionEngine = XPathExpressionEngine()

        val projContext = MockKUtil.mockProjectContext()
        BeanVisitor.doParse("mockFile",config,projContext)

        assertEquals(1,projContext.xmlContext.getBeanNodes().size)

        val beanNode = projContext.xmlContext.getBeanNodes().first()
        // 验证 beanNode
        assertEquals("myClass",beanNode.beanName)
        assertEquals("com.mock.MyClass",beanNode.fullClassName)
        assertEquals("byName",beanNode.autowired)

        // 验证 beanNode 中的 beanInfo
        val beanInfo = beanNode.beanInfo
        assertEquals("com.mock.MyClass",beanInfo.fullClassName)
        assertEquals("myClass",beanInfo.beanName)
        assertTrue(beanInfo.definedByXML)
        assertEquals(BeanRef.AutowiredMode.BY_NAME,beanInfo.beanXmlAutowiredMode)
        assertNotNull(beanInfo.getXMLNode(SplitConstants.BEAN_XML_NODE))

        // 验证 beanInfo 中的 beanRef
        val beanDependOn = beanInfo.beanDependOn
        assertEquals(5,beanDependOn.size)
        val beanNameRefs = beanDependOn.values.flatMap { it.beanNameDefinedInXML!! }
        assertEquals(8,beanNameRefs.size)

        // 验证 beanNode 中的 xmlNode
        val xmlNode = beanInfo.getXMLNode(SplitConstants.BEAN_XML_NODE)
        assertNotNull(xmlNode)
        assertEquals("mockFile",xmlNode.filePath)
        assertNotNull(xmlNode.node)
        assertEquals("//bean[@class='com.mock.MyClass']",xmlNode.nodeXPath)
    }

    @Test
    fun testDoNotParse(){
        val configs = Configurations()
        val config = configs.fileBased(IgnoreDTDXMLConfiguration().javaClass,getEmptyXMLFile())
        config.expressionEngine = XPathExpressionEngine()

        val projContext = MockKUtil.mockProjectContext()
        BeanVisitor.doParse("mockFile",config,projContext)

        assertEquals(0,projContext.xmlContext.getBeanNodes().size)
    }


    private fun getBeanXMLFile(): File {
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
                    <property name="fieldA" ref="mockA"/>
                    
                    <property name="fieldB">
                        <map>
                            <entry key="key1" value-ref="mockKeyBA"/>
                            <entry key="key2" value-ref="mockKeyBB"/>
                        </map>
                    </property>
                    
                    <property name="fieldC">
                        <ref bean="mockC"/>
                    </property>
                    
                    <property name="fieldD">
                        <map>
                            <entry key="keyD1">
                                <list>
                                    <ref bean="mockDA"/>
                                </list>
                            </entry>
                            <entry key="keyD2">
                                <list>
                                    <ref bean="mockDB"/>
                                </list>
                            </entry>
                        </map>
                    </property>
                    
                    <property name="fieldE">
                        <map>
                            <entry key-ref="mockE" value-ref="mockKeyEA"/>
                        </map>
                    </property>
                </bean>
            </beans>
        """.trimIndent().toByteArray())

        inputStream.use {
            FileUtils.copyInputStreamToFile(inputStream, file)
        }

        return file
    }

    private fun getEmptyXMLFile(): File {
        val file = File("mockFile")
        val inputStream = ByteArrayInputStream("""
            <?xml version="1.0" encoding="UTF-8"?>

            <beans xmlns="http://www.springframework.org/schema/beans"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:sofa="http://schema.alipay.com/sofa/schema/service" xmlns:context="http://www.springframework.org/schema/context"
                   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                                http://schema.alipay.com/sofa/schema/service http://schema.alipay.com/sofa/sofa-service-4-0-0.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd"
                   default-autowire="byName">
            </beans>
        """.trimIndent().toByteArray())

        inputStream.use {
            FileUtils.copyInputStreamToFile(inputStream, file)
        }

        return file
    }

}
