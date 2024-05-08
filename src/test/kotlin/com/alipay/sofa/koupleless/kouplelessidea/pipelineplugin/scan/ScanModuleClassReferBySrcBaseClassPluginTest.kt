package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanModuleClassReferBySrcBaseClassPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.Test


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:05
 */
class ScanModuleClassReferBySrcBaseClassPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        // 不测试 parseFromCache
        mockkObject(ParseJavaService){
            every { ParseJavaService.parseFromCache(any(), any<List<JavaParserVisitor<ProjectContext>>>(), any(),any()) } returns Unit
            ScanModuleClassReferBySrcBaseClassPlugin.doProcess(splitModuleContext)
        }
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext> {
            every { moduleContext } returns ModuleContext(this)
            every { srcBaseContext } returns BaseContext(this)
        }
        splitModuleContext.moduleContext.files.addAll(listOf(
            MockKUtil.spyFile("mockPath/ModuleModel.java"),
            MockKUtil.spyFile("resources/config.yaml")))

        return splitModuleContext
    }
}
