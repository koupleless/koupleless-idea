package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.SupplementStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse.AnalyseModuleBeanDependencyPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:13
 */
class AnalyseModuleBeanDependencyPluginTest {
    @Test
    fun testDoProcessWithoutTgtBase(){
        val splitModuleContext = mockSplitModuleContextWithoutTgtBase()
        AnalyseModuleBeanDependencyPlugin.doProcess(splitModuleContext)

        val beanContext = splitModuleContext.moduleContext.beanContext
        val moduleBeanA = beanContext.getBeanByName("moduleBeanA")!!
        val srcBaseBeanA = splitModuleContext.srcBaseContext.beanContext.getBeanByName("srcBaseBeanA")!!

        // 验证 xmlBean
        val beanXMLBean = beanContext.getBeanByName("moduleXMLBean")!!
        assertTrue(beanXMLBean.beanDependOn["fieldA"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanXMLBean.beanDependOn["fieldA"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(moduleBeanA.beanDependBy.contains(beanXMLBean))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanXMLBean))
        assertTrue(beanContext.missedOutsideBean.contains(beanXMLBean))
        val missedBeanRefForXMLBean = beanXMLBean.missedOutsideBean.keys.first { it.fieldName == "fieldA" }
        assertTrue(beanXMLBean.missedOutsideBean[missedBeanRefForXMLBean]!!.contains(srcBaseBeanA))

        // 验证 byType
        val beanJavaBeanByType = beanContext.getBeanByName("moduleJavaBeanByType")!!
        assertTrue(beanJavaBeanByType.beanDependOn["fieldByTypeInModule"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanJavaBeanByType.beanDependOn["fieldByTypeInSrcBase"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(moduleBeanA.beanDependBy.contains(beanJavaBeanByType))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanJavaBeanByType))
        assertTrue(beanContext.missedOutsideBean.contains(beanJavaBeanByType))
        val missedBeanRefForJavaBeanByType = beanJavaBeanByType.missedOutsideBean.keys.first { it.fieldName == "fieldByTypeInSrcBase" }
        assertTrue(beanJavaBeanByType.missedOutsideBean[missedBeanRefForJavaBeanByType]!!.contains(srcBaseBeanA))

        // 验证 byName
        val beanJavaBeanByName = beanContext.getBeanByName("moduleJavaBeanByName")!!
        assertTrue(beanJavaBeanByName.beanDependOn["fieldByNameInModule"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanJavaBeanByName.beanDependOn["fieldByNameInSrcBase"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(moduleBeanA.beanDependBy.contains(beanJavaBeanByName))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanJavaBeanByName))
        assertTrue(beanContext.missedOutsideBean.contains(beanJavaBeanByName))
        val missedBeanRefForByName = beanJavaBeanByName.missedOutsideBean.keys.first { it.fieldName == "fieldByNameInSrcBase" }
        assertTrue(beanJavaBeanByName.missedOutsideBean[missedBeanRefForByName]!!.contains(srcBaseBeanA))

        // 验证 TypeFirst
        val beanJavaBeanTypeFirst = beanContext.getBeanByName("moduleJavaBeanTypeFirst")!!
        assertTrue(beanJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInModule"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInSrcBase"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(moduleBeanA.beanDependBy.contains(beanJavaBeanTypeFirst))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanJavaBeanTypeFirst))
        assertTrue(beanContext.missedOutsideBean.contains(beanJavaBeanTypeFirst))
        val missedBeanRefForTypeFirst = beanJavaBeanTypeFirst.missedOutsideBean.keys.first { it.fieldName == "fieldTypeFirstInSrcBase" }
        assertTrue(beanJavaBeanTypeFirst.missedOutsideBean[missedBeanRefForTypeFirst]!!.contains(srcBaseBeanA))

        // 验证 NameFirst
        val beanJavaBeanNameFirst = beanContext.getBeanByName("moduleJavaBeanNameFirst")!!
        assertTrue(beanJavaBeanNameFirst.beanDependOn["fieldNameFirstInModule"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanJavaBeanNameFirst.beanDependOn["fieldNameFirstInSrcBase"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(moduleBeanA.beanDependBy.contains(beanJavaBeanNameFirst))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanJavaBeanNameFirst))
        assertTrue(beanContext.missedOutsideBean.contains(beanJavaBeanNameFirst))
        val missedBeanRefForNameFirst = beanJavaBeanNameFirst.missedOutsideBean.keys.first { it.fieldName == "fieldNameFirstInSrcBase" }
        assertTrue(beanJavaBeanNameFirst.missedOutsideBean[missedBeanRefForNameFirst]!!.contains(srcBaseBeanA))
    }

    private fun mockSplitModuleContextWithoutTgtBase(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns ApplicationContext(this)
            every { moduleContext } returns ModuleContext(this)
            every { srcBaseContext } returns BaseContext(this)
            every { tgtBaseContext } returns srcBaseContext
            every { toNewBase() } returns false
        }

        // 配置 moduleContext
        splitModuleContext.moduleContext.projectPath = "moduleProjectPath"
        splitModuleContext.moduleContext.moduleTemplateType = SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag

        // 配置 moduleXMLBean
        val moduleXMLBean = MockKUtil.spyBeanInfo("moduleXMLBean","com.mock.moduleXMLBean")
        moduleXMLBean.beanDependOn["fieldA"] = spyk(BeanRef("fieldA",null,moduleXMLBean)){
            every { definedInXML } returns true
            every { beanNameDefinedInXML } returns mutableSetOf("moduleBeanA","srcBaseBeanA","outsideRPCBeanA")
        }

        // 配置 moduleJavaBean
        // byType
        val moduleJavaBeanByType = MockKUtil.spyBeanInfo("moduleJavaBeanByType","com.mock.moduleJavaBeanByType")
        moduleJavaBeanByType.beanDependOn["fieldByTypeInModule"] = spyk(BeanRef("fieldByTypeInModule","com.mock.moduleBeanA",moduleJavaBeanByType)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.moduleBeanA"
            every { beanNameToParse } returns "moduleBeanA"
        }
        moduleJavaBeanByType.beanDependOn["fieldByTypeInSrcBase"] = spyk(BeanRef("fieldByTypeInSrcBase","com.mock.srcBaseBeanA",moduleJavaBeanByType)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.srcBaseBeanA"
            every { beanNameToParse } returns "srcBaseBeanA"
        }
        moduleJavaBeanByType.beanDependOn["fieldByTypeInOutsideRPC"] = mockk<BeanRef>{
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.outsideRPCInterfaceB"
            every { beanNameToParse } returns "outsideRPCBeanB"
        }

        // byName
        val moduleJavaBeanByName = MockKUtil.spyBeanInfo("moduleJavaBeanByName","com.mock.moduleJavaBeanByName")
        moduleJavaBeanByName.beanDependOn["fieldByNameInModule"] = spyk(BeanRef("fieldByNameInModule","com.mock.moduleBeanA",moduleJavaBeanByName)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.moduleBeanA"
            every { beanNameToParse } returns "moduleBeanA"
        }
        moduleJavaBeanByName.beanDependOn["fieldByNameInSrcBase"] = spyk(BeanRef("fieldByNameInSrcBase","com.mock.srcBaseBeanA",moduleJavaBeanByName)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.srcBaseBeanA"
            every { beanNameToParse } returns "srcBaseBeanA"
        }
        moduleJavaBeanByName.beanDependOn["fieldByNameInOutsideRPC"] = mockk<BeanRef>{
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_NAME
            every { beanNameToParse } returns "outsideRPCBeanC"
        }

        // TypeFirst
        val moduleJavaBeanTypeFirst = MockKUtil.spyBeanInfo("moduleJavaBeanTypeFirst","com.mock.moduleJavaBeanTypeFirst")
        moduleJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInModule"] = spyk(BeanRef("fieldTypeFirstInModule","com.mock.moduleBeanA",moduleJavaBeanTypeFirst)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.TYPE_FIRST
            every { beanTypeToParse } returns "com.mock.moduleBeanA"
            every { beanNameToParse } returns "moduleBeanA"
        }
        moduleJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInSrcBase"] = spyk(BeanRef("fieldTypeFirstInSrcBase","com.mock.srcBaseBeanA",moduleJavaBeanTypeFirst)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.TYPE_FIRST
            every { beanTypeToParse } returns "com.mock.srcBaseBeanA"
            every { beanNameToParse } returns "srcBaseBeanA"
        }
        moduleJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInOutsideRPC"] = mockk<BeanRef>{
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.TYPE_FIRST
            every { beanTypeToParse } returns "com.mock.outsideRPCInterfaceD"
            every { beanNameToParse } returns "outsideRPCBeanD"
        }

