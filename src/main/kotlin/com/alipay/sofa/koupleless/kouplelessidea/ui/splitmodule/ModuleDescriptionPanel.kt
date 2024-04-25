package com.alipay.sofa.koupleless.kouplelessidea.ui.splitmodule

import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.ArchetypeInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.BaseData
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.ModuleDescriptionInfo
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.JComponentUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.SINGLE_BUNDLE_TEMPLATE_ARCHETYPE
import com.alipay.sofa.koupleless.kouplelessidea.util.ui.CollapsedPanel
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Component
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JList

/**
 *
 * @author lipeng
 * @version : ModuleDescriptionPanel, v 0.1 2023-10-11 11:43 lipeng Exp $
 */
class ModuleDescriptionPanel(private val proj: Project,titleComp: Component) : CollapsedPanel("模块描述信息",titleComp){

    private val baseComboBox: ComboBox<Any>
    private val moduleGroupIdField = JBTextField("")
    private val moduleArtifactIdField = JBTextField("")
    private val modulePackageField = JBTextField("")
    private val moduleNameLabel = JBLabel("模块名：")
    private val moduleNameField = JBTextField("")
    private val moduleTemplateComboBox = ComboBox(arrayOf(SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE))
    private val moduleModeComboBox = ComboBox(arrayOf(SplitConstants.Labels.MONO_MODE, SplitConstants.Labels.INDEPENDENT_MODE))
    private val moduleIndependentLocationButton: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val locationDescriptionField =JBTextField("")
    private val confirmButton:JButton = JButton("确认并收起")
    val resetButton: JButton = JButton("重置")
    private var baseList:MutableList<Any> = mutableListOf()

    init {
        val defaultModuleLocation = StrUtil.join(IDEConstants.SEPARATOR,proj.baseDir.parent.path,"arkmodule")
        locationDescriptionField.text = "模块将被创建至本地目录：${defaultModuleLocation}${IDEConstants.SEPARATOR}${moduleNameField.text}"
        moduleIndependentLocationButton.text = defaultModuleLocation
    }


    /**
     * 默认值为单模块
     * @param
     * @return
     */
    private fun getModuleTemplateType():String{
        if(null==moduleTemplateComboBox.selectedItem){
            moduleTemplateComboBox.selectedItem = SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE
        }

        return (moduleTemplateComboBox.selectedItem as SplitConstants.Labels).tag
    }

    fun addConfirmActionListener(l: ActionListener){
        confirmButton.addActionListener{e->
            JComponentUtil.disableComponent(this)
            resetButton.isEnabled = true
            collapsePanel()
            l.actionPerformed(e)
        }
    }

    fun addResetActionListener(l: ActionListener){
        resetButton.addActionListener{e->
            l.actionPerformed(e)
            JComponentUtil.enableComponent(this)
        }
    }

    private fun isMono():Boolean{
        if(null == moduleModeComboBox.selectedItem){
            moduleModeComboBox.selectedItem =  SplitConstants.Labels.MONO_MODE
        }
        return moduleModeComboBox.selectedItem ==  SplitConstants.Labels.MONO_MODE
    }

    private fun getModuleName():String?{
        return if(isMono()){
            buildMonoModuleName()
        }else{
            moduleNameField.text
        }
    }

    private fun buildMonoModuleName():String{
        return (baseComboBox.selectedItem as BaseData).app+"-"+moduleNameField.text
    }

    fun getBaseSelectedItem():Any{
        return baseComboBox.selectedItem
    }

    fun getModulePackageName():String?{
        return modulePackageField.text
    }

    private fun getSrcBaseInfo():BaseData?{
        return if(baseList.isNotEmpty()){
            baseList.filterIsInstance<BaseData>().firstOrNull()
        }else if(getBaseSelectedItem() is BaseData){
            getBaseSelectedItem() as BaseData
        }else{
            null
        }
    }

