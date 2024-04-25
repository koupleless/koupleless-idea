package com.alipay.sofa.koupleless.kouplelessidea.util.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/17 20:45
 */
open class CollapsedPanel(panelTitle:String):VerticalFlowLayoutPanel() {
    val childPanel = mutableListOf<JPanel>()
    val collapsedPanel = mutableListOf<JPanel>()
    val managementLabel = JBLabel()
    val title = panelTitle
    var titleLabel:Component = JBLabel(title)
    var titleComp:Component = BorderLayoutPanel().addToLeft(titleLabel).addToRight(managementLabel)

    constructor(panelTitle:String, centerComp: Component):this(panelTitle){
        this.remove(titleComp)
        titleComp = BorderLayoutPanel().addToLeft(titleLabel).addToRight(managementLabel).addToCenter(centerComp)
        this.add(titleComp)
    }

    init {
        managementLabel.icon = AllIcons.Ide.Notification.Expand
        this.add(titleComp)

        managementLabel.addMouseListener(object : MouseAdapter(){
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1) {
                    when(managementLabel.icon){
                        AllIcons.Ide.Notification.Expand -> {
                            collapsePanel()
                        }
                        AllIcons.Ide.Notification.Collapse -> {
                            expandPanel()
                        }
                    }
                }
            }
        })
    }
    fun expandPanel(){
        managementLabel.icon = AllIcons.Ide.Notification.Expand
        collapsedPanel.forEach{
            it.isVisible=true
        }
    }

    fun collapsePanel(){
        managementLabel.icon = AllIcons.Ide.Notification.Collapse
        collapsedPanel.clear()

        childPanel.forEach{
            if(it.isVisible){
                collapsedPanel.add(it)
            }
            it.isVisible=false
        }
    }

    fun addPanel(panel:JPanel){
        childPanel.add(panel)
        this.add(panel)
    }
}
