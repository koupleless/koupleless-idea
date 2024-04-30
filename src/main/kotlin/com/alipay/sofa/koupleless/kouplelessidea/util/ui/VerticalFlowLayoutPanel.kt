package com.alipay.sofa.koupleless.kouplelessidea.util.ui

import com.intellij.openapi.ui.VerticalFlowLayout
import javax.swing.JPanel


open class VerticalFlowLayoutPanel : JPanel() {
    init {
        this.layout = VerticalFlowLayout(0, 2)
    }
}
