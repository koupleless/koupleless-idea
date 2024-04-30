package com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.split

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.XMLContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.PipelinePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.IDEConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/5 14:45
 */
object SplitMapperBeanPlugin: PipelinePlugin() {
    override fun doProcess(splitModuleContext: SplitModuleContext) {
        splitMapperXMLBeanToModule(splitModuleContext)
    }

    override fun getName(): String {
        return "分割模块和基座的 Mapper Bean 服务"
    }
    /**
     * 1. 扫描模块中的 mapper bean：
     *   a. 根据选择 Mapper 的 java 文件
     *     ⅰ. 把相关的 xml bean 注册到模块bean上下文中
     *     ⅱ. 如果 xml 没有选，那么自动加上
     *     ⅲ. 配置数据源、事务模板
     *   b. 如果没有选 Mapper 的 java 文件
     *     ⅰ. 什么都不做
     * 2. 扫描基座中的 mapper bean：
     *   a. 模块以外的都是基座的 mapper bean
     * @param
     * @return 
     */
    private fun splitMapperXMLBeanToModule(splitModuleContext: SplitModuleContext){
        val mapperXMLs = splitModuleContext.appContext.xmlContext.getMapperXMLs()
        val moduleContext = splitModuleContext.moduleContext
        val srcBaseContext = splitModuleContext.srcBaseContext
        val moduleClassInfoContext = moduleContext.classInfoContext
        val srcBaseClassInfoContext = srcBaseContext.classInfoContext

        mapperXMLs.forEach {(interfaceType,mapperXML) ->
            if(moduleClassInfoContext.containsClassName(interfaceType)){
                // 把 classInfo 关联到 mapperXML
                mapperXML.classInfo = moduleClassInfoContext.getClassInfoByName(interfaceType)!!

                // 注册 beanInfo
                moduleContext.beanContext.addBeanInfo(mapperXML.beanInfo)

                // 注册 mapperInterface
                moduleContext.configContext.dbContext.registerMapperInterface(mapperXML.classInfo!!)

                // 记录到模块
                recordXMLToModule(splitModuleContext,mapperXML)
            }else if (srcBaseClassInfoContext.containsClassName(interfaceType)){
                // 把 classInfo 关联到 mapperXML
                mapperXML.classInfo = srcBaseClassInfoContext.getClassInfoByName(interfaceType)!!

                // 注册 beanInfo
                srcBaseContext.beanContext.addBeanInfo(mapperXML.beanInfo)

                // 注册 mapperInterface
                srcBaseContext.configContext.dbContext.registerMapperInterface(mapperXML.classInfo!!)
            }
        }
    }

    private fun recordXMLToModule(splitModuleContext: SplitModuleContext,mapperXML:XMLContext.MapperXML){
        val integrationStageContext = splitModuleContext.integrationStageContext
        val moveContext = integrationStageContext.integrateContext

        val srcXMLPath = mapperXML.filePath
        val tgtXMLPath = StrUtil.join(
            FileUtil.FILE_SEPARATOR,integrationStageContext.getConfig(SplitConstants.MODULE_MYBATIS_MAPPER_LOCATION_CONFIG) as String,srcXMLPath.substringAfterLast(
                FileUtil.FILE_SEPARATOR))
        moveContext.copyXMLFromAbsolutePath(tgtXMLPath,srcXMLPath)
    }
}
