package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.factory.FileDependencyTreeNodeFactory
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnOrByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ApplicationContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/22 17:26
 */
class AnalyseAppDependencyService{
    private val cachedDependOn = mutableMapOf<String,FileDependOnTreeNode>()
    private val cachedDependBy = mutableMapOf<String,FileDependByTreeNode>()

    fun clear(){
        cachedDependOn.clear()
        cachedDependBy.clear()
    }

    fun update(splitModuleContext: SplitModuleContext, treesNodes: List<FileDependOnOrByTreeNode>){
        treesNodes.forEach {
            update(splitModuleContext,it)
        }
    }

    fun update(splitModuleContext: SplitModuleContext,curNode: FileDependOnOrByTreeNode){
        curNode.inModule = splitModuleContext.moduleContext.containsFile(curNode.file.path)

        val dependByModuleClassNum = curNode.dependByClassPaths.count {splitModuleContext.moduleContext.containsFile(it) }
        val dependByModuleBeanNum = curNode.dependByBeanPaths.count { splitModuleContext.moduleContext.containsFile(it) }
        curNode.dependByAppClassNum = curNode.dependByClassPaths.size - dependByModuleClassNum
        curNode.dependByAppBeanNum = curNode.dependByBeanPaths.size - dependByModuleBeanNum
    }

    fun analyse(splitModuleContext: SplitModuleContext, file: File): List<FileDependencyTreeNode> {
        clear()
        if(!file.exists()) return emptyList()

        val appContext = splitModuleContext.appContext
        if(!appContext.classInfoContext.containsPath(file.absolutePath)) return emptyList()

        val path = file.absolutePath
        val dependOnSubTree = analyseAppDependOn(splitModuleContext,path)
        val dependBySubTree = analyseAppDependBy(splitModuleContext,path)

        val res = mutableListOf<FileDependencyTreeNode>()
        dependOnSubTree?.let { res.add(it) }
        dependBySubTree?.let { res.add(it) }
        return res
    }

    private fun analyseAppDependBy(splitModuleContext: SplitModuleContext, path:String, curLayer:Int=0): FileDependByTreeNode? {
        if(curLayer >= SplitConstants.MAX_TREE_NODE_LAYER){
            return null
        }
        if(cachedDependBy.containsKey(path)){
            return cachedDependBy[path]
        }

        val appContext = splitModuleContext.appContext
        val curNode = FileDependencyTreeNodeFactory.createDependByNode(path)
        curNode.apply {
            this.inModule = splitModuleContext.moduleContext.containsFile(path)
            this.isClass = appContext.classInfoContext.containsPath(path)
            val classInfo = appContext.classInfoContext.getClassInfoByPath(path)
            this.isBean = if(classInfo!=null) {
                appContext.beanContext.containsClassName(classInfo.fullName)
            }else{
                false
            }
        }

        val dependByClass = analyseClassDependBy(appContext,path)
        val dependByBean = analyseBeanDependBy(appContext,path)

        curNode.dependByClassPaths.addAll(dependByClass)
        curNode.dependByBeanPaths.addAll(dependByBean)

        curNode.dependByClassNum = dependByClass.size
        curNode.dependByBeanNum = dependByBean.size

        val dependByModuleClassNum = dependByClass.filter {splitModuleContext.moduleContext.containsFile(it) }.size
        val dependByModuleBeanNum = dependByBean.filter { splitModuleContext.moduleContext.containsFile(it) }.size
        curNode.dependByAppClassNum = dependByClass.size - dependByModuleClassNum
        curNode.dependByAppBeanNum = dependByBean.size - dependByModuleBeanNum

        analyseDependByChild(splitModuleContext,curNode,path,curLayer+1)
        cachedDependBy[path] = curNode

        return curNode
    }

    private fun analyseDependByChild(splitModuleContext: SplitModuleContext, curNode: FileDependByTreeNode, curPath: String, curLayer: Int=0) {
        val appContext = splitModuleContext.appContext
        val classDependBy = analyseClassDependBy(appContext, curPath)
        val beanDependBy = analyseBeanDependBy(appContext, curPath)
        val pathDependBy = classDependBy.toSet() union beanDependBy
        val children = pathDependBy.mapNotNull { analyseAppDependBy(splitModuleContext,it,curLayer+1) }
        curNode.addChildren(children)
    }

