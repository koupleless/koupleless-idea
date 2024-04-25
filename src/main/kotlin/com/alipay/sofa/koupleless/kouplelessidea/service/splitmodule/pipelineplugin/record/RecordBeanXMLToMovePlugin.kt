package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.record

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.BEAN_XML_NODE


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/27 20:48
 */
object RecordBeanXMLToMovePlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        recordBeanXMLToMove(splitModuleContext)
    }

    override fun getName(): String {
        return "扫描需要移动的beanXML节点"
    }

    /**
     * 扫描beanXML节点，如果是模块在基座里的xml节点，那么需要移动
     */
    private fun recordBeanXMLToMove(splitModuleContext: SplitModuleContext){
        val moduleContext = splitModuleContext.moduleContext
        val moduleXmlFiles = moduleContext.getXMLFiles().map { it.absolutePath }.toSet()
        val moveMode = splitModuleContext.splitMode == SplitConstants.SplitModeEnum.MOVE

        val moveContext = splitModuleContext.integrationStageContext.integrateContext


        val beanContext = splitModuleContext.moduleContext.beanContext
        beanContext.getBeansWithClassName().forEach { beanInfo ->
            if(!beanInfo.definedByXML){
                return@forEach
            }

            val beanXMLNode = beanInfo.getXMLNode(BEAN_XML_NODE)!!
            val srcXMLPath = beanXMLNode.filePath

            if(moduleXmlFiles.contains(srcXMLPath)){
                return@forEach
            }

            val node = beanXMLNode.node!!
            val moduleDefaultXMLPath = StrUtil.join(
                FileUtil.FILE_SEPARATOR,moduleContext.getSpringResourceDir(),srcXMLPath.substringAfterLast(
                    FileUtil.FILE_SEPARATOR))
            moveContext.addSpringXMLNode(moduleDefaultXMLPath,node)

            if(moveMode){
                val className = beanInfo.fullClassName!!
                moveContext.removeXMLNode(srcXMLPath,"//bean[@class='${className}']")
            }
        }
    }
}
