package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse.AnalyseSrcBaseBeanDependencyPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:12
 */
class AnalyseSrcBaseBeanDependencyPluginTest {
    @Test
    fun testDoProcess(){
        val splitModuleContext = mockSplitModuleContext()
        AnalyseSrcBaseBeanDependencyPlugin.doProcess(splitModuleContext)

        val beanContext = splitModuleContext.srcBaseContext.beanContext
        val srcBaseBeanA = beanContext.getBeanByName("srcBaseBeanA")!!
        val moduleBeanA = splitModuleContext.moduleContext.beanContext.getBeanByName("moduleBeanA")!!

        // 验证 xmlBean
        val beanXMLBean = beanContext.getBeanByName("srcBaseXMLBean")!!
        assertTrue(beanXMLBean.beanDependOn["fieldA"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(beanXMLBean.beanDependOn["fieldA"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanXMLBean))
        assertTrue(moduleBeanA.beanDependBy.contains(beanXMLBean))
        assertTrue(beanContext.missedOutsideBean.contains(beanXMLBean))
        val missedBeanRefForXMLBean = beanXMLBean.missedOutsideBean.keys.first { it.fieldName == "fieldA" }
        assertTrue(beanXMLBean.missedOutsideBean[missedBeanRefForXMLBean]!!.contains(moduleBeanA))

        // 验证 byType
        val beanJavaBeanByType = beanContext.getBeanByName("srcBaseJavaBeanByType")!!
        assertTrue(beanJavaBeanByType.beanDependOn["fieldByTypeInModule"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanJavaBeanByType.beanDependOn["fieldByTypeInSrcBase"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanJavaBeanByType))
        assertTrue(moduleBeanA.beanDependBy.contains(beanJavaBeanByType))
        assertTrue(beanContext.missedOutsideBean.contains(beanJavaBeanByType))
        val missedBeanForByType = beanJavaBeanByType.missedOutsideBean.keys.first { it.fieldName == "fieldByTypeInModule" }
        assertTrue(beanJavaBeanByType.missedOutsideBean[missedBeanForByType]!!.contains(moduleBeanA))


        // 验证 byName
        val beanJavaBeanByName = beanContext.getBeanByName("srcBaseJavaBeanByName")!!
        assertTrue(beanJavaBeanByName.beanDependOn["fieldByNameInModule"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanJavaBeanByName.beanDependOn["fieldByNameInSrcBase"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanJavaBeanByName))
        assertTrue(moduleBeanA.beanDependBy.contains(beanJavaBeanByName))
        assertTrue(beanContext.missedOutsideBean.contains(beanJavaBeanByName))
        val missedBeanForByName = beanJavaBeanByName.missedOutsideBean.keys.first { it.fieldName == "fieldByNameInModule" }
        assertTrue(beanJavaBeanByName.missedOutsideBean[missedBeanForByName]!!.contains(moduleBeanA))

        // 验证 TypeFirst
        val beanJavaBeanTypeFirst = beanContext.getBeanByName("srcBaseJavaBeanTypeFirst")!!
        assertTrue(beanJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInModule"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInSrcBase"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanJavaBeanTypeFirst))
        assertTrue(moduleBeanA.beanDependBy.contains(beanJavaBeanTypeFirst))
        assertTrue(beanContext.missedOutsideBean.contains(beanJavaBeanTypeFirst))
        val missedBeanForTypeFirst = beanJavaBeanTypeFirst.missedOutsideBean.keys.first { it.fieldName == "fieldTypeFirstInModule" }
        assertTrue(beanJavaBeanTypeFirst.missedOutsideBean[missedBeanForTypeFirst]!!.contains(moduleBeanA))

        // 验证 NameFirst
        val beanJavaBeanNameFirst = beanContext.getBeanByName("srcBaseJavaBeanNameFirst")!!
        assertTrue(beanJavaBeanNameFirst.beanDependOn["fieldNameFirstInModule"]!!.parsedRef.contains(moduleBeanA))
        assertTrue(beanJavaBeanNameFirst.beanDependOn["fieldNameFirstInSrcBase"]!!.parsedRef.contains(srcBaseBeanA))
        assertTrue(srcBaseBeanA.beanDependBy.contains(beanJavaBeanNameFirst))
        assertTrue(moduleBeanA.beanDependBy.contains(beanJavaBeanNameFirst))
        assertTrue(beanContext.missedOutsideBean.contains(beanJavaBeanNameFirst))
        val missedBeanForNameFirst = beanJavaBeanNameFirst.missedOutsideBean.keys.first { it.fieldName == "fieldNameFirstInModule" }
        assertTrue(beanJavaBeanNameFirst.missedOutsideBean[missedBeanForNameFirst]!!.contains(moduleBeanA))
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns ApplicationContext(this)
            every { moduleContext } returns ModuleContext(this)
            every { srcBaseContext } returns BaseContext(this)
            every { tgtBaseContext } returns srcBaseContext
            every { toNewBase() } returns false
        }

        // 配置 srcBaseXMLBean
        val srcBaseXMLBean = MockKUtil.spyBeanInfo("srcBaseXMLBean","com.mock.srcBaseXMLBean")
        srcBaseXMLBean.beanDependOn["fieldA"] = spyk(BeanRef("fieldA",null,srcBaseXMLBean)){
            every { definedInXML } returns true
            every { beanNameDefinedInXML } returns mutableSetOf("moduleBeanA","srcBaseBeanA","outsideRPCBeanA")
        }

        // 配置 srcBaseJavaBean
        // byType
        val srcBaseJavaBeanByType = MockKUtil.spyBeanInfo("srcBaseJavaBeanByType","com.mock.srcBaseJavaBeanByType")
        srcBaseJavaBeanByType.beanDependOn["fieldByTypeInModule"] = spyk(BeanRef("fieldByTypeInModule","com.mock.moduleBeanA",srcBaseJavaBeanByType)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.moduleBeanA"
            every { beanNameToParse } returns "moduleBeanA"
        }
        srcBaseJavaBeanByType.beanDependOn["fieldByTypeInSrcBase"] = spyk(BeanRef("fieldByTypeInSrcBase","com.mock.srcBaseBeanA",srcBaseJavaBeanByType)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.srcBaseBeanA"
            every { beanNameToParse } returns "srcBaseBeanA"
        }
        srcBaseJavaBeanByType.beanDependOn["fieldByTypeInOutsideRPC"] = mockk<BeanRef>{
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.outsideRPCInterfaceB"
            every { beanNameToParse } returns "outsideRPCBeanB"
        }

        // byName
        val srcBaseJavaBeanByName = MockKUtil.spyBeanInfo("srcBaseJavaBeanByName","com.mock.srcBaseJavaBeanByName")
        srcBaseJavaBeanByName.beanDependOn["fieldByNameInModule"] = spyk(BeanRef("fieldByNameInModule","com.mock.moduleBeanA",srcBaseJavaBeanByName)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.moduleBeanA"
            every { beanNameToParse } returns "moduleBeanA"
        }
        srcBaseJavaBeanByName.beanDependOn["fieldByNameInSrcBase"] = spyk(BeanRef("fieldByNameInSrcBase","com.mock.srcBaseBeanA",srcBaseJavaBeanByName)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_TYPE
            every { beanTypeToParse } returns "com.mock.srcBaseBeanA"
            every { beanNameToParse } returns "srcBaseBeanA"
        }
        srcBaseJavaBeanByName.beanDependOn["fieldByNameInOutsideRPC"] = mockk<BeanRef>{
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.BY_NAME
            every { beanNameToParse } returns "outsideRPCBeanC"
        }

        // TypeFirst
        val srcBaseJavaBeanTypeFirst = MockKUtil.spyBeanInfo("srcBaseJavaBeanTypeFirst","com.mock.srcBaseJavaBeanTypeFirst")
        srcBaseJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInModule"] = spyk(BeanRef("fieldTypeFirstInModule","com.mock.moduleBeanA",srcBaseJavaBeanTypeFirst)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.TYPE_FIRST
            every { beanTypeToParse } returns "com.mock.moduleBeanA"
            every { beanNameToParse } returns "moduleBeanA"
        }
        srcBaseJavaBeanTypeFirst.beanDependOn["fieldTypeFirstInSrcBase"] = spyk(BeanRef("fieldTypeFirstInSrcBase","com.mock.srcBaseBeanA",srcBaseJavaBeanTypeFirst)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.TYPE_FIRST
            every { beanTypeToParse } returns "com.mock.srcBaseBeanA"
            every { beanNameToParse } returns "srcBaseBeanA"
        }
        srcBaseJavaBeanByType.beanDependOn["fieldTypeFirstInOutsideRPC"] = mockk<BeanRef>{
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.TYPE_FIRST
            every { beanTypeToParse } returns "com.mock.outsideRPCInterfaceD"
            every { beanNameToParse } returns "outsideRPCBeanD"
        }

        // NameFirst
        val srcBaseJavaBeanNameFirst = MockKUtil.spyBeanInfo("srcBaseJavaBeanNameFirst","com.mock.srcBaseJavaBeanNameFirst")
        srcBaseJavaBeanNameFirst.beanDependOn["fieldNameFirstInModule"] = spyk(BeanRef("fieldNameFirstInModule","com.mock.moduleBeanA",srcBaseJavaBeanNameFirst)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.NAME_FIRST
            every { beanTypeToParse } returns "com.mock.moduleBeanA"
            every { beanNameToParse } returns "moduleBeanA"
        }
        srcBaseJavaBeanNameFirst.beanDependOn["fieldNameFirstInSrcBase"] = spyk(BeanRef("fieldNameFirstInSrcBase","com.mock.srcBaseBeanA",srcBaseJavaBeanNameFirst)){
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.NAME_FIRST
            every { beanTypeToParse } returns "com.mock.srcBaseBeanA"
            every { beanNameToParse } returns "srcBaseBeanA"
        }
        srcBaseJavaBeanByType.beanDependOn["fieldNameFirstInOutsideRPC"] = mockk<BeanRef>{
            every { definedInXML } returns false
            every { autowire }  returns BeanRef.AutowiredMode.NAME_FIRST
            every { beanTypeToParse } returns "com.mock.outsideRPCInterfaceE"
            every { beanNameToParse } returns "outsideRPCBeanE"
        }


        splitModuleContext.srcBaseContext.beanContext.addBeanInfo(MockKUtil.spyBeanInfo("srcBaseBeanA","com.mock.srcBaseBeanA"))
        splitModuleContext.moduleContext.beanContext.addBeanInfo(MockKUtil.spyBeanInfo("moduleBeanA","com.mock.moduleBeanA"))
        splitModuleContext.srcBaseContext.beanContext.addBeanInfo(srcBaseXMLBean)
        splitModuleContext.srcBaseContext.beanContext.addBeanInfo(srcBaseJavaBeanByType)
        splitModuleContext.srcBaseContext.beanContext.addBeanInfo(srcBaseJavaBeanByName)
        splitModuleContext.srcBaseContext.beanContext.addBeanInfo(srcBaseJavaBeanTypeFirst)
        splitModuleContext.srcBaseContext.beanContext.addBeanInfo(srcBaseJavaBeanNameFirst)

        return splitModuleContext
    }
}
