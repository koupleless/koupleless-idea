package com.alipay.sofa.koupleless.kouplelessidea.util

import com.intellij.openapi.util.IconLoader

object IDEIcons {
    val logo = load("/images/logo.svg")
    fun load(path: String) = IconLoader.getIcon(path, IDEIcons::class.java)
}
