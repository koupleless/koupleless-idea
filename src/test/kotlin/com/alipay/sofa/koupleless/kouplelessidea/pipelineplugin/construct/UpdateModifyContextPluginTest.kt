package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.ModifyStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.modifier.JavaFileModifier
import com.alipay.sofa.koupleless.kouplelessidea.parser.modifier.XMLFileModifier
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.UpdateModifyContextPlugin
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:18
 */
class UpdateModifyContextPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        UpdateModifyContextPlugin.doProcess(splitModuleContext)
        assertTrue(splitModuleContext.modifyStageContext.modifyContext.getJavaFileModifier("tgt/main/java").isNotEmpty())
        assertTrue(splitModuleContext.modifyStageContext.modifyContext.getXMLFileModifier("tgt/main/xml").isNotEmpty())

        assertTrue(splitModuleContext.modifyStageContext.modifyContext.getJavaFileModifier("src/main/java").isEmpty())
        assertTrue(splitModuleContext.modifyStageContext.modifyContext.getXMLFileModifier("src/main/xml").isEmpty())
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { getSrcPathToTgtPath() } returns mapOf("src/main/java" to "tgt/main/java", "src/main/xml" to "tgt/main/xml")
            }
            every { modifyStageContext } returns ModifyStageContext(this)
        }
        splitModuleContext.modifyStageContext.modifyContext.addModifier("src/main/java",mockk<JavaFileModifier>())
        splitModuleContext.modifyStageContext.modifyContext.addModifier("src/main/xml",mockk<XMLFileModifier>())
        return splitModuleContext
    }
}
