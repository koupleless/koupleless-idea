package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.InitSrcBaseAndModuleJavaContextPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/19 17:10
 */
class InitSrcBaseAndModuleJavaContextPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        InitSrcBaseAndModuleJavaContextPlugin.doProcess(splitModuleContext)

        assertEquals(1,splitModuleContext.moduleContext.classInfoContext.getAllClassInfo().size)
        assertEquals(1,splitModuleContext.srcBaseContext.classInfoContext.getAllClassInfo().size)
        assertEquals(1,splitModuleContext.moduleContext.analyseConfig.getCustomBeanAnnotations().size)
        assertEquals(1,splitModuleContext.srcBaseContext.analyseConfig.getCustomBeanAnnotations().size)
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext> {
            every { appContext } returns MockKUtil.spyApplicationContext(this)
            every { moduleContext } returns ModuleContext(this)
            every { srcBaseContext } returns BaseContext(this)
        }
        splitModuleContext.moduleContext.files.addAll(listOf(MockKUtil.spyFile("mockPath/ModuleModel.java"),
            MockKUtil.spyFile("resources/config.yaml")))

        val appClassInfoContext = splitModuleContext.appContext.classInfoContext
        appClassInfoContext.addClassInfo(MockKUtil.spyClassInfo("ModuleModel","mockPath/ModuleModel.java"))
        appClassInfoContext.addClassInfo(MockKUtil.spyClassInfo("BaseModel","mockPath/BaseModel.java"))

        splitModuleContext.appContext.analyseConfig.addCustomBeanAnnotations(setOf("com.mock.APIController"))
        return splitModuleContext
    }
}
