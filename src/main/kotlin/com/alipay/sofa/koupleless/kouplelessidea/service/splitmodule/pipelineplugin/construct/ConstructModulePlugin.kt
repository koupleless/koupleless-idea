package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil
import org.apache.maven.model.Model
import java.io.File


/**
 * @description: 创建模块中的文件
 * @author lipeng
 * @date 2023/11/23 11:46
 */
object ConstructModulePlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        createFilesInModule(splitModuleContext)
    }

    override fun getName(): String {
        return "构建模块插件"
    }

    private fun createFilesInModule(splitModuleContext: SplitModuleContext) {
        when(splitModuleContext.moduleContext.moduleTemplateType){
            SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag -> createFilesInSingleBundle(splitModuleContext)
        }
    }

    private fun createFilesInSingleBundle(splitModuleContext: SplitModuleContext){
        val moduleContext = splitModuleContext.moduleContext
        val splitMode = splitModuleContext.splitMode
        createFilesInSimpleBundle(moduleContext.getModulePath(),moduleContext.root,moduleContext,splitMode)
    }

    private fun createFilesInSimpleBundle(bundlePath:String, bundleNode:FileWrapperTreeNode, moduleContext: ModuleContext, mode:SplitConstants.SplitModeEnum){
        // 创建java文件，允许为空
        val packageRoot = ModuleTreeUtil.getPackageRoot(bundleNode)
        packageRoot?.let {
            val packageLocation = FileParseUtil.parseJavaRoot(bundlePath)
            createFile(packageRoot,packageLocation,mode)
        }


        // 创建资源文件，允许为空
        val resourceRoot = ModuleTreeUtil.getResourceRootNode(bundleNode)
        resourceRoot?.let {
            val resourceLocation = FileParseUtil.parseResourceRoot(bundlePath)
            createFile(resourceRoot,resourceLocation,mode)
        }

        // 创建 Main 下的其它文件夹，允许为空
        val mainRoot = ModuleTreeUtil.getMainRootNode(bundleNode)
        mainRoot?.let {
            val mainLocation = FileParseUtil.parseMainRoot(bundlePath)
            createOtherDirsInMainRoot(mainRoot,mainLocation,mode)
        }

        // 创建模板文件
        val defaultXMLPath = StrUtil.join(FileUtil.FILE_SEPARATOR,moduleContext.getSpringResourceDir(),SplitConstants.AUTO_SPLIT_MODULE_XML)
        val defaultXMLFile = File(defaultXMLPath)
        if(!defaultXMLFile.exists()){
            val templateUrl = this.javaClass.classLoader.getResource("template/auto_split_module.xml")
            val templateFile = templateUrl!!.openStream()
            FileUtil.writeFromStream(templateFile, defaultXMLPath)
        }

        // 创建pom文件
        createPomInBundle(bundlePath,bundleNode)
    }

    private fun createOtherDirsInMainRoot(mainRoot: FileWrapperTreeNode, mainLocation: File,mode:SplitConstants.SplitModeEnum) {
        val otherDirs = mainRoot.children.filter { it.getName()!="java" && it.getName()!="resources" }
        otherDirs.forEach {
            createDir(it,mainLocation,mode)
        }
    }

    private fun createDir(dirNode:FileWrapperTreeNode,dirLocation:File,mode:SplitConstants.SplitModeEnum){
        val dirPath = File(StrUtil.join(FileUtil.FILE_SEPARATOR,dirLocation,dirNode.getName()))

        // 创建文件夹
        FileUtil.mkdir(dirPath)

        // 创建文件
        val files = dirNode.children.filter { ModuleTreeUtil.isVirtualFile(it) }
        files.forEach {
            createFile(it,dirPath,mode)
        }

        // 创建子文件夹
        val dirs =dirNode.children.filter{ ModuleTreeUtil.isVirtualNormalFolder(it)}
        dirs.forEach {
            createDir(it,dirPath,mode)
        }
    }

    /**
     * 创建pom文件
     * @param
     * @return
     */
    private fun createPomInBundle(bundlePath:String, bundleNode:FileWrapperTreeNode){
        // 配置pom文件
        val pomFile = FileParseUtil.parsePomByBundle(bundlePath)

        // 读取本地的pom文件
        val pom = if(pomFile.exists()){
            MavenPomUtil.buildPomModel(pomFile)
        }else{
            Model()
        }

        // 合并生成的pom
        val integratedPom = integratePomInBundle(bundleNode)
        pom.properties = MavenPomUtil.mergeProperties(integratedPom.properties,pom.properties)
        pom.dependencies= MavenPomUtil.mergeDependencies(integratedPom.dependencies,pom.dependencies)
        pom.build = MavenPomUtil.mergeBuild(integratedPom.build,pom.build)
        pom.profiles = MavenPomUtil.mergeProfiles(integratedPom.profiles,pom.profiles)
        // 注意：不配置 pom 的 version 会导致代码构建失败
        pom.modelVersion = "4.0.0"

        // 保存
        MavenPomUtil.writePomModel(pomFile,pom)
    }

    /**
     * 整合树中的所有java文件所在原bundle的pom
     * @param
     * @return
     */
    private fun integratePomInBundle(bundleNode:FileWrapperTreeNode): Model {
        val javaNodes = ModuleTreeUtil.getAllJavaNode(bundleNode)
        val pomPaths = javaNodes.map { FileParseUtil.parsePomByFile(it.srcFile.absolutePath) }.toSet()
        val poms = mutableListOf<Model>()
        pomPaths.forEach {
            val file = File(it)
            if(file.exists()){
                poms.add(MavenPomUtil.buildPomModel(file))
            }
        }

        val model = poms.firstOrNull()?.clone() ?: Model()

        // 整合 dependency, properties, profiles
        model.dependencies.clear()
        model.properties.clear()
        model.profiles.clear()
        poms.forEach {pom->
            model.dependencies = MavenPomUtil.mergeDependencies(model.dependencies,pom.dependencies)
            model.properties  = MavenPomUtil.mergeProperties(model.properties,pom.properties)
            model.profiles = MavenPomUtil.mergeProfiles(model.profiles,pom.profiles)
        }
        return model
    }

    private fun createFile(node: FileWrapperTreeNode, currentLocation:File,mode:SplitConstants.SplitModeEnum){
        if(ModuleTreeUtil.isPackage(node)){
            val packagePath = StrUtil.join(FileUtil.FILE_SEPARATOR,currentLocation,node.getName().replace(".",FileUtil.FILE_SEPARATOR))
            val packageFile = FileUtil.mkdir(packagePath)
            node.children.forEach {childNode->
                createFile(childNode,packageFile,mode)
            }
            return
        }

        if(ModuleTreeUtil.isVirtualFile(node)){
            val targetPath = StrUtil.join(FileUtil.FILE_SEPARATOR,currentLocation,node.getName())
            node.newPath = targetPath
            when(mode){
                SplitConstants.SplitModeEnum.COPY -> FileUtil.copy(node.srcFile,File(targetPath),true)
                SplitConstants.SplitModeEnum.MOVE -> {
                    if(node.markedAsCopy){
                        FileUtil.copy(node.srcFile,File(targetPath),true)
                    }else{
                        FileUtil.move(node.srcFile,File(targetPath),true)
                    }
                }
            }
            return
        }

        if(ModuleTreeUtil.isResourceDir(node)){
            val resourceDir = if(node.isResourceRoot){
                currentLocation
            }else{
                FileUtil.mkdir(StrUtil.join(FileUtil.FILE_SEPARATOR,currentLocation,node.getName()))
            }

            node.children.forEach {childNode->
                createFile(childNode,resourceDir,mode)
            }
            return
        }
    }

}
