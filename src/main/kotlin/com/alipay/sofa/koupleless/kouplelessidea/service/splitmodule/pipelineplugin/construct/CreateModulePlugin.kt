package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.ModuleService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:40
 */
class CreateModulePlugin(private val contentPanel: ContentPanel): PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        createModule(splitModuleContext)
    }

    override fun getName(): String {
        return "创建模块"
    }

    private fun createModule(splitModuleContext: SplitModuleContext) {
        val moduleName = splitModuleContext.moduleContext.name
        val appName = splitModuleContext.srcBaseContext.name
        val moduleGroupId = splitModuleContext.moduleContext.groupId
        val moduleArtifactId = splitModuleContext.moduleContext.artifactId
        val modulePackage = splitModuleContext.moduleContext.packageName
        val moduleLocation = splitModuleContext.moduleContext.moduleLocation
        val moduleTemplate = splitModuleContext.moduleContext.moduleTemplate
        val moduleType = splitModuleContext.moduleContext.type

        val process = if (splitModuleContext.moduleContext.isMono){
            val uploadMetaInfo = false
            ModuleService.createModule(splitModuleContext.project,appName,moduleName,moduleType,modulePackage= modulePackage,moduleGroupId = moduleGroupId,moduleArtifactId= moduleArtifactId,moduleTemplate?.archetypeGroupId,moduleTemplate?.archetypeArtifactId,moduleTemplate?.archetypeVersion,uploadMetaInfo)
        } else{
            ModuleService.createIndependentModule(
                splitModuleContext.project,
                appName,
                moduleName,
                modulePackage = modulePackage,
                moduleGroupId = moduleGroupId,
                moduleArtifactId = moduleArtifactId,
                moduleTemplate?.archetypeGroupId,
                moduleTemplate?.archetypeArtifactId,
                moduleTemplate?.archetypeVersion,
                moduleLocation!!
            )
        }

        if (0 != process!!.waitFor()) {
            throw RuntimeException("创建模块失败")
        }
        contentPanel.printLog("创建模块 $moduleName 成功")
    }
}
