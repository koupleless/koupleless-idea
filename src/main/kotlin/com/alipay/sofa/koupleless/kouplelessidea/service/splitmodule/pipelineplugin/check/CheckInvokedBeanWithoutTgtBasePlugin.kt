package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.check

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/8 14:44
 */
class CheckInvokedBeanWithoutTgtBasePlugin(private val contentPanel: ContentPanel): PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val beanContextInSrcBase = splitModuleContext.srcBaseContext.beanContext
        if(beanContextInSrcBase.missedOutsideBean.isEmpty()) return

        checkBeanInvoke(splitModuleContext)

        checkMapperInvoke(splitModuleContext)
    }

    override fun getName(): String {
        return "检查原应用调用模块插件"
    }

    private fun checkMapperInvoke(splitModuleContext: SplitModuleContext){
        val beanContextInSrcBase = splitModuleContext.srcBaseContext.beanContext
        var validMapperInvoke = true
        var tips = ""
        beanContextInSrcBase.missedOutsideBean.forEach {beanInfo ->
            val beanType = beanInfo.getClassOrInterfaceType()
            beanInfo.missedOutsideBean.forEach { (beanRef, beanRefInfoSet) ->
                val invokedMapperBean = beanRefInfoSet.filter { isMapperBean(it) }
                if(invokedMapperBean.isNotEmpty()){
                    validMapperInvoke = false
                    val mapperBeanInterfaceName = invokedMapperBean.joinToString(",","(",")"){it.interfaceTypes.first()}
                    val mapperBeanFilePath = invokedMapperBean.joinToString(",","(",")"){it.filePath}
                    tips +="基座中：$beanType 调用了模块的 $mapperBeanInterfaceName : $mapperBeanFilePath \n"
                }
            }
        }

        if(!validMapperInvoke){
            contentPanel.printErrorLog("检测到基座调用模块的 Mapper，请勿拆出该 Mapper，调用如下：")
            contentPanel.printMavenLog(tips)
        }
    }

    private fun checkBeanInvoke(splitModuleContext: SplitModuleContext){
        val beanContextInSrcBase = splitModuleContext.srcBaseContext.beanContext
        contentPanel.printErrorLog("检测到基座调用模块，请检查是否符合拆分需求，调用如下：")
        var tips = ""
        beanContextInSrcBase.missedOutsideBean.forEach { beanInfo ->
            val beanType = beanInfo.getClassOrInterfaceType()
            beanInfo.missedOutsideBean.forEach { (beanRef, beanRefInfoSet) ->
                val beanRefType = beanRefInfoSet.joinToString(",","(",")"){it.getClassOrInterfaceType()}
                tips += if(beanRef.definedInMethod){
                    "基座中：${beanType} 的方法中调用了模块的 $beanRefType \n"
                }else{
                    val fieldName = beanRef.fieldName!!
                    "基座中：${beanType} 的变量 $fieldName 调用了模块的 $beanRefType \n"
                }
            }
        }
        contentPanel.printMavenLog(tips)
    }

    private fun isMapperBean(beanInfo: BeanInfo):Boolean{
        beanInfo.getAttribute(SplitConstants.MAPPER_BEAN)?:return false
        return beanInfo.getAttribute(SplitConstants.MAPPER_BEAN) as Boolean
    }

    override fun checkPreCondition(splitModuleContext: SplitModuleContext):Boolean{
        return !splitModuleContext.toNewBase()
    }
}
