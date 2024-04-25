package com.alipay.sofa.koupleless.kouplelessidea.util


import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil.parsePomPathByBundle
import org.apache.maven.model.*
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*


/**
 * @description: maven pom 的读写
 * @author lipeng
 * @date 2023/6/21 10:39
 */
object MavenPomUtil {
    private val writer = MavenXpp3Writer()
    private val reader = MavenXpp3Reader()

    fun buildPomModel(filePath: String): Model {
        return buildPomModel(File(filePath))
    }

    fun buildPomModel(file: File): Model {
        if(!file.exists()){
            throw RuntimeException("ERROR, MavenPomUtil:buildPomModel for ${file.path} 文件不存在")
        }

        try {
            val inputStream = file.inputStream()
            inputStream.use {
                return reader.read(it)
            }
        } catch (e: IOException) {
            throw RuntimeException("ERROR, MavenPomUtil:buildPomModel for ${file.path}", e)
        }
    }

    fun writePomModel(filePath: String, model: Model?) {
        try {
            val fileWriter = FileWriter(filePath)
            fileWriter.use {
                writer.write(fileWriter, model)
            }
        } catch (e: IOException) {
            throw RuntimeException("ERROR, MavenPomUtil:writePomModel for $filePath", e)
        }
    }

    fun writePomModel(file: File, model: Model?) {
        try {
            val fileWriter = FileWriter(file)
            fileWriter.use {
                writer.write(fileWriter, model)
            }
        } catch (e: IOException) {
            throw RuntimeException("ERROR, MavenPomUtil:writePomModel for ${file.path}", e)
        }
    }

     fun addAllDependencyIfAbsent(dependencyList: ArrayList<Dependency>, dependencyToAdd: List<Dependency>){
         val dependencies = dependencyToAdd.filter { d ->
             dependencyList.none { t->t.artifactId == d.artifactId && t.groupId == d.groupId }
         }.toList()
        dependencyList.addAll(dependencies)
    }

    fun mergeProperties(left:Properties, right:Properties):Properties{
        val properties = left.clone() as Properties
        right.forEach{ (k, v) ->
            properties[k] = v
        }
        return properties
    }

    fun mergeDependencyManagement(left:DependencyManagement?, right: DependencyManagement?):DependencyManagement?{
        left?:return right
        right?:return left
        val dependencyManagement = DependencyManagement()
        dependencyManagement.dependencies = mergeDependencies(left.dependencies,right.dependencies)
        return dependencyManagement
    }

    fun mergeBuild(left:Build?, right:Build?):Build?{
        left?:return right
        right?:return left

        val build  = left.clone()
        val buildPlugins = build.plugins.associateBy { "${it.groupId}:${it.artifactId}" }
        right.plugins.forEach {
            if(!buildPlugins.containsKey("${it.groupId}:${it.artifactId}")) {
                build.plugins.add(it)
            }
        }
        return build
    }

    fun mergeProfiles(left:MutableList<Profile>, right: List<Profile>):MutableList<Profile>{
        val profiles = left.toMutableList()
        val profilesId = profiles.map{it.id}.toSet()
        right.forEach {
            if(!profilesId.contains(it.id)){
               profiles.add(it)
            }
        }
        return profiles
    }

    /**
     * 以左边的变量为主
     * @param
     * @return
     */
    fun mergeDependencies(left:MutableList<Dependency>, right:List<Dependency>):MutableList<Dependency>{
        val dependencies = left.toMutableList()
        val dependencyIds = dependencies.map{ "${it.artifactId}:${it.groupId}" }.toSet()
        right.forEach {
            if(!dependencyIds.contains("${it.artifactId}:${it.groupId}")){
                dependencies.add(it)
            }
        }
        return dependencies
    }

    private fun parseAllJarBundlePoms(bundlePath: String):Map<String,Model>{
        val res = mutableMapOf<String,Model>()
        val pom = buildPomModel(parsePomPathByBundle(bundlePath))

        if(pom.packaging!="pom"){
            res[bundlePath] = pom
        }

        // 解析出来modules
        pom.modules?.let {modules->
            modules.forEach {module->
                val subModulePath = StrUtil.join("/",bundlePath,module)
                res.putAll(parseAllJarBundlePoms(subModulePath))
            }
        }
        return res
    }

    fun parseAllBundlePoms(bundlePath:String):Map<String,Model>{
        val res = mutableMapOf<String,Model>()
        val pom = buildPomModel(parsePomPathByBundle(bundlePath))
        res[bundlePath] = pom
        // 解析出来modules
        pom.modules?.let {modules->
            modules.forEach {module->
                val subModulePath = StrUtil.join("/",bundlePath,module)
                res.putAll(parseAllBundlePoms(subModulePath))
            }
        }
        return res
    }

    fun parseAllJarBundles(bundlePath:String):Set<String>{
        return parseAllJarBundlePoms(bundlePath).keys
    }

    fun parseBundleInfo(bundles:Map<String,Model>):Set<String>{
        return bundles.values.map {
            val groupId = it.groupId?:it.parent.groupId
            "${groupId}:${it.artifactId}"
        }.toSet()
    }
}
