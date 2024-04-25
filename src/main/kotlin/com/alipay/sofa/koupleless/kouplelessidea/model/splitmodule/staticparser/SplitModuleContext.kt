package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.ModuleDescriptionInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.InitStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.IntegrationStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.ModifyStageContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.pipeline.SupplementStageContext
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.intellij.openapi.project.Project


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/8/24 14:02
 */
class SplitModuleContext(val project: Project) {
    val appContext:ApplicationContext = ApplicationContext(this)
    var moduleContext =  ModuleContext(this)
    val srcBaseContext = BaseContext(this)
    var tgtBaseContext = srcBaseContext

    val initStageContext = InitStageContext(this)
    // 整合阶段：在模块创建后修改
    val integrationStageContext  = IntegrationStageContext(this)
    // 修改阶段：在模块创建前修改
    val modifyStageContext = ModifyStageContext(this)
    val supplementStageContext = SupplementStageContext(this)
    var splitMode = SplitConstants.SplitModeEnum.COPY
    var autoModify = true

    init {
        appContext.initWithProject(project)
        srcBaseContext.initWithProject(project)
    }

    fun update(moduleDescriptionInfo:ModuleDescriptionInfo,moduleRoot: FileWrapperTreeNode?,integrationConfigs:Map<String, Any>){
        srcBaseContext.update(moduleDescriptionInfo.srcBaseInfo)
        moduleContext.update(moduleDescriptionInfo, moduleRoot)

        moduleDescriptionInfo.splitToOtherBase?.let { splitToOtherBase ->
            if (splitToOtherBase && tgtBaseContext.projectPath != moduleDescriptionInfo.tgtBaseLocation) {
                tgtBaseContext = BaseContext(this)
                tgtBaseContext.updateProjectPath(moduleDescriptionInfo.tgtBaseLocation!!)
            }
        }

        integrationStageContext.updateConfigs(integrationConfigs)
    }

    fun updateSupplementConfig(map:Map<String, Any>){
        supplementStageContext.updateConfig(map)
    }

    fun reset(){
        srcBaseContext.reset()
        moduleContext.reset()
        tgtBaseContext = srcBaseContext
        appContext.reset()

        initStageContext.clear()
        modifyStageContext.clear()
        integrationStageContext.clear()
        supplementStageContext.clear()
    }

    fun toNewBase():Boolean{
        return tgtBaseContext!=srcBaseContext
    }

    fun clearContext() {
        srcBaseContext.clearContext()
        moduleContext.clearContext()
        tgtBaseContext.clearContext()
        appContext.clearContext()

        initStageContext.clear()
        integrationStageContext.clear()
        modifyStageContext.clear()
        supplementStageContext.clear()
    }
}
