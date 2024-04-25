package com.alipay.sofa.koupleless.kouplelessidea.util

import cn.hutool.core.io.FileUtil
import com.intellij.openapi.application.ApplicationInfo

import java.util.concurrent.atomic.AtomicBoolean


interface IDEConstants {

    companion object {
        val TOOL_WINDOW_ID = "KouplelessIDE"
        val SEPARATOR = FileUtil.FILE_SEPARATOR

        // 如 IC-213.7172.25
        val IDEA_API_VERSION = ApplicationInfo.getInstance().apiVersion

        /**
         * 当前程序是否在拆分
         */
        var isSplitting = AtomicBoolean(false)

        /**
         * 当前程序是否允许移动到模块
         */
        var allowMovingToModule = AtomicBoolean(false)
    }

    /**
     * 对应Action的名称和枚举
     */
    enum class ActionName(val text: String, val desc: String) {
        HELP("帮助文档", "点击查看帮助")
    }
}
