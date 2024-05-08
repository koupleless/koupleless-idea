package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.InitAppCustomAnnoPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/30 21:38
 */
class InitAppCustomAnnoPluginTest {
    @Test
    fun testDoProcess() {
        val splitModuleContext = mockSplitModuleContext()
        InitAppCustomAnnoPlugin.doProcess(splitModuleContext)
        assertEquals("BeanAnnotation",splitModuleContext.appContext.analyseConfig.getCustomBeanAnnotations().first())
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
        }
        splitModuleContext.appContext.classInfoContext.addClassInfo(mockk<ClassInfo>{
            every { isAnnotation } returns true
            every { annotations } returns mutableSetOf("RestController")
            every { fullName } returns "com.mock.bean.BeanAnnotation"
            every { className } returns "BeanAnnotation"
            every { srcPath } returns "mockPath"
        })
        return splitModuleContext
    }
}
