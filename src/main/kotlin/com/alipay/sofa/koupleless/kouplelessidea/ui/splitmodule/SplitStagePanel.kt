package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.ui.VerticalFlowLayoutPanel
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Component
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/19 22:35
 */
class SplitStagePanel: VerticalFlowLayoutPanel() {
    // 阶段1：初步检测
    private val mode = ComboBox(arrayOf(SplitConstants.Labels.MOVE_MODE,SplitConstants.Labels.COPY_MODE))
    private val detectButton = JButton("初步检测")
    val stopDetectingButton = JButton("停止")


    // 阶段2：开始拆分
    private val autoModify = ComboBox(arrayOf(SplitConstants.Labels.AUTO_MODIFY,SplitConstants.Labels.NOT_AUTO_MODIFY))
    private val splitButton = JButton("开始拆分")
    val stopSplittingButton = JButton("停止")

    // 阶段3：部署验证
//    private val deployButton = JButton("部署验证")

    enum class Stage{
        CHECK,
        SPLIT,
        DEPLOY
    }

    private val stageEnabled = mutableMapOf(Pair(Stage.CHECK,true),Pair(Stage.SPLIT,false),Pair(Stage.DEPLOY,false))

    private val stageComponents = mutableMapOf<Stage,List<Component>>()

    init {
        stageComponents[Stage.CHECK] = listOf(mode,detectButton)
        stageComponents[Stage.SPLIT] = listOf(autoModify,splitButton)
//        stageComponents[Stage.DEPLOY] = listOf(deployButton)

        autoModify.renderer = SplitConstants.Labels.DefaultListCellRenderer
        mode.renderer = SplitConstants.Labels.DefaultListCellRenderer

        val checkStagePanel = BorderLayoutPanel().addToLeft(JLabel("阶段1:检测")).addToRight(BorderLayoutPanel().addToLeft(mode).addToCenter(detectButton).addToRight(stopDetectingButton))
        val splitStagePanel = BorderLayoutPanel().addToLeft(JLabel("阶段2:拆分")).addToRight(BorderLayoutPanel().addToLeft(autoModify).addToCenter(splitButton).addToRight(stopSplittingButton))
//        val deployStagePanel = BorderLayoutPanel().addToLeft(JLabel("阶段3:部署验证")).addToRight(deployButton)

        this.add(BorderLayoutPanel().addToLeft(JBLabel("拆分阶段")))
        this.add(checkStagePanel)
        this.add(splitStagePanel)
//        this.add(deployStagePanel)

        toCheckStage()
        stopDetectingButton.isEnabled = false
        stopSplittingButton.isEnabled = false

        border = BorderFactory.createEmptyBorder(5,10,5,10)
    }

    fun toNewBase(){
        // 新基座模式下，不支持移动模式
        mode.removeAllItems()
        mode.addItem(SplitConstants.Labels.COPY_MODE)
        mode.selectedItem = SplitConstants.Labels.COPY_MODE
    }

    fun addDetectActionListener(l:ActionListener){
        detectButton.addActionListener{e->
            detectButton.isEnabled = false
            l.actionPerformed(e)
            stopDetectingButton.isEnabled = true
        }
    }

    fun enableStopDetecting(){
        stopDetectingButton.isEnabled = true
    }

    fun enableStopSplitting(){
        stopSplittingButton.isEnabled = true
    }

    fun addStopDetectingActionListener(l:ActionListener){
        stopDetectingButton.addActionListener { e->
            stopDetectingButton.isEnabled = false
            l.actionPerformed(e)
        }
    }

    fun addSplitActionListener(l:ActionListener){
        splitButton.addActionListener{e->
            splitButton.isEnabled = false
            stopSplittingButton.isEnabled = true
            l.actionPerformed(e)
        }
    }

    fun addStopSplittingActionListener(l:ActionListener){
        stopSplittingButton.addActionListener {e->
            stopSplittingButton.isEnabled = false
            l.actionPerformed(e)
        }
    }

    fun addDeployActionListener(l:ActionListener){
//        deployButton.addActionListener{e->
//            l.actionPerformed(e)
//        }
    }


    private fun enableStageComponents(){
        stageComponents.forEach { (stage, buttons) ->
            buttons.forEach {
                it.isEnabled = stageEnabled[stage]!!
            }
        }
    }

    fun toCheckStage(){
        stageEnabled[Stage.CHECK]=true
        stageEnabled[Stage.SPLIT]=false
        stageEnabled[Stage.DEPLOY] = false
        enableStageComponents()
    }

    fun toSplitStage(){
        stageEnabled[Stage.CHECK]= true
        stageEnabled[Stage.SPLIT] = true
        stageEnabled[Stage.DEPLOY] = false
        enableStageComponents()
    }

    fun toDeployStage(){
        stageEnabled[Stage.CHECK]= true
        stageEnabled[Stage.SPLIT]=false
        stageEnabled[Stage.DEPLOY] = true
        enableStageComponents()
    }

    fun getMode():SplitConstants.SplitModeEnum{
        val selected = mode.selectedItem as SplitConstants.Labels
        if(selected == SplitConstants.Labels.MOVE_MODE){
            return SplitConstants.SplitModeEnum.MOVE
        }
        return SplitConstants.SplitModeEnum.COPY
    }

    fun getAutoModify():Boolean{
        val selected = autoModify.selectedItem as SplitConstants.Labels
        return selected == SplitConstants.Labels.AUTO_MODIFY
    }
}
