package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfoContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.github.javaparser.ParserConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/2 17:28
 */
class ClassRefVisitorTest {
    @Test
    fun testDoParse(){
        val url = this.javaClass.classLoader.getResource("parser/ClassRefDemo.java")!!
        val file = File(url.toURI())
        val splitModuleContext = mockSplitModuleContext()

        val demoClassInfo = MockKUtil.spyClassInfo("com.mock.ClassRefDemo","mockClassRefDemoPath")
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(demoClassInfo)
        ParseJavaService.parseOnly(listOf(file), ParserConfiguration(),listOf(ClassRefVisitor),splitModuleContext)

        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.CommonResult"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.BaseController"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.BaseFacade"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.MockException"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.CustomClassAnno"))

        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.UserInfo"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.UserInfoQueryRequest"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.OrderListQueryResponse"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.AException"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.CustomMethodAnno"))
    }

    private fun mockSplitModuleContext():SplitModuleContext{

        return mockk<SplitModuleContext>{
            every { srcBaseContext.classInfoContext } returns spyk(ClassInfoContext(srcBaseContext)){
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.CommonResult","mockCommonResultPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.BaseController","mockBaseControllerPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.BaseFacade","mockBaseFacadePath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.MockException","mockMockExceptionPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.CustomClassAnno","mockCustomClassAnno"))
            }
            every { moduleContext.classInfoContext } returns spyk(ClassInfoContext(moduleContext)){
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.UserInfo","mockUserInfoPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.UserInfoQueryRequest","mockUserInfoQueryRequestPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.OrderListQueryResponse","mockOrderListQueryResponsePath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.AException","mockAExceptionPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.CustomMethodAnno","mockCustomMethodAnno"))
            }
        }
    }
}