    private fun getTgtBaseLocation():String?{
        return if(getBaseSelectedItem() is LocalBaseData){
            (getBaseSelectedItem() as LocalBaseData).path
        }else{
            null
        }
    }

    private fun getModuleLocation():String{
        return if(isMono()) {
            getMonoModuleLocation()
        } else {
            getIndependentModuleLocation()
        }
    }

    private fun splitToOtherBase():Boolean?{
        return when(getBaseSelectedItem()){
            is BaseData -> false
            is LocalBaseData -> true
            else -> null
        }
    }

    private fun getModuleTemplate(): ArchetypeInfo {
        return when (getModuleTemplateType()) {
            SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag -> SINGLE_BUNDLE_TEMPLATE_ARCHETYPE
            else -> {SINGLE_BUNDLE_TEMPLATE_ARCHETYPE}
        }
    }

    private fun getModuleMode():String?{
        moduleModeComboBox.selectedItem?:return null
        return (moduleModeComboBox.selectedItem as SplitConstants.Labels).tag
    }

    fun getModuleDescriptionInfo():ModuleDescriptionInfo{
        val srcBaseInfo = getSrcBaseInfo()
        val tgtBaseLocation = getTgtBaseLocation()
        val moduleLocation = getModuleLocation()
        val splitToOtherBase  = false
        val templateType = getModuleTemplateType()
        val moduleTemplate = getModuleTemplate()
        val moduleMode = getModuleMode()
        val moduleName = getModuleName()

        return ModuleDescriptionInfo(
            srcBaseInfo,
            tgtBaseLocation,
            moduleName,
            moduleGroupIdField.text,
            moduleArtifactIdField.text,
            modulePackageField.text,
            templateType,
            moduleTemplate,
            moduleMode,
            moduleLocation,
            splitToOtherBase
        )
    }


