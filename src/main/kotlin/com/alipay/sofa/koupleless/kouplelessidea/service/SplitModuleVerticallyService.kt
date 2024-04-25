package com.alipay.sofa.koupleless.kouplelessidea.service


import cn.hutool.core.util.RuntimeUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnOrByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.AnalyseAppDependencyService
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelinestage.*
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.ui.IDEUtil
import com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule.SplitPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEConstants
import com.intellij.execution.ExecutionException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.commons.logging.LogFactory
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/11 11:17
 */
object SplitModuleVerticallyService {

    private val LOG = LogFactory.getLog(SplitModuleVerticallyService::class.java)

    private fun getContentPanel(project: Project):ContentPanel{
        return project.service<ContentPanel>()
    }

    fun splitToModule(splitModuleContext: SplitModuleContext):Thread{
        // 先保证 contentPanel 在 Idea 的线程中初始化。因为在非 Event Dispatch Thread 中不允许初始化 UI
        ensureContentPanelCreated(splitModuleContext.project)

        val thread = Thread {
            IDEConstants.isSplitting.set(true)
            val project = splitModuleContext.project

            try {
                getContentPanel(splitModuleContext.project).printLog("开始拆分")
                Thread.currentThread().contextClassLoader = this.javaClass.classLoader

                // 记录阶段
                RecordStage(project).process(splitModuleContext)

                // 构建阶段
                ConstructStage(project).process(splitModuleContext)

                // 整合阶段 移动pom节点、整合 pom 等操作
                IntegrateStage(project).process(splitModuleContext)

                // 修改阶段 修改文件(修改+重构文件)
                ModifyStage(project).process(splitModuleContext)

                // 补充阶段
                SupplementStage(project).process(splitModuleContext)

                // 结束阶段
                EndStage(project).process(splitModuleContext)

                getContentPanel(project).printLog("拆分完成")
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishSplit(true)
            } catch (e: InterruptedException){
                IDEUtil.showErrorBalloon("用户终止任务", project)
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishSplit(success = false,interrupted = true)
            } catch (e: Exception) {
                IDEUtil.showErrorBalloon("${e.message}，请查看执行日志", project)
                val contentPanel = project.service<ContentPanel>()
                contentPanel.printMavenErrorLog("${e.message}，请查看执行日志")
                contentPanel.printMavenLog(e.stackTraceToString())
                contentPanel.printMavenErrorLog("如遇拆分问题，欢迎联系 @立蓬 反馈，感谢！")
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishSplit(false)
            } finally {
                IDEConstants.isSplitting.set(false)
            }
        }

        thread.start()
        return thread
    }

    fun analyse(splitModuleContext: SplitModuleContext):Thread{
        // 先保证 contentPanel 在 Idea 的线程中初始化。因为在非 Event Dispatch Thread 中不允许初始化 UI
        ensureContentPanelCreated(splitModuleContext.project)

        val thread = Thread{
            getContentPanel(splitModuleContext.project).printLog("开始检测")

            val old = Thread.currentThread().contextClassLoader
            val project = splitModuleContext.project
            try {
                Thread.currentThread().contextClassLoader = this.javaClass.classLoader

                InitialStage(project).process(splitModuleContext)

                AnalyseStage(project).process(splitModuleContext)

                getContentPanel(splitModuleContext.project).printLog("检测完成")
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishAnalysis(true)
            } catch (e: InterruptedException){
                IDEUtil.showErrorBalloon("用户终止任务", project)
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishAnalysis(success = false,interrupted = true)
            }
            catch (e: Exception) {
                val contentPanel = project.service<ContentPanel>()
                IDEUtil.showErrorBalloon("${e.message}，请查看执行日志", project)
                contentPanel.printMavenErrorLog("${e.message}，请查看执行日志")
                contentPanel.printMavenErrorLog(e.stackTraceToString())
                contentPanel.printMavenErrorLog("如遇拆分问题，欢迎联系 @立蓬 反馈，感谢！")
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishAnalysis(false)
            }
            finally {
                IDEConstants.isSplitting.set(false)
                Thread.currentThread().contextClassLoader = old
            }
        }
        thread.start()
        return thread
    }

