package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.analyse

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants


/**
 * @description: 分析模块 bean 依赖的 xml 节点：
 * 1. <sofa:reference> xml 节点 ：如果模块依赖的 bean 不属于基座类（原基座/新基座）对应的 Bean，那么可能以 <sofa:reference> xml 节点引入的外部 rpc bean。
 * @author lipeng
 * @date 2023/12/3 20:56
 */
object AnalyseModuleBeanDependOnXMLNodePlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        parseFromBeanDependency(splitModuleContext)
    }

    override fun getName(): String {
        return "分析模块相关的 <sofa:reference> xml 节点"
    }

    /**
     * 从 bean 依赖中解析需要的 <sofa:reference> xml rpc 节点
     * @param
     * @return
     */
    private fun parseFromBeanDependency(splitModuleContext: SplitModuleContext){
        val moduleContext = splitModuleContext.moduleContext
        val beanContext = moduleContext.beanContext
        val srcBaseBeanContext = splitModuleContext.srcBaseContext.beanContext
        val tgtBaseBeanContext = splitModuleContext.tgtBaseContext.beanContext

        // 按照beanId去解析tgtBase中的bean是否被模块bean引用
        val supplementStageContext = splitModuleContext.supplementStageContext
        val analyseTgtBeanByNameOnly = (supplementStageContext.getConfig(SplitConstants.ANALYSE_MODULE_BEAN_DEPENDENCY_WITH_TGT_BASE_BY_NAME_ONLY_PATTEN)?:false) as Boolean
        // 只考虑有 className 的 BeanInfo:
        beanContext.classNameToBeanInfo.forEach {(_,beanInfo)->
            beanInfo.beanDependOn.forEach { (_, beanRef) ->
                if(beanRef.definedInXML){
                    analyseBeanRefInXML(
                        beanRef,
                        beanContext,
                        tgtBaseBeanContext,srcBaseBeanContext,analyseTgtBeanByNameOnly,
                        splitModuleContext
                    )
                }else{
                    analyseBeanRefInJava(
                        beanRef,
                        beanContext,
                        tgtBaseBeanContext,srcBaseBeanContext,analyseTgtBeanByNameOnly,
                        splitModuleContext
                    )
                }
            }
        }
    }

    private fun analyseBeanRefInXML(beanRef: BeanRef, moduleBeanContext: BeanContext, tgtBeanContext: BeanContext, srcBeanContext: BeanContext, analyseTgtBeanByNameOnly:Boolean, splitModuleContext: SplitModuleContext) {
        val beanNameDefinedInXML = beanRef.beanNameDefinedInXML?:return
        val outBeanContext  = if(analyseTgtBeanByNameOnly){
            listOf(tgtBeanContext,srcBeanContext)
        }else{
            listOf(srcBeanContext)
        }

        beanNameDefinedInXML.forEach {name ->
            analyseBeanRefByName(
                name,
                beanRef,
                moduleBeanContext,
                outBeanContext,
                splitModuleContext
            )
        }
    }

    private fun analyseBeanRefByName(
        name: String,
        beanRef: BeanRef,
        moduleBeanContext: BeanContext,
        referBeanContext: List<BeanContext>,
        splitModuleContext: SplitModuleContext
    ): Boolean {
        if (moduleBeanContext.containsBeanName(name)) return true

        return referBeanContext.any { it.containsBeanName(name) }
    }

    private fun analyseBeanRefByType(beanRef: BeanRef, moduleBeanContext:BeanContext, tgtBeanContext: BeanContext, srcBeanContext: BeanContext, analyseTgtBeanByNameOnly:Boolean, splitModuleContext: SplitModuleContext):Boolean {
        val type = beanRef.beanTypeToParse?:return false
        if(moduleBeanContext.containsBeanType(type)) return true

        if(analyseTgtBeanByNameOnly){
            val name = beanRef.beanNameToParse
            name?.let {
                if(tgtBeanContext.containsBeanName(it)) return true
            }
        }

        return srcBeanContext.containsBeanType(type)
    }

    /**
     * 如果 beanRef 没有 beanName 则不会被解析
     * @param
     * @return
     */
    private fun analyseBeanRefInJava(beanRef: BeanRef, moduleBeanContext: BeanContext, tgtBeanContext: BeanContext, srcBeanContext: BeanContext, analyseTgtBeanByNameOnly:Boolean, splitModuleContext: SplitModuleContext) {
        val name = beanRef.beanNameToParse?:return
        val outBeanContext  = if(analyseTgtBeanByNameOnly){
            listOf(tgtBeanContext,srcBeanContext)
        }else{
            listOf(srcBeanContext)
        }
        when(beanRef.autowire){
            BeanRef.AutowiredMode.BY_TYPE -> {
                analyseBeanRefByType(
                    beanRef,
                    moduleBeanContext,
                    tgtBeanContext,
                    srcBeanContext,
                    analyseTgtBeanByNameOnly,
                    splitModuleContext
                )
            }

            BeanRef.AutowiredMode.BY_NAME -> {
                analyseBeanRefByName(
                    name,
                    beanRef,
                    moduleBeanContext,
                    outBeanContext,
                    splitModuleContext
                )
            }

            BeanRef.AutowiredMode.TYPE_FIRST -> {
                val parsedSuccess = analyseBeanRefByType(
                    beanRef,
                    moduleBeanContext,
                    tgtBeanContext,
                    srcBeanContext,
                    analyseTgtBeanByNameOnly,
                    splitModuleContext
                )
                analyseBeanRefByName(
                    name,
                    beanRef,
                    moduleBeanContext,
                    outBeanContext,
                    splitModuleContext
                )
            }

            BeanRef.AutowiredMode.NAME_FIRST -> {
                val parsedSuccess = analyseBeanRefByName(
                    name,
                    beanRef,
                    moduleBeanContext,
                    outBeanContext,
                    splitModuleContext
                )
                if(!parsedSuccess) {
                    analyseBeanRefByType(
                        beanRef,
                        moduleBeanContext,
                        tgtBeanContext,
                        srcBeanContext,
                        analyseTgtBeanByNameOnly,
                        splitModuleContext
                    )
                }
            }

            else -> {
                // TODO:log
            }
        }

    }
}
