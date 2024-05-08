package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.initial


import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.initial.InitAppDefaultBeanPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/30 21:39
 */
class InitAppDefaultBeanPluginTest {

    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()

        mockkObject(ParseJavaService){
            every { ParseJavaService.parseFromCache(any(), any<List<JavaParserVisitor<ProjectContext>>>(), any(),any()) } returns Unit
            InitAppDefaultBeanPlugin.doProcess(splitModuleContext)
        }

        val beanContext = splitModuleContext.appContext.beanContext
        assertTrue(beanContext.containsBeanName("mockXMLBeanId"))
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
        }

        val xmlBeanInfo = spyk(BeanInfo("mockXMLBeanId","com.mock.MockXMLClass")) {
            every { interfaceTypes } returns mutableSetOf()
        }

        // 添加 xml 中定义的bean
        splitModuleContext.appContext.xmlContext.registerBeanNode(mockk<XMLContext.BeanXMLNode>{
            every { beanName } returns "mockXMLBeanId"
            every { fullClassName } returns "com.mock.MockXMLClass"
            every { autowired } returns "byName"
            every { beanInfo } returns xmlBeanInfo
            every { xmlNode } returns mockk<XMLNode>{}
        })
        splitModuleContext.appContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.MockXMLClass","mockPath"))
        return splitModuleContext
    }
}
