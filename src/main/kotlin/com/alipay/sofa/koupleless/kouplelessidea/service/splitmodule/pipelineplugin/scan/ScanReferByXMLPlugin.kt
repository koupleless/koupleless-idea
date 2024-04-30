package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.scan

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseXmlService
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml.ReferByXmlVisitor
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants


/**
 * @description:扫描模块类被 xml 引用
 * @author lipeng
 * @date 2023/11/23 18:01
 */
object ScanReferByXMLPlugin:PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        scanReferByXML(splitModuleContext)
    }

    override fun getName(): String {
        return "扫描被xml引用的插件"
    }

    /**
     * 扫描类引用：扫描模块类被 xml 引用。不认为基座 xml 会引用模块类，因此仅解析模块内部的 xml
     * @param
     * @return
     */
    private fun scanReferByXML(splitModuleContext: SplitModuleContext){
        val referByXMLVisitor = ReferByXmlVisitor
        val modulePath = splitModuleContext.moduleContext.getModulePath()
        val moduleContext = splitModuleContext.moduleContext
        ParseXmlService.parseFromComponentScan(modulePath, listOf(referByXMLVisitor), moduleContext)
    }
}