    init{
        // 0. 选择基座
        baseList = mutableListOf()

        val toSelect = baseList.toMutableList()
        if(toSelect.isEmpty()){
            toSelect.add(BaseData().app(proj.name))
        }
        baseComboBox = ComboBox(toSelect.toTypedArray())


        // 配置每一行的渲染文本
        baseComboBox.renderer = object : ColoredListCellRenderer<Any>() {
            override fun customizeCellRenderer(list: JList<out Any>, value: Any?, index: Int, selected: Boolean, hasFocus: Boolean) {
                when(value){
                    is BaseData -> {
                        requireNotNull(value.app){"未查询到应用名"}
                        append(value.app)
                    }
                    is LocalBaseData -> append(value.name+"（本地目录）")
                }
            }
        }


        baseComboBox.addActionListener{
            when(baseComboBox.selectedItem){
                is BaseData ->{
                    if(isMono()){
                        moduleNameLabel.text = "模块名："+ (baseComboBox.selectedItem as BaseData).app +"-"
                    }else{
                        moduleNameLabel.text = "模块名："
                    }
                }
            }
        }


        val selectBasePanel = BorderLayoutPanel().addToLeft(JBLabel("基座：")).addToCenter(baseComboBox)
        childPanel.add(selectBasePanel)
        this.add(selectBasePanel)


        // 1. 选择模块
        val moduleComboBox = ComboBox(arrayOf(SplitConstants.Labels.ADD_MODULE))
        moduleComboBox.renderer = SplitConstants.Labels.DefaultListCellRenderer
        // 添加选中的监听器
        moduleComboBox.addActionListener{
            when(moduleComboBox.selectedItem){
                SplitConstants.Labels.ADD_MODULE -> {
                    childPanel.forEach{
                        it.isVisible=true
                    }
                }
            }
        }

        val selectModulePanel = BorderLayoutPanel().addToLeft(JBLabel("模块：")).addToCenter(moduleComboBox)
        childPanel.add(selectModulePanel)
        this.add(selectModulePanel)

        // 2. 填入模块名
        val moduleNamePanel = BorderLayoutPanel().addToLeft(moduleNameLabel).addToCenter(moduleNameField)
        moduleNamePanel.isVisible = false
        childPanel.add(moduleNamePanel)
        this.add(moduleNamePanel)

        moduleNameField.addActionListener{
            moduleArtifactIdField.text = getModuleName()
            modulePackageField.text = buildPackageName()
        }


        // 3. 填入groupId, artifactId, packageName
        val groupIdPanel = BorderLayoutPanel().addToLeft(JBLabel("模块GroupId：")).addToCenter(moduleGroupIdField)
        groupIdPanel.isVisible = false
        childPanel.add(groupIdPanel)
        this.add(groupIdPanel)

        val artifactIdPanel = BorderLayoutPanel().addToLeft(JBLabel("模块ArtifactId：")).addToCenter(moduleArtifactIdField)
        artifactIdPanel.isVisible = false
        childPanel.add(artifactIdPanel)
        this.add(artifactIdPanel)

        val packagePanel = BorderLayoutPanel().addToLeft(JBLabel("模块PackageName：")).addToCenter(modulePackageField)
        packagePanel.isVisible = false
        childPanel.add(packagePanel)
        this.add(packagePanel)

        moduleGroupIdField.addActionListener {
            modulePackageField.text = buildPackageName()
        }

        // 4. 选择模块模式：共库/独立目录
        moduleModeComboBox.renderer = SplitConstants.Labels.DefaultListCellRenderer
        val moduleModePanel = BorderLayoutPanel().addToLeft(JBLabel("模块模式：")).addToCenter(moduleModeComboBox)
        moduleModePanel.isVisible = false
        moduleNameLabel.text = if(isMono()){
            "模块名："+ (baseComboBox.selectedItem as BaseData).app +"-"
        }else{
            "模块名："
        }
        this.add(moduleModePanel)
        childPanel.add(moduleModePanel)


        // 5. 选择模块模板
        moduleTemplateComboBox.renderer = SplitConstants.Labels.DefaultListCellRenderer
        val moduleTemplatePanel = BorderLayoutPanel().addToLeft(JBLabel("初始模板：")).addToCenter(moduleTemplateComboBox)
        moduleTemplatePanel.isVisible = false
        childPanel.add(moduleTemplatePanel)
        this.add(moduleTemplatePanel)


        // 6. 选择模块独立目录
        val pathChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        pathChooserDescriptor.setRoots(proj.baseDir)
        moduleIndependentLocationButton.addBrowseFolderListener(TextBrowseFolderListener(pathChooserDescriptor,proj))
        val moduleIndependentLocationPanel = BorderLayoutPanel().addToLeft(JBLabel("模块独立目录：")).addToCenter(moduleIndependentLocationButton)
        moduleIndependentLocationPanel.isVisible = false
        this.add(moduleIndependentLocationPanel)
        childPanel.add(moduleIndependentLocationPanel)
        moduleModeComboBox.addActionListener {
            when(moduleModeComboBox.selectedItem){
                SplitConstants.Labels.MONO_MODE ->{
                    moduleIndependentLocationButton.isEnabled = false
                    moduleIndependentLocationPanel.isVisible = false
                    moduleNameLabel.text = "模块名："+ (baseComboBox.selectedItem as BaseData).app +"-"
                    updateLocationDescriptionPanel()

                    moduleTemplateComboBox.removeAllItems()
                    moduleTemplateComboBox.addItem(SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE)
                }
                SplitConstants.Labels.INDEPENDENT_MODE ->{
                    moduleIndependentLocationButton.isEnabled = true
                    moduleIndependentLocationPanel.isVisible = true
                    moduleNameLabel.text = "模块名："
                    updateLocationDescriptionPanel()

                    moduleTemplateComboBox.removeAllItems()
                    moduleTemplateComboBox.addItem(SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE)
                }
            }

        }

        // 7. 提示模块创建的位置
        locationDescriptionField.border = BorderFactory.createEmptyBorder()
        locationDescriptionField.isEditable=false
        locationDescriptionField.foreground= JBColor.GRAY
        locationDescriptionField.font = Font(locationDescriptionField.font.name, Font.ITALIC, 12)
        val locationDescriptionPanel = BorderLayoutPanel().addToLeft(locationDescriptionField)
        locationDescriptionPanel.isVisible = false
        this.add(locationDescriptionPanel)
        childPanel.add(locationDescriptionPanel)

        // 选择模块名称时，更新提示信息
        moduleNameField.addActionListener{
            locationDescriptionPanel.isVisible = true
            updateLocationDescriptionPanel()
        }

        // 选择目录地址时，更新提示信息
        moduleIndependentLocationButton.addBrowseFolderListener(object : TextBrowseFolderListener(pathChooserDescriptor,proj) {
            override fun actionPerformed(e: ActionEvent) {
                super.actionPerformed(e)
                locationDescriptionPanel.isVisible = true
                updateLocationDescriptionPanel()
            }
        })

        // 选择模块模式时，显示提示信息
        moduleModeComboBox.addItemListener{
            locationDescriptionPanel.isVisible = true
            updateLocationDescriptionPanel()
        }


        // 8. 重置与确认
        resetButton.isEnabled = false
        val buttonPanel = BorderLayoutPanel().addToRight(BorderLayoutPanel().addToCenter(confirmButton).addToRight(resetButton))
        buttonPanel.isVisible = false
        this.add(buttonPanel)
        childPanel.add(buttonPanel)

        border = BorderFactory.createEmptyBorder(10,10,5,10)
    }

