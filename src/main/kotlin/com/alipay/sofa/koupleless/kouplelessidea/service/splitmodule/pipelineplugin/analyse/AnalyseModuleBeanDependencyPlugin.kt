package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.AnalyseBeanUtil


/**
 * @description: 分析模块 Bean 依赖
 * @author lipeng
 * @date 2023/11/8 14:01
 */
object AnalyseModuleBeanDependencyPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        if(splitModuleContext.toNewBase()){
            analyseWithTgtBase(splitModuleContext)
        }else{
            analyseWithoutTgtBase(splitModuleContext)
        }
    }

    private fun analyseWithTgtBase(splitModuleContext: SplitModuleContext){
        val moduleContext = splitModuleContext.moduleContext
        val beanContext = moduleContext.beanContext
        val srcBaseBeanContext = splitModuleContext.srcBaseContext.beanContext
        val tgtBaseBeanContext = splitModuleContext.tgtBaseContext.beanContext

        // 按照beanId去解析tgtBase中的bean是否被模块bean引用
        val supplementStageContext = splitModuleContext.supplementStageContext
        val analyseTgtBeanByNameOnly = (supplementStageContext.getConfig(SplitConstants.ANALYSE_MODULE_BEAN_DEPENDENCY_WITH_TGT_BASE_BY_NAME_ONLY_PATTEN)?:false) as Boolean

        beanContext.allBeanInfo.forEach {beanInfo->
            beanInfo.beanDependOn.forEach { (_, beanRef) ->
                if(beanRef.definedInXML){
                    analyseBeanRefInXML(beanRef,beanContext,tgtBaseBeanContext,srcBaseBeanContext,analyseTgtBeanByNameOnly)
                }else{
                    analyseBeanRefInJava(beanRef,beanContext,tgtBaseBeanContext,srcBaseBeanContext,analyseTgtBeanByNameOnly)
                }
            }
        }
    }

    private fun analyseWithoutTgtBase(splitModuleContext: SplitModuleContext){
        val moduleContext = splitModuleContext.moduleContext
        val beanContext = moduleContext.beanContext
        val beanContextInSrcBase = splitModuleContext.srcBaseContext.beanContext

        beanContext.allBeanInfo.forEach { beanInfo ->
            beanInfo.beanDependOn.forEach { (_, beanRef) ->
                if(beanRef.definedInXML){
                    AnalyseBeanUtil.analyseBeanRefInXML(beanRef,beanContext,beanContextInSrcBase)
                }else{
                    AnalyseBeanUtil.analyseBeanRefInJava(beanRef,beanContext,beanContextInSrcBase)
                }
            }
        }
    }

    private fun analyseBeanRefInXML(beanRef: BeanRef, beanContext: BeanContext, tgtBeanContext: BeanContext, srcBeanContext: BeanContext, analyseTgtBeanByNameOnly:Boolean){
        val beanNameDefinedInXML = beanRef.beanNameDefinedInXML?:return
        beanNameDefinedInXML.forEach {name ->
            analyseBeanRefByName(name,beanRef,beanContext,tgtBeanContext,srcBeanContext,analyseTgtBeanByNameOnly)
        }
    }

    private fun analyseBeanRefByName(name:String, beanRef: BeanRef, beanContext: BeanContext, tgtBeanContext: BeanContext, srcBeanContext: BeanContext, analyseTgtBeanByNameOnly:Boolean):Boolean{
        if(beanContext.containsBeanName(name)){
            val beanRefInfo = beanContext.getBeanByName(name)!!
            beanRef.parsedAs(beanRefInfo)
            return true
        }
        if(analyseTgtBeanByNameOnly){
            if(tgtBeanContext.containsBeanName(name)){
                val targetBeanInfo = tgtBeanContext.getBeanByName(name)!!
                beanRef.parsedAs(targetBeanInfo)
                beanContext.addMissedOutsideBean(beanRef,targetBeanInfo)
                return true
            }
        }
        if(srcBeanContext.containsBeanName(name)){
            val targetBeanInfo = srcBeanContext.getBeanByName(name)!!
            beanRef.parsedAs(targetBeanInfo)
            beanContext.addMissedOutsideBeanToReport(beanRef,targetBeanInfo)
            return true
        }
        return false
    }

    private fun analyseBeanRefInJava(beanRef: BeanRef, beanContext: BeanContext, tgtBeanContext: BeanContext, srcBeanContext: BeanContext, analyseTgtBeanByNameOnly:Boolean){
        val name = beanRef.beanNameToParse?:return
        when(beanRef.autowire){
            BeanRef.AutowiredMode.BY_TYPE -> {
                analyseBeanRefByType(beanRef,beanContext,tgtBeanContext,srcBeanContext,analyseTgtBeanByNameOnly)
            }

            BeanRef.AutowiredMode.BY_NAME -> {
                analyseBeanRefByName(name,beanRef,beanContext,tgtBeanContext,srcBeanContext,analyseTgtBeanByNameOnly)
            }

            BeanRef.AutowiredMode.TYPE_FIRST -> {
                val parsed = analyseBeanRefByType(beanRef,beanContext,tgtBeanContext,srcBeanContext,analyseTgtBeanByNameOnly)
                if(!parsed){
                    analyseBeanRefByName(name,beanRef,beanContext,tgtBeanContext,srcBeanContext,analyseTgtBeanByNameOnly)
                }
            }

            BeanRef.AutowiredMode.NAME_FIRST -> {
                val parsed = analyseBeanRefByName(name,beanRef,beanContext,tgtBeanContext,srcBeanContext,analyseTgtBeanByNameOnly)
                if(!parsed){
                    analyseBeanRefByType(beanRef,beanContext,tgtBeanContext,srcBeanContext,analyseTgtBeanByNameOnly)
                }
            }

            else -> {
                // TODO:log
            }
        }

    }

    private fun analyseBeanRefByType(beanRef: BeanRef, beanContext: BeanContext, tgtBeanContext: BeanContext, srcBeanContext: BeanContext, analyseTgtBeanByNameOnly:Boolean):Boolean{
        val type = beanRef.beanTypeToParse?:return false
        if(beanContext.containsBeanType(type)){
            val beanRefInfo = beanContext.getBeanByType(type)!!
            beanRef.parsedAs(beanRefInfo)
            return true
        }
        if(analyseTgtBeanByNameOnly){
            val name = beanRef.beanNameToParse
            name?.let {
                if(tgtBeanContext.containsBeanName(it)){
                    val targetBeanInfo = tgtBeanContext.getBeanByName(it)!!
                    beanRef.parsedAs(targetBeanInfo)
                    beanContext.addMissedOutsideBean(beanRef,targetBeanInfo)
                    return true
                }
            }
        }
        if(srcBeanContext.containsBeanType(type)){
            val targetBeanInfo = srcBeanContext.getBeanByType(type)!!
            beanRef.parsedAs(targetBeanInfo)
            beanContext.addMissedOutsideBeanToReport(beanRef,targetBeanInfo)
            return true
        }
        return false
    }

    override fun getName(): String {
        return "分析模块 Bean 依赖插件"
    }
}
