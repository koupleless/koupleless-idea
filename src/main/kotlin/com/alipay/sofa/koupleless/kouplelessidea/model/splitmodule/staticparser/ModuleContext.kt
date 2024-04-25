package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.ArchetypeInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.ModuleDescriptionInfo
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil
import com.github.javaparser.ParserConfiguration
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/11 12:01
 */
class ModuleContext(parent: SplitModuleContext): ProjectContext(parent) {
    var packageName = ""

    var artifactId = ""

    var groupId = ""

    var type = "MODULE"

    lateinit var root: FileWrapperTreeNode

    val files = mutableListOf<File>()

    var isMono = false

    var moduleLocation:String? = null

    var moduleTemplate: ArchetypeInfo? = null

    var moduleTemplateType: String?=null

    private val srcPathToNewPath = mutableMapOf<String,String>()

    private val configsToAdd = mutableMapOf<String,Any>()
    /**
     * 仅更新必要的信息：模块和基座的描述信息，不进行初始化
     * @param
     * @return
     */
    fun update(moduleDescriptionInfo: ModuleDescriptionInfo, myRoot: FileWrapperTreeNode?=null){
        myRoot?.let { initRoot(myRoot) }
        moduleDescriptionInfo.name?.let { name = moduleDescriptionInfo.name }
        moduleDescriptionInfo.groupId?.let { groupId = moduleDescriptionInfo.groupId }
        moduleDescriptionInfo.artifactId?.let { artifactId=moduleDescriptionInfo.artifactId }
        moduleDescriptionInfo.packageName?.let { packageName = moduleDescriptionInfo.packageName }
        moduleDescriptionInfo.mode?.let {
            isMono = (moduleDescriptionInfo.mode == SplitConstants.Labels.MONO_MODE.tag)
        }
        moduleDescriptionInfo.location?.let {
            moduleLocation = moduleDescriptionInfo.location
            projectPath = StrUtil.join(FileUtil.FILE_SEPARATOR,moduleLocation,moduleDescriptionInfo.name)
        }
        moduleDescriptionInfo.templateType?.let { moduleTemplateType = moduleDescriptionInfo.templateType }
        moduleDescriptionInfo.template?.let { moduleTemplate = moduleDescriptionInfo.template }
    }

    override fun getResourceRoot():String{
        requireNotNull(moduleTemplateType){"请先选择模块模板"}
        return if(moduleTemplateType == SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag){
                StrUtil.join(FileUtil.FILE_SEPARATOR, projectPath, "src","main","resources")
            }else{
                StrUtil.join(FileUtil.FILE_SEPARATOR, projectPath, "app", "bootstrap","src","main","resources")
            }
    }

    override fun getParserConfig(): ParserConfiguration? {
        return parserConfiguration?: run {
            initParserConfig()
            parserConfiguration
        }
    }

    private fun initParserConfig() {
        parserConfiguration = parentContext.appContext.getParserConfig()
    }

    fun initRoot(myRoot: FileWrapperTreeNode){
        root = myRoot
        files.clear()
        addFilesRecursively(root)
    }

    // TODO:最终只应该用projectPath 一个变量
    fun getModulePath():String{
        return StrUtil.join(FileUtil.FILE_SEPARATOR,moduleLocation,name)
    }

    fun getAllAbsolutePaths():Set<String>{
        return files.map { it.absolutePath }.toSet()
    }

    fun getJavaFiles():List<File>{
        return files.filter { it.path.endsWith(".java")}.toList()
    }

    fun containsFile(path:String):Boolean{
        return files.any { it.absolutePath.equals(path) }
    }

    fun getXMLFiles():List<File>{
        return files.filter { it.path.endsWith(".xml")}.toList()
    }

    private fun addFilesRecursively(fileWrapper: FileWrapperTreeNode){
        if(ModuleTreeUtil.isVirtualFile(fileWrapper)){
            files.add(fileWrapper.srcFile)
        }
        fileWrapper.children.forEach {
            addFilesRecursively(it)
        }
    }

    override fun reset() {
        resetModuleInfo()
        clearContext()
        super.reset()
    }

    private fun resetModuleInfo(){
        packageName = ""
        artifactId = ""
        groupId = ""
        type = "MODULE"
        isMono = false
        moduleLocation = null
        moduleTemplate = null
        moduleTemplateType = null
    }

    override fun clearContext() {
        root = FileWrapperTreeNode(File(""))
        files.clear()
        classInfoContext.clear()
        srcPathToNewPath.clear()
        configsToAdd.clear()
        super.clearContext()
    }

    fun getSrcPathToTgtPath():Map<String,String>{
        if(srcPathToNewPath.isEmpty()){
            srcPathToNewPath.putAll(ModuleTreeUtil.getAllSrcPathToNewPath(root))
        }
        return srcPathToNewPath
    }

    fun getTgtPath(srcPath:String):String?{
        if(getSrcPathToTgtPath().isEmpty()){
            val nodePaths = ModuleTreeUtil.getAbsoluteNodePath(root,srcPath, emptyList())
            val nodeName = nodePaths?.lastOrNull()
            val nodePathBeforeName = nodePaths?.subList(0,nodePaths.size-1)
            val relativePath = nodePathBeforeName?.joinToString(separator = ".")?.replace(".",FileUtil.FILE_SEPARATOR)?.substringAfter(name+FileUtil.FILE_SEPARATOR)
            relativePath?:return null
            return StrUtil.join(FileUtil.FILE_SEPARATOR,moduleLocation,relativePath,nodeName)
        }else{
            return srcPathToNewPath[srcPath]
        }
    }

    fun getTgtPackageName(srcPath:String):String?{
        if(!FileUtil.isFile(srcPath)) return null

        val nodePath = ModuleTreeUtil.getAbsoluteNodePath(root,srcPath, emptyList())
        nodePath?:return null

        val nodePathBeforeName = nodePath.subList(0,nodePath.size-1)
        return nodePathBeforeName.joinToString(".").substringAfter("src.main.java.").removeSuffix(".")
    }
}
