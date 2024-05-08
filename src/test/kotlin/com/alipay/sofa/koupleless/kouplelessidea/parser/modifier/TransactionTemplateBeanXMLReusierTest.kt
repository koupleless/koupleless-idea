package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.CompilationUnit
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/6 14:43
 */
class TransactionTemplateBeanXMLReusierTest {
    private val cu = getCu()

    private fun getCu(): CompilationUnit {
        return MockKUtil.readCu(SplitConstants.MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE)
    }


    @Test
    fun testDoParse(){

        // 准备数据
        val templateBeanInfo = MockKUtil.spyBeanInfo("mockTransactionTemplate","org.springframework.transaction.support.TransactionTemplate")
        val transactionTemplate = mockk<DBContext.TransactionTemplate>{
            every { beanInfo } returns templateBeanInfo
        }

        // 修改
        val modifier = TransactionTemplateBeanXMLReuser("mockPath",transactionTemplate,MockKUtil.mockContentPanel())
        modifier.doParse(Path.of("mockPath"),cu,null)

        // 验证 cu 有 mockTransactionTemplate 的方法
        val mockTransactionTemplateMethod = JavaParserUtil.filterMethodByName(cu.getType(0),
            "mockTransactionTemplate").firstOrNull()
        assertNotNull(mockTransactionTemplateMethod)

        // 验证该方法没有参数
        assertTrue(mockTransactionTemplateMethod.parameters.isEmpty())

        // 验证该方法的bean名称为 mockTransactionTemplate
        val beanName = ParseBeanService.parseBeanName(mockTransactionTemplateMethod).firstOrNull()
        assertEquals("mockTransactionTemplate",beanName)

        // 验证该方法有 getBean 语句
        val getBeanStat = JavaParserUtil.filterMethodCallStatInMethodBody(mockTransactionTemplateMethod,"getBaseBean").firstOrNull()
        assertNotNull(getBeanStat)

        // 验证 cu 的 imports 有 org.springframework.transaction.PlatformTransactionManager
        assertTrue(cu.imports.any { it.nameAsString == "org.springframework.transaction.support.TransactionTemplate" })
        assertTrue(cu.imports.any { it.nameAsString == "org.springframework.context.annotation.Bean" })
    }
}
