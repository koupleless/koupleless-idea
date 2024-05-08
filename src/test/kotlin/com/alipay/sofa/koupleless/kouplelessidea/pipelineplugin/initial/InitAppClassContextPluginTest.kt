package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.InitAppClassContextPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/30 15:51
 */
class InitAppClassContextPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()

        InitAppClassContextPlugin.doProcess(splitModuleContext)

        val appContext = splitModuleContext.appContext
        assertEquals(6,appContext.classInfoContext.getAllClassInfo().size)
    }

    private fun mockSplitModuleContext():SplitModuleContext{
        return mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
        }
    }
}
