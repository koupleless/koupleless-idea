package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.ConfigSingleBundlePomDependencyPlugin
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
class ConfigSingleBundlePomDependencyPluginTest {
    // 可能导致问题
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        mockkObject(MavenPomUtil){
            val slots = mutableListOf<Model>()
            every { MavenPomUtil.writePomModel(any<File>(),capture(slots)) } returns Unit
            ConfigSingleBundlePomDependencyPlugin.doProcess(splitModuleContext)

            val model = slots[0]
            assertTrue(model.dependencies.any { it.artifactId == "mockbase-model" && it.groupId == "com.mock" })
            assertTrue(model.dependencies.none{ it.artifactId == "mocksingle" && it.groupId == "com.mock" })
        }
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every {moduleContext} returns spyk(ModuleContext(this)){
                every { getModulePath() } returns MockKUtil.getTestResourcePath("mockproj/mocksinglemodule")
            }
        }

        val moduleImplClassInfoPath  = MockKUtil.getTestResourcePath("mockproj/mocksinglemodule/src/main/java/com/singlemodule/service/MockFacade.java")
        val moduleModelClassInfoPath = MockKUtil.getTestResourcePath("mockproj/mocksinglemodule/src/main/java/com/singlemodule/model/MockModel.java")
        val srcBaseClassInfoPath  = MockKUtil.getTestResourcePath("mockproj/mockbase/app/core/model/src/main/java/com/mock/core/model/MockModel.java")
        val moduleImplClassInfo = MockKUtil.spyClassInfo("MockModuleImpl",moduleImplClassInfoPath)
        val moduleModelClassInfo = MockKUtil.spyClassInfo("MockModuleModel",moduleModelClassInfoPath)
        val srcBaseClassInfo = MockKUtil.spyClassInfo("MockSrcBaseModel",srcBaseClassInfoPath)
        moduleImplClassInfo.referClass["fieldA"] = srcBaseClassInfo
        moduleImplClassInfo.referClass["fieldB"] =  moduleModelClassInfo
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(moduleImplClassInfo)
        return splitModuleContext
    }
}
