package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan.ScanModuleDefaultBeanPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.github.javaparser.ParserConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:06
 */
class ScanModuleDefaultBeanPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        ScanModuleDefaultBeanPlugin.doProcess(splitModuleContext)
        assertTrue(splitModuleContext.moduleContext.beanContext.allBeanInfo.isNotEmpty())
        assertTrue(splitModuleContext.moduleContext.configContext.dbContext.mapperInterfaces.isNotEmpty())
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns MockKUtil.spyApplicationContext(this)
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { getParserConfig() } returns ParserConfiguration()
            }
        }
        // 配置模块文件信息
        val moduleImplPath = "mockproj/mockbase/app/core/service/src/main/java/com/mock/core/service/impl/MockFacadeImpl.java"
        val moduleFacadePath = "mockproj/mockbase/app/core/service/src/main/java/com/mock/core/service/MockFacade.java"
        val methodBeanPath = "parser/MybatisMethodConfigDemo.java"
        val methodConfigPath = "parser/MybatisConfigurationDemo.java"
        val mapperPath = "parser/MybatisMapperDemo.java"

        val moduleImplFile = MockKUtil.readFile(moduleImplPath)
        val moduleFacadeFile = MockKUtil.readFile(moduleFacadePath)
        val methodBeanFile = MockKUtil.readFile(methodBeanPath)
        val methodConfigFile = MockKUtil.readFile(methodConfigPath)
        val mapperFile = MockKUtil.readFile(mapperPath)

        splitModuleContext.moduleContext.files.addAll(listOf(
            moduleImplFile,
            moduleFacadeFile,
            methodBeanFile,
            methodConfigFile,
            mapperFile
        ))

        // 配置模块类信息
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.MockXMLClass","mockXMLClassPath"))
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.core.service.impl.MockFacadeImpl",moduleImplFile.absolutePath))
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.core.service.MockFacade",moduleFacadeFile.absolutePath))
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.config.MybatisMethodConfigDemo",methodBeanFile.absolutePath))
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.config.MybatisConfiguration",methodConfigFile.absolutePath))
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.example.DOMapper",mapperFile.absolutePath))

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
