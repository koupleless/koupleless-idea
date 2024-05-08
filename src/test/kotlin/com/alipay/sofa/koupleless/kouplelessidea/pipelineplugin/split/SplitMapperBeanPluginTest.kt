package com.alipay.cloudide.serverless.pipelineplugin.split

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.IntegrationStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.XmlUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml.MybatisMapperVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.split.SplitMapperBeanPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:08
 */
class SplitMapperBeanPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        SplitMapperBeanPlugin.doProcess(splitModuleContext)

        assertTrue(splitModuleContext.moduleContext.beanContext.containsBeanName("moduleMockDAO"))
        assertTrue(splitModuleContext.moduleContext.configContext.dbContext.mapperInterfaces.containsKey("com.mock.ModuleMockDAO"))

        assertTrue(splitModuleContext.srcBaseContext.beanContext.containsBeanName("baseMockDAO"))
        assertTrue(splitModuleContext.srcBaseContext.configContext.dbContext.mapperInterfaces.containsKey("com.mock.BaseMockDAO"))

        val srcXMLPath = "mockModuleXMLPath"
        val tgtXMLPath =  StrUtil.join(
            FileUtil.FILE_SEPARATOR,splitModuleContext.integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_MAPPER_LOCATION_CONFIG) as String,srcXMLPath.substringAfterLast(
                FileUtil.FILE_SEPARATOR))
        val modifier = splitModuleContext.integrationStageContext.integrateContext.getXMLFileModifier(tgtXMLPath).first()
        assertEquals(srcXMLPath, modifier.absolutePathToCopy)
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every {appContext} returns MockKUtil.spyApplicationContext(this)
            every { moduleContext } returns ModuleContext(this)
            every { srcBaseContext } returns BaseContext(this)
            every { integrationStageContext } returns IntegrationStageContext(this)
        }

        val baseXmlFile = spyk(MockKUtil.readFile("mockproj/mockbase/app/core/model/src/main/resources/base_mapping.xml")){
            every { absolutePath } returns "mockBaseXMLPath"
            every { path } returns "mockBaseXMLPath"
        }

        val moduleXmlFile = spyk(MockKUtil.readFile("mockproj/mockbase/app/bootstrap/src/main/resources/module_mapping.xml")){
            every { absolutePath } returns "mockModuleXMLPath"
            every { path } returns "mockModuleXMLPath"
        }

        splitModuleContext.integrationStageContext.setConfig(SplitConstants.MODULE_MYBATIS_MAPPER_LOCATION_CONFIG,"mockLocationConfig")

        XmlUtil.parseDefaultXml(listOf(baseXmlFile,moduleXmlFile), listOf(MybatisMapperVisitor), splitModuleContext.appContext)

        splitModuleContext.moduleContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.ModuleMockDAO","mockModuleXMLPath"))
        splitModuleContext.srcBaseContext.classInfoContext.addClassInfo(MockKUtil.spyClassInfo("com.mock.BaseMockDAO","mockBaseXMLPath"))
        return splitModuleContext
    }
}
