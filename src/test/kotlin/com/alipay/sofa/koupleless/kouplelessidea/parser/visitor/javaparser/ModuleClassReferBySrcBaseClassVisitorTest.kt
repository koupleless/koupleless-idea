package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfoContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.github.javaparser.JavaParser
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertTrue


/**
 * @description:
 * @author lipeng
 * @date 2024/2/20 00:03
 */
class ModuleClassReferBySrcBaseClassVisitorTest {
    @Test
    fun testDoParse() {
        val url = this.javaClass.classLoader.getResource("parser/ModuleClassReferBySrcBaseClassDemo.java")!!
        val file = File(url.toURI())
        val splitModuleContext = mockSplitModuleContext()

        val demoPath = "mockModuleClassReferBySrcBaseClassDemoPath"
        val demoClassInfo = MockKUtil.spyClassInfo("com.mock.ModuleClassReferBySrcBaseClassDemo",demoPath)
        splitModuleContext.srcBaseContext.classInfoContext.addClassInfo(demoClassInfo)

        ModuleClassReferBySrcBaseClassVisitor.doParse(Path.of(demoPath),JavaParser().parse(file).result.get(),splitModuleContext)
        // 验证在不同package 的情况
        // 此处未验证 import com.a.b.* 的情况，因为这种情况需要配置 parserConfiguration
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.outside.MockClass"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.CommonResult"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.BaseController"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.BaseFacade"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.src.MockException"))

        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.UserInfo"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.UserInfoQueryRequest"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.OrderListQueryResponse"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.module.AException"))

        // 验证在相同package 的情况
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.ModelA"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.ModelB"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.ModelC"))
        assertTrue(demoClassInfo.referClass.containsKey("com.mock.ModelD"))
    }



    private fun mockSplitModuleContext(): SplitModuleContext {

        return mockk<SplitModuleContext>{
            every { srcBaseContext.classInfoContext } returns ClassInfoContext(srcBaseContext)
            every { moduleContext.classInfoContext } returns spyk(ClassInfoContext(moduleContext)){
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.UserInfo","mockUserInfoPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.UserInfoQueryRequest","mockUserInfoQueryRequestPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.OrderListQueryResponse","mockOrderListQueryResponsePath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.AException","mockAExceptionPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.outside.MockClass","mockOutsideMockClassPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.CommonResult","mockCommonResultPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.BaseController","mockBaseControllerPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.BaseFacade","mockBaseFacadePath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.MockException","mockMockExceptionPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.ModelA","mockModelAPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.ModelB","mockModelBPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.ModelC","mockModelCPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.ModelD","mockModelDPath"))
            }
        }
    }
}
