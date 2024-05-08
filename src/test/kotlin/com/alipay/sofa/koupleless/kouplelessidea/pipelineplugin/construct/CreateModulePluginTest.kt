package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.ModuleService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.CreateModulePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.SINGLE_BUNDLE_TEMPLATE_ARCHETYPE
//import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import org.junit.Test


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:18
 */
class CreateModulePluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContextWithMono()

        // 验证共库模块创建
        mockkObject(ModuleService){
            every { ModuleService.createModule(any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any()) } returns mockk<Process>{
                every { waitFor() } returns 0
            }

            val plugin = spyk(CreateModulePlugin(MockKUtil.mockContentPanel()),recordPrivateCalls = true)
            plugin.doProcess(splitModuleContext)
        }

        // 验证独立库模块创建
        mockkObject(ModuleService){
            every { ModuleService.createIndependentModule(any(),any(),any(),any(),any(),any(),any(),any(),any(),any()) } returns mockk<Process>{
                every { waitFor() } returns 0
            }

            val plugin = spyk(CreateModulePlugin(MockKUtil.mockContentPanel()),recordPrivateCalls = true)
            splitModuleContext.moduleContext.isMono = false
            plugin.doProcess(splitModuleContext)
        }
    }

    private fun mockSplitModuleContextWithMono(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { name } returns "moduleName"
                every { groupId } returns "moduleGroupId"
                every { artifactId } returns "moduleArtifactId"
                every { packageName } returns "modulePackage"
                every { moduleLocation } returns "moduleLocation"
                every { moduleTemplate } returns SINGLE_BUNDLE_TEMPLATE_ARCHETYPE
                every { type } returns "MODULE"
            }
            every { srcBaseContext } returns mockk<BaseContext>{
                every { name } returns "appName"
            }
            every { project } returns mockk<Project>{
//                every{ service<Any>()} returns mockk<IDEProjectSettings>{
//                    every { namespace } returns "namespace"
//                    every { project } returns "project"
//                }
            }
        }

        splitModuleContext.moduleContext.isMono = true
        return splitModuleContext
    }
}
