package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanSrcBaseDefaultBeanPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:06
 */
class ScanSrcBaseDefaultBeanPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        mockkObject(ParseJavaService){
            // parseFromCache 不验证。该流程和模块一致，所以此处不验证
            every { ParseJavaService.parseFromCache(any(), any<List<JavaParserVisitor<ProjectContext>>>(), any(),any()) } returns Unit
            ScanSrcBaseDefaultBeanPlugin.doProcess(splitModuleContext)
        }
        assertEquals(1,splitModuleContext.srcBaseContext.beanContext.allBeanInfo.size)
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
            every { srcBaseContext} returns BaseContext(this)
            every { moduleContext } returns ModuleContext(this)
        }

        // 配置基座类信息
        splitModuleContext.srcBaseContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.MockXMLClass","mockXMLClassPath"))

        // 配置应用xml信息
        splitModuleContext.appContext.xmlContext.registerBeanNode(mockk<XMLContext.BeanXMLNode>{
            every { beanName } returns "mockXMLBeanId"
            every { fullClassName } returns "com.mock.MockXMLClass"
            every { autowired } returns "byName"
            every { beanInfo } returns spyk(BeanInfo("mockXMLBeanId","com.mock.MockXMLClass")) {
                every { interfaceTypes } returns mutableSetOf()
            }
            every { xmlNode } returns mockk<XMLNode>{}
        })
        return splitModuleContext
    }
}