        // NameFirst
        val moduleJavaBeanNameFirst = MockKUtil.spyBeanInfo("moduleJavaBeanNameFirst","com.mock.moduleJavaBeanNameFirst")
        moduleJavaBeanNameFirst.beanDependOn["fieldNameFirstInModule"] = spyk(BeanRef("fieldNameFirstInModule","com.mock.moduleBeanA",moduleJavaBeanNameFirst)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.NAME_FIRST
            every { beanTypeToParse } returns "com.mock.moduleBeanA"
            every { beanNameToParse } returns "moduleBeanA"
        }
        moduleJavaBeanNameFirst.beanDependOn["fieldNameFirstInSrcBase"] = spyk(BeanRef("fieldNameFirstInSrcBase","com.mock.srcBaseBeanA",moduleJavaBeanNameFirst)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.NAME_FIRST
            every { beanTypeToParse } returns "com.mock.srcBaseBeanA"
            every { beanNameToParse } returns "srcBaseBeanA"
        }
        moduleJavaBeanNameFirst.beanDependOn["fieldNameFirstInOutsideRPC"] = mockk<BeanRef>{
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.NAME_FIRST
            every { beanTypeToParse } returns "com.mock.outsideRPCInterfaceE"
            every { beanNameToParse } returns "outsideRPCBeanE"
        }


        splitModuleContext.srcBaseContext.beanContext.addBeanInfo(MockKUtil.spyBeanInfo("srcBaseBeanA","com.mock.srcBaseBeanA"))
        splitModuleContext.moduleContext.beanContext.addBeanInfo(MockKUtil.spyBeanInfo("moduleBeanA","com.mock.moduleBeanA"))
        splitModuleContext.moduleContext.beanContext.addBeanInfo(moduleXMLBean)
        splitModuleContext.moduleContext.beanContext.addBeanInfo(moduleJavaBeanByType)
        splitModuleContext.moduleContext.beanContext.addBeanInfo(moduleJavaBeanByName)
        splitModuleContext.moduleContext.beanContext.addBeanInfo(moduleJavaBeanTypeFirst)
        splitModuleContext.moduleContext.beanContext.addBeanInfo(moduleJavaBeanNameFirst)

        return splitModuleContext
    }
}
