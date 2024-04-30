package com.alipay.sofa.koupleless.kouplelessidea.util

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.actionSystem.DataKey

object IDEDataKeys {
    val RUN_CONSOLE_VIEW: DataKey<ConsoleView> = DataKey.create<ConsoleView>("run.logView")
    val BUILD_CONSOLE_VIEW: DataKey<ConsoleView> = DataKey.create<ConsoleView>("build.logView")
}
