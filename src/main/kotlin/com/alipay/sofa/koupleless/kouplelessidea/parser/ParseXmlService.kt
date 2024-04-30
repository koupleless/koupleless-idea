package com.alipay.sofa.koupleless.kouplelessidea.parser

import com.alipay.sofa.koupleless.kouplelessidea.parser.util.XmlUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.xml.XmlVisitor
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import java.io.File
import java.nio.file.FileSystems


/**
 * @description: XML解析服务
 * @author lipeng
 * @date 2023/8/23 20:39
 */
object ParseXmlService {

    /**
     * 获取 projectPath 目录下所有spring目录下 xml 文件
     * @param projectPath 项目路径
     * @return
     */
     fun <A> parseFromComponentScan(projectPath:String, visitorList:List<XmlVisitor<A>>, arg:A?, absolutePathBlacklist:Set<String> = emptySet()){
        val xmlFileList = collectXmlFromComponentScan(projectPath)
        if(absolutePathBlacklist.isNotEmpty()){
            xmlFileList.removeIf {absolutePathBlacklist.contains(it.absolutePath) }
        }
        XmlUtil.parseDefaultXml(xmlFileList,visitorList,arg)
    }


    fun <A> parseFromResources(projectPath:String, visitorList:List<XmlVisitor<A>>, arg:A?){
        val xmlFileList = collectXmlFromResources(projectPath)
        XmlUtil.parseDefaultXml(xmlFileList,visitorList,arg)
    }

    /**
     * 目前是直接从资源目录下的spring目录下读取xml
     * @param
     * @return
     */
    private fun collectXmlFromComponentScan(projectPath:String):MutableList<File>{
        val jarBundles = MavenPomUtil.parseAllJarBundles(projectPath)
        val resourcesPaths = jarBundles.map { FileParseUtil.parseResourceRoot(it)}
        // TODO: xml的解析路径从bootstrap里读取
        val springDirList = mutableListOf<File>()
        resourcesPaths.flatMapTo(springDirList) {resourcesPath ->
            resourcesPath.walk().filter { it.name.equals("spring") && it.isDirectory}.toList()
        }
        val xmlFileList = mutableListOf<File>()
        springDirList.forEach{
            val xmlList = it.walk().filter { file ->  file.name.endsWith(".xml") }
            xmlFileList.addAll(xmlList)
        }
        return xmlFileList
    }

    private fun collectXmlFromResources(projectPath: String):MutableList<File>{
        val jarBundles = MavenPomUtil.parseAllJarBundles(projectPath)
        val resourcesPaths = jarBundles.map { FileParseUtil.parseResourceRoot(it)}

        val xmlFileList = mutableListOf<File>()
        resourcesPaths.forEach{
            val xmlList = it.walk().filter { file ->  file.name.endsWith(".xml") }
            xmlFileList.addAll(xmlList)
        }
        return xmlFileList
    }
}
