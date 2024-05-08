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
 * @date 2024/1/5 17:18
 */
class SqlSessionTemplateBeanXMLReuserTest {
    private val cu = getCu()

    private fun getCu(): CompilationUnit {
        return MockKUtil.readCu(SplitConstants.MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE)
    }

    @Test
    fun testDoParse(){
        // 准备数据
        val templateBeanInfo = MockKUtil.spyBeanInfo("mockSqlSessionTemplateBean","org.mybatis.spring.SqlSessionTemplate")
        val sqlSessionTemplate = mockk<DBContext.SqlSessionTemplate>{
            every { beanInfo } returns templateBeanInfo
        }

        // 修改
        val modifier = SqlSessionTemplateBeanXMLReuser("mockPath",sqlSessionTemplate,MockKUtil.mockContentPanel())
        modifier.doParse(Path.of("mockPath"),cu,null)

        // 验证 cu 有 mockSqlSessionTemplateBean 的方法
        val mockSqlSessionTemplateBeanMethod = JavaParserUtil.filterMethodByName(cu.getType(0),
            "mockSqlSessionTemplateBean").firstOrNull()
        assertNotNull(mockSqlSessionTemplateBeanMethod)

        // 验证该方法没有参数
        assertTrue(mockSqlSessionTemplateBeanMethod.parameters.isEmpty())

        // 验证该方法的bean名称为 mockSqlSessionTemplateBean
        val beanName = ParseBeanService.parseBeanName(mockSqlSessionTemplateBeanMethod).firstOrNull()
        assertEquals("mockSqlSessionTemplateBean",beanName)

        // 验证该方法有 getBean 语句
        val getBeanStat = JavaParserUtil.filterMethodCallStatInMethodBody(mockSqlSessionTemplateBeanMethod,"getBaseBean").firstOrNull()
        assertNotNull(getBeanStat)

        // 验证 cu 的 imports 有 org.mybatis.spring.SqlSessionTemplate
        assertTrue(cu.imports.any { it.nameAsString == "org.mybatis.spring.SqlSessionTemplate" })
        assertTrue(cu.imports.any { it.nameAsString == "org.springframework.context.annotation.Bean" })
    }
}
