package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.ConfigDependencyProvidedPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import org.apache.maven.model.Model
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:19
 */
class ConfigDependencyProvidedPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        mockkObject(MavenPomUtil){
            val pomSlot = slot<Model>()
            every { MavenPomUtil.writePomModel(any<File>(),capture(pomSlot)) } returns Unit

            ConfigDependencyProvidedPlugin.doProcess(splitModuleContext)

            val pom = pomSlot.captured
            assertTrue(pom.dependencies.all { it.scope == "provided"})
        }
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns mockk<ModuleContext>{
                every { getModulePath() } returns MockKUtil.getTestResourcePath("mockproj/mocksinglemodule")
            }
        }
        return splitModuleContext
    }
}
