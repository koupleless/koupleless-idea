package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.ConfigSrcBaseDependencyInModulePomPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.*
import org.apache.maven.model.Model
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:20
 */
class ConfigSrcBaseDependencyInModulePomPluginTest {
    @Test
    fun testDoProcessWithoutNewBase(){
        val splitModuleContext = mockSplitModuleContextWithoutNewBase()
        mockkObject(MavenPomUtil){
            val slotPom = slot<Model>()
            every { MavenPomUtil.writePomModel(any<File>(),capture(slotPom)) } returns Unit

            val plugin = ConfigSrcBaseDependencyInModulePomPlugin(MockKUtil.mockContentPanel())
            plugin.doProcess(splitModuleContext)

            val pom = slotPom.captured
            assertTrue(pom.dependencies.first { it.artifactId == "mockbase-model" }.scope == "provided")
        }
    }

    @Test
    fun testDoProcessWithNewBase(){
        val splitModuleContext = mockSplitModuleContextWithNewBase()
        mockkObject(MavenPomUtil){
            val plugin = ConfigSrcBaseDependencyInModulePomPlugin(MockKUtil.mockContentPanel())
            plugin.doProcess(splitModuleContext)
        }
    }

    private fun mockSplitModuleContextWithoutNewBase(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { getModulePath() } returns MockKUtil.getTestResourcePath("mockproj/mocksinglemodule")
            }
            every { srcBaseContext } returns BaseContext(this)
            every { toNewBase() } returns false
        }
        splitModuleContext.srcBaseContext.projectPath = MockKUtil.getTestResourcePath("mockproj/mockbase")
        return splitModuleContext
    }

    private fun mockSplitModuleContextWithNewBase(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { getModulePath() } returns MockKUtil.getTestResourcePath("mockproj/mocksinglemodule")
            }
            every { srcBaseContext } returns BaseContext(this)
            every { toNewBase() } returns true
        }
        splitModuleContext.srcBaseContext.projectPath = MockKUtil.getTestResourcePath("mockproj/mockbase")
        return splitModuleContext
    }


}
