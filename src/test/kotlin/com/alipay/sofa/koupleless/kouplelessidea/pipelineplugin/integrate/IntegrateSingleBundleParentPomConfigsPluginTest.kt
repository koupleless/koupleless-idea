package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.IntegrateSingleBundleParentPomConfigsPlugin
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
 * @date 2024/2/20 00:21
 */
class IntegrateSingleBundleParentPomConfigsPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        mockkObject(MavenPomUtil){

            val slot = slot<Model>()
            every { MavenPomUtil.writePomModel(any<File>(),capture(slot)) } returns Unit
            IntegrateSingleBundleParentPomConfigsPlugin.doProcess(splitModuleContext)
            val parentPom = slot.captured
            assertTrue(parentPom.dependencies.first { it.artifactId == "outside-model-a" }.version == "1.1.0")
            assertTrue(parentPom.dependencies.first { it.artifactId == "outside-model-b" }.version == "1.2.0")
        }
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every {moduleContext} returns spyk(ModuleContext(this)){
                every { getModulePath() } returns MockKUtil.getTestResourcePath("mockproj/mocksinglemodule")
            }
            every { srcBaseContext } returns BaseContext(this)
        }

        splitModuleContext.srcBaseContext.projectPath = MockKUtil.getTestResourcePath("mockproj/mockbase")

        val srcClassInfoPath  = MockKUtil.getTestResourcePath("mockproj/mockbase/app/core/model/src/main/java/com/mock/core/model/MockModel.java")
        val classInfo = MockKUtil.spyClassInfo("Model",srcClassInfoPath)
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        return splitModuleContext
    }
}
