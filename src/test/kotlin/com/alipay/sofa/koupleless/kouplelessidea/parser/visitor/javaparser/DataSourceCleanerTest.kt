package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.SimpleName
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/3 15:48
 */
class DataSourceCleanerTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var dataSourceCleaner: DataSourceCleaner

    @MockK
    private lateinit var dataSource: DBContext.MybatisDataSource

    @MockK
    private lateinit var compilationUnit: CompilationUnit

    @Before
    fun setUp() {
        val filePath = "testFilePath"
        dataSourceCleaner = DataSourceCleaner(filePath, dataSource)
    }

    @Test
    fun `doParse should remove method from compilation unit`() {
        every { dataSource.beanInfo } returns MockKUtil.spyBeanInfo()
        every { dataSource.beanInfo.definedByMethod() } returns true
        every { dataSource.beanInfo.getAttribute(SplitConstants.METHOD_BEAN) } returns "methodBeanSignature()"
        val method = MethodDeclaration()
        method.name = SimpleName("methodBeanSignature")
        every { compilationUnit.getType(0) } returns ClassOrInterfaceDeclaration()
        compilationUnit.getType(0).addMember(method)

        assertEquals(compilationUnit.getType(0).methods.size,1)
        dataSourceCleaner.doParse(null, compilationUnit, null)
        assertEquals(compilationUnit.getType(0).methods.size,0)
    }

    @Test
    fun `doParse should not remove any method from compilation unit`() {
        every { dataSource.beanInfo } returns MockKUtil.spyBeanInfo()
        every { dataSource.beanInfo.definedByMethod() } returns false

        dataSourceCleaner.doParse(null, compilationUnit, null)
    }
}
