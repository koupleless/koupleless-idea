package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate.*
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineservice.PipelineService
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 14:50
 */
class IntegrateSingleBundlePomService(proj: Project): PipelineService(proj) {
    override fun initService(splitModuleContext: SplitModuleContext) {
        // 1. 根据类依赖关系，配置 pom 的依赖
        this.addPlugin(ConfigSingleBundlePomDependencyPlugin)

        // 2. 独立库模式：整合原应用的 pom 的各项配置
        this.addPlugin(IntegrateSingleBundleParentPomConfigsPlugin)

        // 4. 调整依赖中的原应用的jar包
        this.addPlugin(ConfigSrcBaseDependencyInModulePomPlugin(getContentPanel()))

        // 5. 拆为原基座的模块时：把依赖都设置为provided
        this.addPlugin(ConfigDependencyProvidedPlugin)
    }

    override fun getName(): String {
        return "整合 pom 服务"
    }
}
