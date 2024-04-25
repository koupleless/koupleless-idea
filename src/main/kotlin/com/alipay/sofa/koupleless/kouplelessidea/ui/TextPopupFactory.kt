package com.alipay.sofa.koupleless.kouplelessidea.ui

import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

/**
 *
 * @author lipeng
 * @version : TextPopupFactory, v 0.1 2023-09-05 10:53 lipeng Exp $
 */

object TextPopupFactory{
    fun createTextPopup(textAreaTitle:String, textAreaText:String): JBPopup {
        val textArea  = JBTextArea(textAreaText)
        textArea.preferredSize = Dimension(500,25)

        val popup = JBPopupFactory.getInstance().createComponentPopupBuilder(textArea, JBLabel())
            .setTitle(textAreaTitle)
            .setMayBeParent(true)
            .setResizable(true)
            .setMovable(true)
            .setNormalWindowLevel(true)
            .setRequestFocus(true)
            .createPopup()


        textArea.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {}

            override fun keyPressed(e: KeyEvent) {}

            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    popup.closeOk(null)
                }
            }
        })

        textArea.caretPosition = textArea.document.length
        return popup
    }
}
