package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.InitAppCustomDataSourcePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/30 21:39
 */
class InitAppCustomDataSourcePluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        InitAppCustomDataSourcePlugin.doProcess(splitModuleContext)
        assertEquals("com.mock.MockDataSource",splitModuleContext.appContext.analyseConfig.getCustomDataSourceClasses().first())
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
        }
        splitModuleContext.appContext.classInfoContext.addClassInfo(mockk<ClassInfo>{
            every { fullName } returns "com.mock.MockDataSource"
            every { className } returns "MockDataSource"
            every { srcPath } returns "mockPath"
            every { extendClass } returns mutableSetOf("com.alipay.zdal.client.jdbc.ZdalDataSource")
        })
        return splitModuleContext
    }
}