    fun activateDependencyAnalyse(splitModuleContext: SplitModuleContext):Thread{
        ensureContentPanelCreated(splitModuleContext.project)

        val thread = Thread{
            getContentPanel(splitModuleContext.project).printLog("开始激活")

            val old = Thread.currentThread().contextClassLoader
            val project = splitModuleContext.project
            try {
                Thread.currentThread().contextClassLoader = this.javaClass.classLoader

                InitialStage(project).process(splitModuleContext)

                ActivateStage(project).process(splitModuleContext)

                getContentPanel(splitModuleContext.project).printLog("激活完成")
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishActivateDependency(true)
            } catch (e: InterruptedException){
                IDEUtil.showErrorBalloon("用户终止任务", project)
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishActivateDependency(success = false,interrupted = true)
            }
            catch (e: Exception) {
                val contentPanel = project.service<ContentPanel>()
                IDEUtil.showErrorBalloon("${e.message}，请查看执行日志", project)
                contentPanel.printMavenErrorLog("${e.message}，请查看执行日志")
                contentPanel.printMavenErrorLog(e.stackTraceToString())
                contentPanel.printMavenErrorLog("如遇拆分问题，欢迎联系 @立蓬 反馈，感谢！")
                val splitPanel: SplitPanel = project.service<SplitPanel>()
                splitPanel.finishActivateDependency(false)
            }
            finally {
                IDEConstants.isSplitting.set(false)
                Thread.currentThread().contextClassLoader = old
            }
        }
        thread.start()
        return thread
    }

    fun analyseDependency(splitModuleContext: SplitModuleContext,file: File):List<FileDependencyTreeNode>{
        return AnalyseAppDependencyService().analyse(splitModuleContext,file)
    }

    /**
     * 保证 contentPanel 初始化。因为 UI 需要在 Event Dispatch Thread 中初始化。
     * 见：https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html?from=jetbrains.org#write-access
     * @param
     * @return
     */
    private fun ensureContentPanelCreated(project: Project){
        ApplicationManager.getApplication().invokeAndWait {
            getContentPanel(project)
        }
    }


    fun reset(splitModuleContext: SplitModuleContext){
        // 重置分支
        resetBranch(splitModuleContext)

        // 清理 splitModuleContext
        splitModuleContext.clearContext()
    }


    private fun resetBranch(splitModuleContext: SplitModuleContext){
        requireNotNull(splitModuleContext.srcBaseContext.projectPath){"${splitModuleContext.srcBaseContext.name}项目路径不能为空"}
        // 重置原应用分支
        resetBranch(splitModuleContext.srcBaseContext.projectPath,splitModuleContext)

        // 重置新基座分支
        if(splitModuleContext.toNewBase()){
            requireNotNull(splitModuleContext.tgtBaseContext.projectPath){"${splitModuleContext.tgtBaseContext.name}的项目路径不能为空"}
            resetBranch(splitModuleContext.tgtBaseContext.projectPath,splitModuleContext)
        }
    }

    private fun resetBranch(location: String,splitModuleContext: SplitModuleContext){
        val contentPanel = splitModuleContext.project.service<ContentPanel>()
        contentPanel.printLog("重置分支：$location")
        val cdCmd = "cd $location"
        // 此处不执行 git clean -xdf，因为参数 -x 会删除被ignore的文件或目录。如果删除了 .idea，用户配置的项目IDEA参数会被重置
        val resetCmd = "git checkout .;git clean -df"
        try {
            val cmd = "${cdCmd};${resetCmd}"
            RuntimeUtil.execForStr("/bin/sh","-c","-l",cmd)
        } catch (e: Exception) {
            contentPanel.printMavenErrorLog(e.stackTraceToString())

            throw ExecutionException("重置分支失败")
        }
    }

    fun updateDependency(splitModuleContext: SplitModuleContext, dependNodes: List<FileDependOnOrByTreeNode>) {
        AnalyseAppDependencyService().update(splitModuleContext,dependNodes)
    }
}
