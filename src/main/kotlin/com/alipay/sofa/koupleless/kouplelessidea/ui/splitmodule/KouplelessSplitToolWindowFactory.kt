package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 *
 * @author lipeng
 * @version : KouplelessSplitToolWindowFactory, v 0.1 2024-04-22 21:47 lipeng Exp $
 */
class KouplelessSplitToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val splitContent = ContentFactory.getInstance().createContent(project.service<SplitPanel>(), "splitmodule", true)
        splitContent.isCloseable = false
        toolWindow.contentManager.addContent(splitContent)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }
}