    private fun analyseBeanDependBy(appContext: ApplicationContext, path: String): List<String> {
        val classInfoContext = appContext.classInfoContext
        val classInfo = classInfoContext.getClassInfoByPath(path)
        classInfo?: return emptyList()

        val beanContext = appContext.beanContext
        val beanInfo = beanContext.getBeanByType(classInfo.fullName)
        beanInfo?: return emptyList()

        return beanInfo.beanDependBy.map { it.filePath }.filter { it.isNotEmpty() }.toList()
    }

    private fun analyseClassDependBy(appContext: ApplicationContext, path: String): List<String> {
        val classInfoContext = appContext.classInfoContext
        val classInfo = classInfoContext.getClassInfoByPath(path)
        classInfo?: return emptyList()

        return classInfo.referByClass.values.map { it.getPath() }.toList()
    }

    private fun analyseAppDependOn(splitModuleContext:SplitModuleContext, path:String, curLayer:Int=0): FileDependOnTreeNode? {
        if(curLayer >= SplitConstants.MAX_TREE_NODE_LAYER){
            return null
        }
        if(cachedDependOn.containsKey(path)){
            return cachedDependOn[path]
        }

        val appContext = splitModuleContext.appContext
        val curNode = FileDependencyTreeNodeFactory.createDependOnNode(path)
        curNode.apply {
            this.inModule = splitModuleContext.moduleContext.containsFile(path)
            this.isClass = appContext.classInfoContext.containsPath(path)
            val classInfo = appContext.classInfoContext.getClassInfoByPath(path)
            this.isBean = if(classInfo!=null) {
                appContext.beanContext.containsClassName(classInfo.fullName)
            }else{
                false
            }
        }

        val dependByClass = analyseClassDependBy(appContext,path)
        val dependByBean = analyseBeanDependBy(appContext,path)
        curNode.dependByClassPaths.addAll(dependByClass)
        curNode.dependByBeanPaths.addAll(dependByBean)

        curNode.dependByClassNum = dependByClass.size
        curNode.dependByBeanNum = dependByBean.size

        val dependByModuleClassNum = dependByClass.filter {splitModuleContext.moduleContext.containsFile(it) }.size
        val dependByModuleBeanNum = dependByBean.filter { splitModuleContext.moduleContext.containsFile(it) }.size
        curNode.dependByAppClassNum = dependByClass.size - dependByModuleClassNum
        curNode.dependByAppBeanNum = dependByBean.size - dependByModuleBeanNum

        cachedDependOn[path] = curNode

        analyseDependOnChild(splitModuleContext,curNode,path,curLayer+1)
        return curNode
    }
    private fun analyseDependOnChild(splitModuleContext: SplitModuleContext, curNode:FileDependOnTreeNode, curPath: String, curLayer:Int=0){
        val appContext = splitModuleContext.appContext
        val classDependOn = analyseClassDependOn(appContext, curPath)
        val beanDependOn = analyseBeanDependOn(appContext, curPath)
        val pathDependOn = classDependOn.toSet() union beanDependOn
        val children = pathDependOn.mapNotNull { analyseAppDependOn(splitModuleContext,it,curLayer+1) }
        curNode.addChildren(children)
    }

    private fun analyseBeanDependOn(appContext:ApplicationContext, path: String): List<String>{
        val classInfoContext = appContext.classInfoContext
        val classInfo = classInfoContext.getClassInfoByPath(path)
        classInfo?: return emptyList()

        val beanContext = appContext.beanContext
        val beanInfo = beanContext.getBeanByType(classInfo.fullName)
        beanInfo?: return emptyList()

        val beanRefs = beanInfo.beanDependOn.values.flatMap { it.parsedRef }
        return beanRefs.map { it.filePath }.filter { it.isNotEmpty() }.toList()
    }

    private fun analyseClassDependOn(appContext:ApplicationContext, path: String): List<String> {
        val classInfoContext = appContext.classInfoContext
        val classInfo = classInfoContext.getClassInfoByPath(path)
        classInfo?: return emptyList()

        return classInfo.referClass.values.map { it.getPath() }.toList()
    }

}
