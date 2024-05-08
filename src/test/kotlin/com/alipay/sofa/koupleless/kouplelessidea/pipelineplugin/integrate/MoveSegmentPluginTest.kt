package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.integrate

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.FileUtil.copyFile
import cn.hutool.core.io.FileUtil.writeFromStream
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.IntegrationStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.XmlUtil
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.MoveSegmentPlugin
import io.mockk.*
import org.junit.Test
import java.io.InputStream
import kotlin.test.assertEquals



/**
 * @description:
 * @author lipeng
 * @date 2024/2/20 00:21
 */
class MoveSegmentPluginTest {
    @Test
    fun testDoProcessWithPath(){
        val splitModuleContext = mockSplitModuleContextWithoutResource()

        mockkObject(ParseJavaService)
        mockkObject(XmlUtil)
        every { ParseJavaService.parseAndSave(any(),any(),any(),null) } answers { nothing }
        every { XmlUtil.parseAndSave(any(),any(),null) } answers { nothing }

        mockkStatic(FileUtil::class){
            val copiedFileTgtPath = mutableListOf<String>()
            every { copyFile(any() as String,capture(copiedFileTgtPath))} answers { nothing }


            MoveSegmentPlugin.doProcess(splitModuleContext)

            assertEquals("tgtJavaCopyPath",copiedFileTgtPath[0])
            assertEquals("tgtXMLCopyPath",copiedFileTgtPath[1])
        }

        unmockkObject(XmlUtil)
        unmockkObject(ParseJavaService)
    }

    @Test
    fun testDoProcessWithResource(){
        val splitModuleContext = mockSplitModuleContextWithResource()
        mockkObject(ParseJavaService)
        mockkObject(XmlUtil)

        every { ParseJavaService.parseAndSave(any(),any(),any(),null) } answers { nothing }
        every { XmlUtil.parseAndSave(any(),any(),null) } answers { nothing }

        mockkStatic(FileUtil::class){
            val copiedResourceTgtPath = mutableListOf<String>()
            every { writeFromStream(any() as InputStream,capture(copiedResourceTgtPath)) } answers { nothing }

            MoveSegmentPlugin.doProcess(splitModuleContext)

            assertEquals("tgtJavaResource",copiedResourceTgtPath[0])
            assertEquals("tgtXMLResource",copiedResourceTgtPath[1])
        }

        unmockkObject(XmlUtil)
        unmockkObject(ParseJavaService)
    }

    private fun mockSplitModuleContextWithoutResource(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { integrationStageContext } returns IntegrationStageContext(this)
        }
        val integrateContext = splitModuleContext.integrationStageContext.integrateContext
        integrateContext.setJavaCopyPath("tgtJavaCopyPath","srcJavaCopyPath")
        integrateContext.setXMLPathToCopy("tgtXMLCopyPath","srcXMLPathToCopy")

        return splitModuleContext
    }

    private fun mockSplitModuleContextWithResource():SplitModuleContext{
        val splitModuleContext = mockk<SplitModuleContext>{
            every { integrationStageContext } returns IntegrationStageContext(this)
        }
        val integrateContext = splitModuleContext.integrationStageContext.integrateContext
        integrateContext.setJavaResource("tgtJavaResource","parser/ClassInfoDemo.java")
        integrateContext.setXMLResource("tgtXMLResource","parser/ClassInfoDemo.java")
        return splitModuleContext
    }
}
