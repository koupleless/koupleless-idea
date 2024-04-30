package com.alipay.sofa.koupleless.kouplelessidea.ui

import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEDataKeys
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ComponentManager
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.extensions.BaseExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Color
import java.awt.Font.PLAIN


class ContentPanel(private val myProject: Project) : SimpleToolWindowPanel(true, true) {
    private val centerPanel = BorderLayoutPanel()
    private val logTabPane = JBTabbedPane()
    private val runLogView = IDEUtil.getConsoleView(myProject)
    private val executeLogView: ConsoleViewImpl = IDEUtil.getConsoleView(myProject)
    private val whiteConsoleViewContentType =
        ConsoleViewContentType(
            "Stack",
            TextAttributes(Color(179, 179, 179), null, null, EffectType.LINE_UNDERSCORE, PLAIN)
        )

    init {
        initView()
    }

    fun scrollToEnd() {
        executeLogView.scrollToEnd();
    }

    /**
     * 初始化界面信息
     */
    private fun initView() {
        initLogView()
        setContent(centerPanel)
    }

    private fun initLogView() {
        logTabPane.addTab(LogType.EXECUTE.title, executeLogView.component)
        centerPanel.addToCenter(logTabPane)
    }

    /**
     * 打印日志
     */
    fun printLog(text: String?) = text?.let {
        executeLogView.print(
            "\n${it}",
            ConsoleViewContentType(
                "Stack",
                TextAttributes(Color(165, 156, 116), null, null, EffectType.LINE_UNDERSCORE, PLAIN)
            )
        )
    }
    fun printLogNoBreak(text: String?) = text?.let {
        executeLogView.print(
            "${it}",
            ConsoleViewContentType(
                "Stack",
                TextAttributes(Color(165, 156, 116), null, null, EffectType.LINE_UNDERSCORE, PLAIN)
            )
        )
    }

    fun printMavenLog(text: String?) = text?.let { executeLogView.print("\n${it}", whiteConsoleViewContentType) }

    /**
     * 打印堆栈信息
     */
    fun printStackLog(text: String?) = text?.let {
        executeLogView.print("\n", whiteConsoleViewContentType)
        executeLogView.flushDeferredText()
        val placeholderText = StrUtil.split(it, "\n").getOrNull(0) ?: "异常信息"
        val myEditor = executeLogView.editor
        val startLine = myEditor.document.lineCount
        val startOffSet = myEditor.visualPositionToOffset(VisualPosition(startLine + 1, 0))

        executeLogView.print("\n${it}", whiteConsoleViewContentType)
        executeLogView.flushDeferredText()

        val endLine = myEditor.document.lineCount
        val endOffSet = myEditor.visualPositionToOffset(VisualPosition(endLine, 0))
        myEditor.foldingModel.runBatchFoldingOperation {
            // 删除异常区域内原生的折叠框，换为自己的
            val foldRegionList =
                myEditor.foldingModel.allFoldRegions.filter { foldRegin -> foldRegin.startOffset >= startOffSet && foldRegin.endOffset <= endOffSet }
            if (foldRegionList.isNotEmpty()) {
                foldRegionList.forEach { foldRegin -> myEditor.foldingModel.removeFoldRegion(foldRegin) }
            }
            myEditor.foldingModel.addFoldRegion(startOffSet + 1, endOffSet, placeholderText)?.isExpanded = false
        }
    }


    /**
     * 打印错误日志
     */
    fun printErrorLog(text: String?) =
        text?.let { executeLogView.print("\n${it}", ConsoleViewContentType.LOG_ERROR_OUTPUT) }

    fun printMavenErrorLog(text: String?) =
        text?.let { executeLogView.print("\n${it}", ConsoleViewContentType.ERROR_OUTPUT) }

    fun clearLogView() = executeLogView.clear()
    override fun getData(dataId: String): Any? = when {
        IDEDataKeys.RUN_CONSOLE_VIEW.`is`(dataId) -> runLogView
        IDEDataKeys.BUILD_CONSOLE_VIEW.`is`(dataId) -> executeLogView
        else -> super.getData(dataId)
    }

    /**
     * 日志类型打印
     */
    enum class LogType(val title: String) {
        RUN("运行日志"), EXECUTE("执行日志"),
    }
}

fun <T : Any> ComponentManager.registerMyExtension(
    name: BaseExtensionPointName<*>,
    instance: T,
    parentDisposable: Disposable
) {
    extensionArea.getExtensionPoint<T>(name.name).registerExtension(instance, parentDisposable)
}
