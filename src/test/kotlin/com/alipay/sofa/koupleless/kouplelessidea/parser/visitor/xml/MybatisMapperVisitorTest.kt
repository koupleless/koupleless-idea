package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.IgnoreDTDXMLConfiguration
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine
import org.apache.commons.io.FileUtils
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.test.*


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 12:01
 */
class MybatisMapperVisitorTest {

    @Test
    fun testDoParse(){
        val configs = Configurations()
        val config = configs.fileBased(IgnoreDTDXMLConfiguration().javaClass,getXMLFile())
        config.expressionEngine = XPathExpressionEngine()

        val projContext = MockKUtil.mockProjectContext()
        MybatisMapperVisitor.doParse("mockFilePath",config,projContext)

        assertEquals(1,projContext.xmlContext.getMapperXMLs().size)

        val mapperXML = projContext.xmlContext.getMapperXMLs()["com.mock.MockMapper"]
        assertNotNull(mapperXML)
        // 验证：MapperXML(val interfaceType: String, val filePath: String,val beanInfo: BeanInfo)
        assertEquals("com.mock.MockMapper",mapperXML.interfaceType)
        assertEquals("mockFilePath",mapperXML.filePath)

        // 验证 BeanInfo
        val beanInfo = mapperXML.beanInfo
        assertEquals("mockMapper",beanInfo.beanName)
        assertNull(beanInfo.fullClassName)
        assertEquals(1,beanInfo.interfaceTypes.size)
        assertTrue(beanInfo.interfaceTypes.contains("com.mock.MockMapper"))
        assertEquals(BeanInfo.DefineObjectMode.ByXMLFile,beanInfo.defineObjectMode)
        assertEquals("mockFilePath",beanInfo.filePath)
        assertEquals(true,beanInfo.getAttribute(SplitConstants.MAPPER_BEAN))
    }

    @Test
    fun testDoNotParse(){
        val configs = Configurations()
        val config = configs.fileBased(IgnoreDTDXMLConfiguration().javaClass,getEmptyXMLFile())
        config.expressionEngine = XPathExpressionEngine()

        val projContext = MockKUtil.mockProjectContext()
        assertFalse(MybatisMapperVisitor.checkPreCondition("mockFile",config,projContext))
    }

    private fun getXMLFile():File{
        val file = File("mockFile")
        val inputStream = ByteArrayInputStream("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
            <mapper namespace="com.mock.MockMapper">
            </mapper>
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
