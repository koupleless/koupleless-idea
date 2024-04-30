package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.analyse.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.split.SplitBeanService
import com.intellij.openapi.project.Project

/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/7 17:10
 */
class AnalyseStage(proj: Project): PipelineStage(proj) {

    override fun initStage(splitModuleContext: SplitModuleContext) {
        // 分析类依赖
        this.addService(AnalyseReferClassService(proj))

        // 扫描模块和基座中：默认Bean和Mybatis在xml中的配置
        this.addService(ScanDefaultBeanService(proj))
            .addService(ScanConfigInXmlService(proj))

        // 分割模块和基座的 Mapper Bean
        this.addService(SplitBeanService(proj))

        // 分析需要添加的配置模块
        this.addService(AnalyseToAddConfigService(proj))

        // 分析模块依赖的rpc服务
        this.addService(AnalyseModuleXMLNodeService(proj))

        // 分析Bean依赖和Bean调用
        this.addService(AnalyseBeanDependencyService(proj))
    }

    override fun getName(): String {
        return "分析阶段"
    }
}
