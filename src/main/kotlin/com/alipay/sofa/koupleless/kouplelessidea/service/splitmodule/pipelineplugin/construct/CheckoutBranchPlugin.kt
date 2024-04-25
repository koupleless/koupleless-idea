package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.CommandUtil
import com.intellij.execution.ExecutionException


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 11:17
 */
class CheckoutBranchPlugin(private val contentPanel: ContentPanel): PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        val moduleName = splitModuleContext.moduleContext.name
        val branchName = "split_module_$moduleName"

        // 切换原应用分支
        checkoutBranch(
            splitModuleContext.srcBaseContext.projectPath,
            branchName,
            splitModuleContext
        )

        // 切换新基座分支
        if(splitModuleContext.toNewBase()){
            checkoutBranch(
                splitModuleContext.tgtBaseContext.projectPath,
                branchName,
                splitModuleContext
            )
        }
    }

    override fun getName(): String {
        return "切换分支插件"
    }

    private fun checkoutBranch(location:String,branchName:String,splitModuleContext: SplitModuleContext){
        contentPanel.printLog("切换分支：$location -> $branchName")
        val cdCmd = "cd $location"
        val recordCodeCmd = "git add .;git commit -m \"record_before_split\""
        val checkoutCmd = "git checkout -B $branchName"
        try {
            val cmd = "${cdCmd};${recordCodeCmd};${checkoutCmd}"
            CommandUtil.execSync(cmd)
        } catch (e: Exception) {
            contentPanel.printMavenErrorLog(e.stackTraceToString())

            throw ExecutionException("切换分支失败")
        }
    }
}
