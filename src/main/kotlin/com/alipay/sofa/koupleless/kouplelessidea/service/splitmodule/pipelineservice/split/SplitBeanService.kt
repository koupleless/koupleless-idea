package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.split

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.split.SplitMapperBeanPlugin
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/5 14:44
 */
class SplitBeanService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        this.addPlugin(SplitMapperBeanPlugin)
    }

    override fun getName(): String {
        return "分割模块和基座的 Bean 服务"
    }
}
