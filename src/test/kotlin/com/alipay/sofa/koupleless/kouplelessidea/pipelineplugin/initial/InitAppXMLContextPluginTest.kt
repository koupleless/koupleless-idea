package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.initial

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.InitAppXMLContextPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/30 21:39
 */
class InitAppXMLContextPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()

        mockkObject(ParseJavaService){
            every { ParseJavaService.parseFromCache(any(), any<List<JavaParserVisitor<ProjectContext>>>(), any(),any()) } returns Unit
            InitAppXMLContextPlugin.doProcess(splitModuleContext)
        }
        val xmlContext = splitModuleContext.appContext.xmlContext
        assertEquals(xmlContext.getMapperXMLs().size,2)
        assertEquals(xmlContext.getBeanNodes().size,1)
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
        }
        return splitModuleContext
    }
}
