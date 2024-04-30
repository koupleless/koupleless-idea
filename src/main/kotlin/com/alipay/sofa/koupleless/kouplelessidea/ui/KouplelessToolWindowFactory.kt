package com.alipay.sofa.koupleless.kouplelessidea.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory


class KouplelessToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val kouplelessContent = ContentFactory.getInstance().createContent(project.service<ContentPanel>(), "KouplelessIDE", true)
        kouplelessContent.isCloseable = false
        toolWindow.contentManager.addContent(kouplelessContent)
    }

    /**
     * 判断是否启用此文件
     */
    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }
}
