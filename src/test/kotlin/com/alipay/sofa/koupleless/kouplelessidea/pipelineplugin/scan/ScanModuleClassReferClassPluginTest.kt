package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfoContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanModuleClassReferClassPlugin
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
 * @date 2024/2/20 00:04
 */
class ScanModuleClassReferClassPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        // 添加文件
        val url = this.javaClass.classLoader.getResource("parser/ClassRefDemo.java")!!
        val file = File(url.toURI())
        splitModuleContext.moduleContext.files.add(file)
        // 添加类信息
        val demoClassInfo = MockKUtil.spyClassInfo("com.mock.ClassRefDemo",file.absolutePath)
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(demoClassInfo)

        ScanModuleClassReferClassPlugin.doProcess(splitModuleContext)
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

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { srcBaseContext.classInfoContext } returns spyk(ClassInfoContext(srcBaseContext)){
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.CommonResult","mockCommonResultPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.BaseController","mockBaseControllerPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.BaseFacade","mockBaseFacadePath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.MockException","mockMockExceptionPath"))
                this.addClassInfo(MockKUtil.spyClassInfo("com.mock.src.CustomClassAnno","mockCustomClassAnno"))
            }
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { classInfoContext } returns spyk(ClassInfoContext(this)){
                    this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.UserInfo","mockUserInfoPath"))
                    this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.UserInfoQueryRequest","mockUserInfoQueryRequestPath"))
                    this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.OrderListQueryResponse","mockOrderListQueryResponsePath"))
                    this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.AException","mockAExceptionPath"))
                    this.addClassInfo(MockKUtil.spyClassInfo("com.mock.module.CustomMethodAnno","mockCustomMethodAnno"))
                }
                every { getParserConfig() } returns ParserConfiguration()
            }
        }
        return splitModuleContext
    }
}
