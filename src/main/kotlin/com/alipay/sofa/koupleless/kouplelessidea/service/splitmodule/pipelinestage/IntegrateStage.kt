package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.integrate.IntegrateSingleBundlePomService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.integrate.MoveSegmentService
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:07
 */
class IntegrateStage(proj: Project): PipelineStage(proj) {
    override fun initStage(splitModuleContext: SplitModuleContext) {
        // 移动部分文件
        this.addService(MoveSegmentService(proj))

        // 整合Pom
        when(splitModuleContext.moduleContext.moduleTemplateType){
            SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag ->{
                this.addService(IntegrateSingleBundlePomService(proj))
            }
        }
    }

    override fun getName(): String {
        return "整合阶段"
    }

    override fun checkPreCondition(splitModuleContext: SplitModuleContext): Boolean {
        return splitModuleContext.autoModify
    }
}
