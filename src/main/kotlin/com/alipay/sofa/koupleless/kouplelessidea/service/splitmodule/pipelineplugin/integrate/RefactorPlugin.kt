package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.integrate

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModifyContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.github.javaparser.JavaParser


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/23 18:05
 */
object RefactorPlugin:PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        refactor(splitModuleContext)
    }

    override fun getName(): String {
        return "记录 java 文件和 xml 文件的重构项"
    }

    private fun recordToRefactor(splitModuleContext: SplitModuleContext){
        val refactorContext = splitModuleContext.modifyStageContext.refactorContext
        val classInfoSet = splitModuleContext.moduleContext.classInfoContext.getAllClassInfo()
        classInfoSet.forEach {classInfo->
            if(!classInfo.needRefactor){
                return@forEach
            }

            refactorPackageName(refactorContext,classInfo)

            refactorReferByJavaFile(refactorContext,classInfo,splitModuleContext.moduleContext)

            refactorReferByXML(refactorContext,classInfo)
        }
    }

    private fun refactor(splitModuleContext: SplitModuleContext){
        recordToRefactor(splitModuleContext)
        splitModuleContext.modifyStageContext.refactorContext.modifyAndSave(JavaParser())
    }

    private fun refactorPackageName(refactorContext: ModifyContext, classInfo: ClassInfo){
        refactorContext.setPackageName(classInfo.newPath!!,classInfo.newPackageName!!)
    }

    private fun refactorReferByJavaFile(refactorContext: ModifyContext, classInfo: ClassInfo, moduleContext: ModuleContext){
        classInfo.referByClass.forEach {(referByClassName,referByClassInfo) ->
            // 如果 referByClassName 属于模块，那么重命名。模块类不会被基座类引用
            if(moduleContext.classInfoContext.containsClassName(referByClassName)) {
                val referByJavaPath = referByClassInfo.getPath()
                refactorContext.replacePartImportName(referByJavaPath, classInfo.fullName, classInfo.getNewFullName()!!)
            }
        }
    }

    private fun refactorReferByXML(refactorContext: ModifyContext, classInfo: ClassInfo){
        // classInfo.referByXML 是由上一个plugin记录的，只扫描了模块里的xml
        classInfo.referByXML.forEach {xmlInfo ->
            refactorContext.setXMLNode(xmlInfo,classInfo.getNewFullName()!!)
        }
    }
}
