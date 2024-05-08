package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.ModifyStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModifyContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.XMLPropertyPos
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.RefactorPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:21
 */
class RefactorPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        RefactorPlugin.doProcess(splitModuleContext)
        val refactorContext = splitModuleContext.modifyStageContext.refactorContext

        val modelModifier = refactorContext.getJavaFileModifier("mockproj/mocksinglemodule/src/main/java/com/singlemodule/model/MockModel.java").first()
        assertEquals("com.singlemodule.model",modelModifier.packageModifier.packageName)

        val modelAModifier = refactorContext.getJavaFileModifier("mockModuleAPath").first()
        assertEquals("com.singlemodule.model.MockModel",modelAModifier.importModifier.importsToReplacePartName["com.base.MockModel"])

        val xmlModifier = refactorContext.getXMLFileModifier("mockXMl.xml").first()
        assertTrue(xmlModifier.propertiesToSet.containsKey("mockNodeXPath/@propertyName"))
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { modifyStageContext } returns spyk(ModifyStageContext(this)){
                every { refactorContext } returns spyk(ModifyContext()){
                    every { modifyAndSave(any()) } answers { nothing }
                }
            }
            every { moduleContext } returns ModuleContext(this)

        }

        val moduleModelAClassInfo = MockKUtil.spyClassInfo("ModelA","mockModuleAPath")
        val classInfo = MockKUtil.spyClassInfo("com.base.MockModel","mockModuleSrcPath")
        classInfo.referByClass["ModelA"] = moduleModelAClassInfo
        classInfo.referByXML.add(XMLPropertyPos("mockNodeXPath","propertyName","mockXMl.xml"))
        classInfo.move("mockproj/mocksinglemodule/src/main/java/com/singlemodule/model/MockModel.java")

        splitModuleContext.moduleContext.classInfoContext.addClassInfo(classInfo)
        splitModuleContext.moduleContext.classInfoContext.addClassInfo(moduleModelAClassInfo)
        return splitModuleContext
    }
}
