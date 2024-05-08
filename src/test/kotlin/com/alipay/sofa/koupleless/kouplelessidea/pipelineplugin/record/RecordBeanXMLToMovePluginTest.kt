package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.record

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.IntegrationStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.XMLNode
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.record.RecordBeanXMLToMovePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.configuration2.tree.ImmutableNode
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:16
 */
class RecordBeanXMLToMovePluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        RecordBeanXMLToMovePlugin.doProcess(splitModuleContext)
        val moduleDefaultXMLPath = StrUtil.join(FileUtil.FILE_SEPARATOR,splitModuleContext.moduleContext.getSpringResourceDir(),"mockXML.xml")
        val moveContext = splitModuleContext.integrationStageContext.integrateContext
        assertEquals(1,moveContext.getXMLFileModifier(moduleDefaultXMLPath).first().nodesToAdd.size)
        assertEquals(1,moveContext.getXMLFileModifier("mockXML.xml").first().nodesToRemove.size)
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns ModuleContext(this)
            every { srcBaseContext } returns BaseContext(this)
            every { integrationStageContext } returns IntegrationStageContext(this)
            every { splitMode } returns SplitConstants.SplitModeEnum.MOVE

        }
        // 配置moduleContext
        splitModuleContext.moduleContext.projectPath = "mockModulePath"
        splitModuleContext.moduleContext.moduleTemplateType = SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag

        val beanInfo = MockKUtil.spyBeanInfo("mockXMLBean","com.mock.MockXMLBean")
        beanInfo.definedByXML = true
        beanInfo.registerXMLNode(SplitConstants.BEAN_XML_NODE, XMLNode("mockXML.xml",mockk<ImmutableNode>{}))
        splitModuleContext.moduleContext.beanContext.addBeanInfo(beanInfo)

        return splitModuleContext
    }
}
