package com.alipay.sofa.koupleless.kouplelessidea.ui

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

/**
 *
 * 获取项目所需要的所有配置信息
 **/
object IDEUtil {
    val KOUPLELESS_TOOL_WINDOW_GROUP: NotificationGroup
        get() = NotificationGroupManager.getInstance().getNotificationGroup("KouplelessIDE.NotificationGroup")

    /**
     * 获取日志打印
     */
    fun getConsoleView(project: Project): ConsoleViewImpl =
        TextConsoleBuilderFactory.getInstance()
            .createBuilder(project, GlobalSearchScope.projectScope(project)).console as ConsoleViewImpl



    /**
     * 在窗口上展示信息
     */
    fun showInfoBalloon(message: String, project: Project? = null) {
        KOUPLELESS_TOOL_WINDOW_GROUP.createNotification(message, NotificationType.INFORMATION).notify(project)
    }

    fun showWarningBalloon(message: String, project: Project? = null) {
        KOUPLELESS_TOOL_WINDOW_GROUP.createNotification(message, NotificationType.WARNING).notify(project)
    }

    fun showErrorBalloon(message: String, project: Project? = null) {
        KOUPLELESS_TOOL_WINDOW_GROUP.createNotification(message, NotificationType.ERROR).notify(project);
    }
}
