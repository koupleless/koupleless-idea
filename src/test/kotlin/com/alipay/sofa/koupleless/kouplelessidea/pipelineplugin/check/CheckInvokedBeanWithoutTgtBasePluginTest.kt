package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.check

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BaseContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.check.CheckInvokedBaseBeanPlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Test


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/3/17 11:34
 */
class CheckInvokedBeanWithoutTgtBasePluginTest {
    @Test
    fun testDoProcessWithoutMissing(){
        val splitModuleContext  = mockSplitModuleContext()
        splitModuleContext.srcBaseContext.beanContext.missedOutsideBean.clear()

        CheckInvokedBaseBeanPlugin(MockKUtil.mockContentPanel()).doProcess(splitModuleContext)
    }

    @Test
    fun testDoProcessWithMissing(){
        val splitModuleContext  = mockSplitModuleContext()
        CheckInvokedBaseBeanPlugin(MockKUtil.mockContentPanel()).doProcess(splitModuleContext)
    }

    private fun mockSplitModuleContext(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { srcBaseContext } returns BaseContext(this)
        }

        // 配置 srcBaseXMLBean
        val xmlBean = MockKUtil.spyBeanInfo("srcBaseXMLBean","com.mock.srcBaseXMLBean")
        val xmlBeanRef = spyk(BeanRef("fieldA",null,xmlBean)){
            every { beanNameDefinedInXML } returns mutableSetOf("moduleBeanA")
            every { definedInMethod } returns false
        }
        val xmlBeanRefInfo = MockKUtil.spyBeanInfo("moduleBeanA","com.mock.moduleBeanA")
        xmlBean.addMissedOutsideBean(xmlBeanRef,xmlBeanRefInfo)


        // 配置 srcBaseJavaBean
        val javaBeanMethod = MockKUtil.spyBeanInfo("srcBaseJavaBeanMethod","com.mock.srcBaseJavaBeanMethod")
        val javaBeanMethodBeanRef = spyk(BeanRef(null,"com.mock.moduleBeanB",javaBeanMethod)){
            every { definedInMethod } returns true
        }
        val javaBeanMethodBeanRefInfo = MockKUtil.spyBeanInfo("moduleBeanB","com.mock.moduleBeanB")
        javaBeanMethod.addMissedOutsideBean(javaBeanMethodBeanRef,javaBeanMethodBeanRefInfo)

        // 配置 srcMapperBean
        val mapperBean = MockKUtil.spyBeanInfo("srcMapperBean","com.mock.srcMapperBean")
        val mapperBeanRef = spyk(BeanRef("fieldA",null,mapperBean)){
            every { beanNameDefinedInXML } returns mutableSetOf("moduleBeanA")
            every { definedInMethod } returns false
        }
        val mapperBeanRefInfo = MockKUtil.spyBeanInfo("moduleBeanC","com.mock.moduleBeanC")
        mapperBeanRefInfo.registerAttribute(SplitConstants.MAPPER_BEAN,true)
        mapperBeanRefInfo.interfaceTypes.add("com.mock.moduleBeanCMapper")
        mapperBeanRefInfo.filePath = "moduleBeanCMapperFilePath"
        mapperBean.addMissedOutsideBean(mapperBeanRef,mapperBeanRefInfo)

        splitModuleContext.srcBaseContext.beanContext.missedOutsideBean.add(xmlBean)
        splitModuleContext.srcBaseContext.beanContext.missedOutsideBean.add(javaBeanMethod)
        splitModuleContext.srcBaseContext.beanContext.missedOutsideBean.add(mapperBean)
        return splitModuleContext
    }
}
