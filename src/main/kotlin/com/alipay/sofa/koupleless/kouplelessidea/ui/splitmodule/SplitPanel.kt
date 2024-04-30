package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule


import com.alipay.sofa.koupleless.kouplelessidea.model.OpenHelpType
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.SplitModuleVerticallyService
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JSplitPane
import javax.swing.JTextPane
import javax.swing.SwingConstants

/**
 *
 * @author lipeng
 * @version : SplitPanel, v 0.1 2024-04-22 21:47 lipeng Exp $
 */
class SplitPanel (private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val centerPanel = BorderLayoutPanel()
    // 模版信息面板
    private val moduleDescriptionPanel = ModuleDescriptionPanel(project,getHelpLabel())

    private val viewPane = JSplitPane()

    // 模版视图
    private val moduleViewPanel = ModuleViewPanel(project)

    // 依赖视图
    private val dependencyViewPanel = DependencyViewPanel(project)

    // 阶段面板
    private val splitStagePanel =  SplitStagePanel()

    // 提示面板
    private val tipPanel = BorderLayoutPanel()
    private val tipPane = JTextPane()

    // 拆分上下文
    private val splitModuleContext: SplitModuleContext = SplitModuleContext(project)

    // 记录分析线程和拆分线程
    private var analyseThread:Thread?=null
    private var splitThread:Thread?=null

    init {
        initView()
    }

    private fun initView(){
        initModuleDescriptionPanel()
        initViewPane()
        initSplitStagePanel()
        initTipPanel()

        centerPanel.addToTop(BorderLayoutPanel().addToTop(moduleDescriptionPanel))
        centerPanel.addToCenter(viewPane)
        centerPanel.addToBottom(BorderLayoutPanel().addToTop(splitStagePanel).addToBottom(tipPanel))
        setContent(centerPanel)
    }

    private fun initViewPane(){
        initModuleViewPanel()
        initDependencyViewPanel()
        viewPane.leftComponent = moduleViewPanel
        viewPane.rightComponent = dependencyViewPanel
        viewPane.isEnabled = false
        viewPane.dividerSize = 0
        viewPane.isVisible = false
    }

    private fun initDependencyViewPanel() {
        dependencyViewPanel.addActivateListener{
            // 清理当前上下文
            splitModuleContext.clearContext()

            // 更新上下文
            updateSplitContext()

            // 激活
            activateToAnalyse()
            dependencyViewPanel.activating()
        }

        dependencyViewPanel.addAnalyseListener{
            val selectedFile = dependencyViewPanel.getCurrentFile()
            selectedFile?.let {
                // 更新模块当前文件
                updateModuleFiles()

                val dependencies = SplitModuleVerticallyService.analyseDependency(splitModuleContext,it)
                dependencyViewPanel.showDependencies(it,dependencies)
            }
        }

        dependencyViewPanel.addRefreshListener{
            // 更新模块当前文件
            updateModuleFiles()

            // 更新依赖关系
            SplitModuleVerticallyService.updateDependency(splitModuleContext,dependencyViewPanel.getAllDependNodes())

            // 更新视图
            dependencyViewPanel.refresh()
        }

        dependencyViewPanel.isVisible = false
    }

    private fun updateModuleFiles() {
        val root = moduleViewPanel.getModuleTreeRoot()
        root?.let {
            splitModuleContext.moduleContext.initRoot(root)
        }
    }

    private fun activateToAnalyse(): Thread{
        return SplitModuleVerticallyService.activateDependencyAnalyse(splitModuleContext)
    }

    private fun initModuleDescriptionPanel() {
        moduleDescriptionPanel.addConfirmActionListener{
            updateSplitContext()
            showModuleView()
            showDependencyView()
            showViewPane()
            showSplitStagePanel()
        }

        moduleDescriptionPanel.addResetActionListener{
            splitModuleContext.reset()
            splitStagePanel.toCheckStage()
            resetModuleTree()
        }
    }

    private fun showViewPane() {
        viewPane.isVisible = true
        viewPane.isEnabled = true
        viewPane.dividerSize = 2
        viewPane.setDividerLocation(0.5)
    }

    private fun showDependencyView() {
        dependencyViewPanel.reset()
        dependencyViewPanel.isVisible = true
        dependencyViewPanel.enableAll()
    }

    private fun showSplitStagePanel() {
        splitStagePanel.isVisible = true

        val baseSelected = moduleDescriptionPanel.getBaseSelectedItem()
        if(baseSelected is ModuleDescriptionPanel.LocalBaseData){
            splitStagePanel.toNewBase()
        }
    }

    private fun updateSplitContext(){
        val moduleDescriptionInfo = moduleDescriptionPanel.getModuleDescriptionInfo()
        val root = moduleViewPanel.getModuleTreeRoot()

        val configs = integrateConfigs()
        splitModuleContext.update(moduleDescriptionInfo,root,configs)
    }

    private fun integrateConfigs():Map<String,Any>{
        val res = mutableMapOf<String,Any>()

        val moduleMapperLocation = moduleDescriptionPanel.getModuleMapperLocation()
        moduleMapperLocation?.let { res[SplitConstants.MODULE_MYBATIS_MAPPER_LOCATION_CONFIG] = it }

        val moduleMybatisDir = moduleDescriptionPanel.getModuleMybatisDir()
        moduleMybatisDir?.let { res[SplitConstants.MODULE_MYBATIS_DIR_CONFIG] = it }
        return res
    }


    private fun showModuleView(){
        resetModuleTree()
        val moduleDescriptionInfo = moduleDescriptionPanel.getModuleDescriptionInfo()
        moduleViewPanel.setModuleDescriptionInfo(moduleDescriptionInfo)
        moduleViewPanel.isVisible = true
        moduleViewPanel.enableAll()
    }


    private fun initSplitStagePanel() {
        splitStagePanel.addDetectActionListener{
            moduleDescriptionPanel.resetButton.isEnabled = false
            moduleViewPanel.disableAll()
            dependencyViewPanel.disableAll()
            splitModuleContext.clearContext()
            updateSplitContext()
            analyseToSplit()
            analyseThread?.let {
                splitStagePanel.enableStopDetecting()
            }
        }

        splitStagePanel.addStopDetectingActionListener{
            printTip("正在取消任务，请等待")
            analyseThread?.let{thread->
                thread.interrupt()
                analyseThread = null
            }
        }

        splitStagePanel.addSplitActionListener{
            moduleDescriptionPanel.resetButton.isEnabled = false
            moduleViewPanel.disableAll()
            dependencyViewPanel.disableAll()
            splitModuleContext.splitMode = splitStagePanel.getMode()
            splitModuleContext.autoModify = splitStagePanel.getAutoModify()
            splitModule()
            splitStagePanel.enableStopSplitting()
        }

        splitStagePanel.addStopSplittingActionListener{
            printTip("正在取消任务，请等待")
            splitThread?.let{thread->
                thread.interrupt()
                splitThread = null
            }
        }

        splitStagePanel.isVisible = false
    }


    private fun initModuleViewPanel(){
        moduleViewPanel.addClearActionListener{
            resetModuleTree()
            splitStagePanel.toCheckStage()
        }
        moduleViewPanel.isVisible = false
    }

    private fun initTipPanel(){
        tipPane.text = "请打开下侧面板中 KouplelessIDE，查看提示"
        tipPane.foreground = JBColor.YELLOW
        tipPane.isEditable = false
        tipPanel.add(tipPane)
        tipPanel.border = BorderFactory.createEmptyBorder(10,10,5,10)
    }

    private fun getHelpLabel():JBLabel{
        val helpLabel = JBLabel("使用文档",AllIcons.Xml.Browsers.Chrome, SwingConstants.LEFT)
        helpLabel.addMouseListener(object : MouseAdapter(){
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1) {
                    BrowserUtil.browse(OpenHelpType.OpenHelpModuleSplit.link)
                }
            }
        })
        return helpLabel
    }

    fun printWarning(tip:String){
        val contentPanel = project.service<ContentPanel>()
        contentPanel.printMavenErrorLog(tip)
    }

    fun printTip(tip:String){
        val contentPanel = project.service<ContentPanel>()
        contentPanel.printMavenLog(tip)
    }

    private fun resetModuleTree(){
        val moduleInfo = moduleDescriptionPanel.getModuleDescriptionInfo()
        moduleViewPanel.resetModuleTree(moduleInfo)
    }

    private fun analyseToSplit(){
        val root = moduleViewPanel.getModuleTreeRoot()!!
        if(!ModuleTreeUtil.containsUserFile(root)){
            printWarning("模块中必须含有java文件!")
            return
        }
        analyseThread = SplitModuleVerticallyService.analyse(splitModuleContext)
    }


    private fun splitModule(){
        val root = moduleViewPanel.getModuleTreeRoot()!!
        if(!ModuleTreeUtil.containsUserFile(root)){
            printWarning("模块中必须含有java文件!")
            return
        }
        splitThread = SplitModuleVerticallyService.splitToModule(splitModuleContext)
    }

    fun finishAnalysis(success:Boolean,interrupted:Boolean=false){
        if(success){
            splitStagePanel.toSplitStage()
            printTip("检测完成，请开始拆分或重置")
        }else {
            splitModuleContext.clearContext()
            splitStagePanel.toCheckStage()
            printTip("检测失败，查看提示")
        }
        if(interrupted){
            printTip("取消成功")
        }
        splitStagePanel.stopDetectingButton.isEnabled = false
        moduleViewPanel.enableAll()
        dependencyViewPanel.enableAll()
        moduleDescriptionPanel.resetButton.isEnabled = true
    }


    fun finishSplit(success: Boolean,interrupted:Boolean=false){
        if(success){
            splitStagePanel.toDeployStage()
            val modulePath = splitModuleContext.moduleContext.getModulePath()
            printTip("拆分完成，请打开拆分目录：${modulePath}，查看结果")
        }else{
            SplitModuleVerticallyService.reset(splitModuleContext)
            splitStagePanel.toCheckStage()
        }
        if(interrupted){
            printTip("取消成功")
        }
        splitStagePanel.stopSplittingButton.isEnabled = false
        moduleViewPanel.enableAll()
        dependencyViewPanel.enableAll()
        moduleDescriptionPanel.resetButton.isEnabled = true
    }

    fun finishActivateDependency(success: Boolean,interrupted:Boolean=false){
        if(success){
            printTip("激活完成，请拖拽文件分析依赖")
            dependencyViewPanel.enableAll()
            dependencyViewPanel.activated()
            return
        }else{
            splitModuleContext.clearContext()
            splitStagePanel.toCheckStage()
            printTip("激活失败，查看提示")
        }
        if(interrupted){
            printTip("取消成功")
        }
        dependencyViewPanel.enableAll()
    }
}