    class LocalBaseData(baseDir:String){
        val path = baseDir
        val name = baseDir.substringAfterLast(IDEConstants.SEPARATOR)
    }

    private fun updateLocationDescriptionPanel(){
        val moduleLocation = getModuleLocation()
        val moduleName = getModuleName()
        locationDescriptionField.text="模块将被创建至本地目录：${moduleLocation}${IDEConstants.SEPARATOR}${moduleName}"
    }

    private fun getMonoModuleLocation():String{
        return StrUtil.join(IDEConstants.SEPARATOR,proj.basePath,"arkmodule")
    }

    private fun getIndependentModuleLocation():String{
        return moduleIndependentLocationButton.text
    }

    private fun buildPackageName():String{
        return "${moduleGroupIdField.text}.${moduleArtifactIdField.text}".replace("-",".")
    }


    private fun getModulePath():String?{
        val moduleLocation = getModuleLocation()
        val moduleName = getModuleName()
        if(moduleName.isNullOrBlank()) return null

        return StrUtil.join(IDEConstants.SEPARATOR,moduleLocation,moduleName)
    }

    fun getModuleMapperLocation(): String? {
        val moduleTemplateType = getModuleTemplateType()
        val modulePath = getModulePath() ?:return null
        return if(moduleTemplateType == SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag){
            StrUtil.join(IDEConstants.SEPARATOR, modulePath, "src","main","resources","mapper")
        }else{
            StrUtil.join(IDEConstants.SEPARATOR, modulePath, "app", "service","src","main","resources","mapper")
        }
    }

    fun getModuleMybatisDir():String?{
        val moduleTemplateType = getModuleTemplateType()
        val modulePath = getModulePath() ?:return null
        val packageName = getModulePackageName() ?:return null
        return if(moduleTemplateType == SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag){
            StrUtil.join(IDEConstants.SEPARATOR, modulePath, "src","main","java",packageName.replace(".",IDEConstants.SEPARATOR),"common","dal","mybatis")
        }else{
            StrUtil.join(IDEConstants.SEPARATOR, modulePath, "app", "service","src","main","java",packageName.replace(".",IDEConstants.SEPARATOR),"common","dal","mybatis")
        }
    }
}
