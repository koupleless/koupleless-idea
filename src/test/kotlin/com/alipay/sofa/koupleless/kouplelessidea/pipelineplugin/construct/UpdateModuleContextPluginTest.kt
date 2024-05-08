package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.UpdateModuleContextPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:18
 */
class UpdateModuleContextPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        UpdateModuleContextPlugin.doProcess(splitModuleContext)

        val moduleContext = splitModuleContext.moduleContext
        assertTrue(moduleContext.classInfoContext.containsPath("src/main/java/tgtClassPath.java"))
        assertFalse(moduleContext.classInfoContext.containsPath("src/main/java/srcClassPath.java"))
        val classInfo = moduleContext.classInfoContext.getClassInfoByPath("src/main/java/tgtClassPath.java")
        assertEquals("src/main/java/tgtClassPath.java",classInfo!!.getPath())
        assertTrue(moduleContext.files.isNotEmpty())
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { getSrcPathToTgtPath() } returns mapOf("src/main/java/srcClassPath.java" to "src/main/java/tgtClassPath.java")
                every { moduleLocation } returns MockKUtil.getTestResourcePath("mockproj")
                every { name } returns "mockbase"
            }
        }
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("srcClass","src/main/java/srcClassPath.java"))
        return splitModuleContext
    }
}
