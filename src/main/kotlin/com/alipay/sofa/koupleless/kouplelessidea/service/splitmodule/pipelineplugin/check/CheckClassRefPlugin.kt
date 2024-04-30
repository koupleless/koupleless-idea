package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.check

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.util.CollectionUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/8 16:06
 */
class CheckClassRefPlugin(private val contentPanel: ContentPanel): PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        checkClassRef(splitModuleContext)
    }

    override fun getName(): String {
        return "检查类引用插件"
    }

    override fun checkPreCondition(splitModuleContext: SplitModuleContext): Boolean {
        return splitModuleContext.splitMode == SplitConstants.SplitModeEnum.MOVE
    }

    private fun checkClassRef(splitModuleContext: SplitModuleContext){
        val moduleClassInfoSet = splitModuleContext.moduleContext.classInfoContext.getAllClassInfo()
        val srcBaseClassInfoContext = splitModuleContext.srcBaseContext.classInfoContext
        val referByPathToClassPath = mutableMapOf<String,MutableList<String>>()
        moduleClassInfoSet.forEach { classInfo ->
            classInfo.referByClass.forEach {(referClassName,referClassInfo)->
                // 如果 referClassInfo 属于基座，则提示用户
                if(srcBaseClassInfoContext.containsClassName(referClassName)){
                    CollectionUtil.addOrPutList(referByPathToClassPath,referClassInfo.getPath(),classInfo.getPath())
                }
            }
        }

        if (referByPathToClassPath.isNotEmpty()) {
            contentPanel.printErrorLog("检测到以下基座类引用了模块类，拆分后类引用将失效，请考虑将这些基座的类移至模块：")
            var tips = ""
            referByPathToClassPath.forEach { (referByJavaPath, classPaths) ->
                val classPathTip = classPaths.joinToString(",","[","]")
                tips +="基座类路径 $referByJavaPath 引用了模块类： $classPathTip \n"
            }
            contentPanel.printMavenLog(tips)
        }
    }
}
