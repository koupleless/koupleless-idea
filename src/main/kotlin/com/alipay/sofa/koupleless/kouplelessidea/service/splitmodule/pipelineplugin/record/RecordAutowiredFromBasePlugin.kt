package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.record

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.alipay.sofa.koupleless.kouplelessidea.parser.BuildJavaService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.ImportDeclaration


/**
 * @description: 记录AutowiredFromBase：检测到模块调用基座bean后，把跨上下文调用 bean 的方式从 autowired 换成 autowiredFromBase
 * @author lipeng
 * @date 2024/5/7 16:21
 */
class RecordAutowiredFromBasePlugin(private val contentPanel: ContentPanel): PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        recordAutowiredFromBase(splitModuleContext)
    }

    override fun getName(): String {
        return "记录 AutowiredFromBase 插件"
    }

    /**
     * 为模块调用基座Bean做记录；注意，插件只会自动修改模块调用基座Bean的方式，不会修改基座调用模块Bean的方式，
     * @param
     * @return
     */
    private fun recordAutowiredFromBase(splitModuleContext: SplitModuleContext) {
        val moduleContext = splitModuleContext.moduleContext
        val modifyContext = splitModuleContext.modifyStageContext.modifyContext
        val moveContext = splitModuleContext.integrationStageContext.integrateContext
        val beanContextInModule = moduleContext.beanContext
        beanContextInModule.missedOutsideBean.forEach { beanInfoInModule ->
            beanInfoInModule.missedOutsideBean.forEach { (beanRef, beanRefInfoSet) ->
                markAsAutowiredFromBase(beanInfoInModule, beanRef, beanRefInfoSet,moduleContext,modifyContext,moveContext)
            }
        }
    }

    private fun markAsAutowiredFromBase(beanInfo: BeanInfo, beanRef: BeanRef, beanRefInfoSet: Set<BeanInfo>, moduleContext: ModuleContext, modifyContext: ModifyContext, moveContext:ModifyContext){
        if(beanRef.fieldName==null){
            contentPanel.printMavenErrorLog("无法创建 AutowiredFromBase，因为${beanRef.beanNameToParse} 不是 ${beanRef.parentBean.fullClassName} 的字段")
            return
        }
        if(beanRefInfoSet.isEmpty()){
            contentPanel.printMavenErrorLog("无法创建 AutowiredFromBase，因为没有找到使用 beanId =  ${beanRef.beanNameToParse} 的类")
            return
        }

        // 给该 field 删除原来的注解
        val filePath = beanInfo.filePath
        modifyContext.removeFieldAnnotation(filePath,beanRef.fieldName, SplitConstants.AUTOWIRED_ANNOTATIONS)
        // 给该 field 添加 sofaReference 注解
        val sofaReferenceAnno = BuildJavaService.buildAutowiredFromBaseAnno(beanRefInfoSet.first())
        modifyContext.addFieldAnnotation(filePath,beanRef.fieldName,sofaReferenceAnno)
        // 新增 import
        val sofaReferenceImport = ImportDeclaration("com.alipay.sofa.koupleless.common.api.AutowiredFromBase",false,false)
        modifyContext.addImport(filePath,sofaReferenceImport)
    }
}
