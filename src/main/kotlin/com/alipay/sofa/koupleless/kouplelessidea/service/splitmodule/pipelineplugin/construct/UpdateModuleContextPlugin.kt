package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEConstants
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 12:00
 */
object UpdateModuleContextPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val moduleContext = splitModuleContext.moduleContext

        // 更新 ClassInfo
        updateClassInfoContext(moduleContext)

        // 最后更新文件路径
        updateFilesPath(moduleContext)
    }

    override fun getName(): String {
        return "更新模块上下文插件"
    }

    /**
     * 更新 ClassInfo 的信息：仅更新了 classPathToClassInfo，没有更新 classNameToClassInfo
     * @param
     * @return
     */
    private fun updateClassInfoContext(moduleContext: ModuleContext) {
        val srcPathToTgtPath = moduleContext.getSrcPathToTgtPath()
        srcPathToTgtPath.forEach { (srcPath, tgtPath) ->
            moduleContext.classInfoContext.updateClassInfo(srcPath,tgtPath)
        }
    }

    private fun updateFilesPath(moduleContext: ModuleContext) {
        moduleContext.files.clear()
        val moduleRoot =  File(StrUtil.join(FileUtil.FILE_SEPARATOR,moduleContext.moduleLocation,moduleContext.name))
        moduleRoot.walk().filterTo(moduleContext.files) { it.isFile && !it.isHidden}
    }
}
