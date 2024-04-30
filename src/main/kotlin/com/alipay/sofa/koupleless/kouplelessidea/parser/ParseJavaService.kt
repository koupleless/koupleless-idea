package com.alipay.sofa.koupleless.kouplelessidea.parser

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.FileUtil.FILE_SEPARATOR
import cn.hutool.core.io.file.FileReader
import cn.hutool.core.util.RuntimeUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseResult
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.printer.DefaultPrettyPrinter
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import com.github.javaparser.utils.SourceRoot
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path


/**
 * @description: JAVA解析服务
 * @author lipeng
 * @date 2023/8/23 20:39
 */
object ParseJavaService {

    private val printer = DefaultPrettyPrinter()::print

    fun initParserConfiguration(projPath: String): ParserConfiguration {
        val combinedSolver = CombinedTypeSolver()
        // 添加bundle
        val blackWordInPathSet = setOf("test")
        val javaParserTypeSolverList = initJavaParserTypeSolver(projPath, blackWordInPathSet)
        javaParserTypeSolverList.forEach { combinedSolver.add(it) }

        // 添加jar包
        val jarPaths = parseDependentJarFromMockBase(projPath)
        val jarTypeParserList = jarPaths.map { JarTypeSolver(it) }.toList()
        jarTypeParserList.forEach { combinedSolver.add(it) }

        // 添加基础库
        combinedSolver.add(ReflectionTypeSolver())

        // 初始化ParserConfiguration
        val symbolResolver = JavaSymbolSolver(combinedSolver)
        val parserConfiguration = ParserConfiguration()
        parserConfiguration.setSymbolResolver(symbolResolver)
        return parserConfiguration
    }

    private fun parseDependentJarFromMockBase(projectPath:String):List<File>{
        // 本地创建一个和原项目相同，但排除了原项目依赖的项目结构
        val mockBaseDir = StrUtil.join(FILE_SEPARATOR, projectPath, ".mockToSplit")
        createMockBaseProj(projectPath,mockBaseDir)

        val dependentJarPaths = parseDependentJar(mockBaseDir)

        // 删除本地mock目录
        FileUtil.del(mockBaseDir)
        return dependentJarPaths
    }

    fun parseDependentJar(projectPath:String):List<File>{
        // 读取所有外部依赖
        val mvnDependencyCommand = "cd ${projectPath};mvn dependency:list | grep ':jar:' | grep '^\\[INFO' | sed 's/\\[INFO\\]//g;s/ //g'> dependencies.txt"
        RuntimeUtil.execForStr("/bin/sh","-c","-l",mvnDependencyCommand)

        // 读取本地仓库
        val mvnRepositoryCommand = "mvn help:effective-settings|grep localRepository | sed 's/<localRepository>//g;s/<\\/localRepository>//g;s/ //g'"
        val mavenLocalRepository = RuntimeUtil.execForStr("/bin/sh","-c","-l",mvnRepositoryCommand).trim()

        // 解析所有外部依赖的jar包的GAV
        val reader = FileReader(StrUtil.join(FILE_SEPARATOR,projectPath,"dependencies.txt"))
        val dependencyGAV = mutableSetOf<String>()
        reader.readLines().mapNotNullTo(dependencyGAV) { packageDescription->
            val list = packageDescription.split(":")
            if(list.isNotEmpty()){
                val groupId = list[0]
                val artifactId = list[1]
                val version = list[3]
                val GAV = StrUtil.join(":",groupId,artifactId,version)
                GAV
            }else{
                null
            }
        }

        val dependentJarPaths = dependencyGAV.mapNotNull {packageDescription->
            val list = packageDescription.split(":")
            if(list.isNotEmpty()){
                val groupId = list[0]
                val artifactId = list[1]
                val version = list[2]
                val jarLocation = StrUtil.join(FILE_SEPARATOR,mavenLocalRepository,groupId.replace(".",FILE_SEPARATOR),artifactId,version)
                val jarPath = File(jarLocation).listFiles()?.firstOrNull { it.extension=="jar" && !it.name.endsWith("sources.jar")}
                jarPath
            }else{
                null
            }
        }.toList()

        return dependentJarPaths
    }

    private fun createMockBaseProj(projectPath:String,mockBaseDir:String){
        val bundles = MavenPomUtil.parseAllBundlePoms(projectPath)
        // 本地创建一个和原项目相同，但排除了原项目依赖的项目结构
        if(FileUtil.exist(mockBaseDir)){
            FileUtil.del(mockBaseDir)
        }
        FileUtil.mkdir(mockBaseDir)
        val bundlesInfo = MavenPomUtil.parseBundleInfo(bundles)
        bundles.forEach { (bundlePath,pom) ->
            val mockedBundlePath = bundlePath.replace(projectPath,mockBaseDir)
            val mockedPom = pom.clone()
            mockedPom.dependencies.removeIf { bundlesInfo.contains("${it.groupId}:${it.artifactId}")}
            mockedPom.dependencyManagement?.dependencies?.removeIf { bundlesInfo.contains("${it.groupId}:${it.artifactId}")}

            val mockedPomPath = StrUtil.join(FILE_SEPARATOR,mockedBundlePath,"pom.xml")
            FileUtil.mkdir(mockedBundlePath)
            MavenPomUtil.writePomModel(mockedPomPath,mockedPom)
        }
    }

    /**
     * 初始化 ParserConfiguration，把基座和依赖项里的包都加入解析配置中
     * @param
     * @return
     */
    fun initParserConfiguration(proj: Project): ParserConfiguration {
        val combinedSolver = CombinedTypeSolver()
        // 添加bundle
        val blackWordInPathSet = setOf("test")
        val javaParserTypeSolverList = initJavaParserTypeSolver(proj.basePath!!, blackWordInPathSet)
        javaParserTypeSolverList.forEach { combinedSolver.add(it) }

        // 添加jar包，如果Idea能加载到，则从Idea加载，否则依赖mvn加载
        val jarTypeParserListFromIdea = initJarTypeParser(proj)
        if(jarTypeParserListFromIdea.isNotEmpty()){
            jarTypeParserListFromIdea.forEach { combinedSolver.add(it) }
        }else{
            val jarPaths = parseDependentJarFromMockBase(proj.basePath!!)
            val jarTypeParserListFromMvn = jarPaths.map { JarTypeSolver(it) }.toList()
            jarTypeParserListFromMvn.forEach { combinedSolver.add(it) }
        }


        // 添加基础库
        combinedSolver.add(ReflectionTypeSolver())

        // 初始化ParserConfiguration
        val parserConfiguration = ParserConfiguration()
        val symbolResolver = JavaSymbolSolver(combinedSolver)
        parserConfiguration.setSymbolResolver(symbolResolver)
        return parserConfiguration
    }

    /**
     * 把 path 目录下的所有源代码目录加入组合解析器
     * @param blackWordInPathSet : 不解析的黑名单路径
     * @param path：目录路径
     * @return
     */
    private fun initJavaParserTypeSolver(path: String, blackWordInPathSet: Set<String>): List<JavaParserTypeSolver> {
        val bundlePaths = MavenPomUtil.parseAllJarBundles(path)
        val filteredBundlePath = bundlePaths.filterNot { blackWordInPathSet.contains(it) }
        return filteredBundlePath.mapNotNull { getSourceRoot(it) }
            .map { root-> JavaParserTypeSolver(root) }
            .toList()
    }

    fun getSourceRoot(bundlePath:String):File?{
        val file = File(StrUtil.join(FILE_SEPARATOR,bundlePath,"src","main","java"))
        return if (file.exists()){
            file
        }else{
            null
        }
    }

    /**
     * 初始化 JarTypeParser，用于解析 jar 包中的类
     * @param
     * @return
     */
    private fun initJarTypeParser(proj: Project): List<JarTypeSolver> {
        val modules = ModuleManager.getInstance(proj).modules
        val dependencyJarPathSet = mutableSetOf<String>()
        for (module in modules) {
            // 获取 ModuleRootManager 对象
            val moduleRootManager = ModuleRootManager.getInstance(module)
            // 获取 OrderEntry 数组，包括 ModuleSourceOrderEntry 和 LibraryOrderEntry
            val orderEntries = moduleRootManager.orderEntries
            for (orderEntry in orderEntries) {
                // 判断是否为 LibraryOrderEntry
                if (orderEntry is LibraryOrderEntry) {
                    val library = orderEntry.library
                    if (library != null) {
                        // 获取 Library 中的 Jar 包列表
                        val jarFiles: Array<VirtualFile> = library.getFiles(OrderRootType.CLASSES)
                        for (jarFile in jarFiles) {
                            val jarFilePath = jarFile.canonicalPath!!.substringBeforeLast("!")
                            dependencyJarPathSet.add(jarFilePath)
                        }
                    }
                }
            }
        }

        return dependencyJarPathSet.map { JarTypeSolver(it) }
    }


    /**
     * 解析 bundlePaths 目录下所有符合条件的 java 文件
     * @param bundlePaths bundle的路径
     * @param parserConfiguration 解析配置器
     * @param visitorList 解析器
     * @param absolutePathBlackList 绝对路径的黑名单：用于跳过解析
     * @return
     */
    fun <A> parseOnly(projectPath: String, parserConfiguration: ParserConfiguration, visitorList:List<JavaParserVisitor<A>>, arg:A?, absolutePathBlackList:Set<String> = emptySet()){
        val jarBundles = MavenPomUtil.parseAllJarBundles(projectPath)
        for(bundlePath in jarBundles) {
            val sourceRootFile = getSourceRoot(bundlePath)
            val bundleSourceRootPath = sourceRootFile?.absolutePath
            // 忽略测试并忽略其它 arkmodule
            if(null==bundleSourceRootPath||isTestBundle(bundleSourceRootPath)|| isArkModule(bundleSourceRootPath)){
                continue
            }

            val sourceRoot = SourceRoot(sourceRootFile.toPath(),parserConfiguration)
            // SourceRoot 用parse解析的的内存消耗更小，不会存缓存
            sourceRoot.parseParallelized("", object : SourceRoot.Callback {
                override fun process(
                    localPath: Path?,
                    absolutePath: Path?,
                    parseResult: ParseResult<CompilationUnit>?
                ): SourceRoot.Callback.Result {
                    if(absolutePathBlackList.contains(absolutePath!!.toString())){
                        return SourceRoot.Callback.Result.DONT_SAVE
                    }

                    var cu: CompilationUnit? = parseResult!!.result.get()
                    visitorList.forEach { visitor ->
                        visitor.parse(absolutePath,cu!!,arg)
                    }
                    cu = null
                    return SourceRoot.Callback.Result.DONT_SAVE
                }
            })

        }
    }


    /**
     * 解析 bundlePaths 目录下所有符合条件的 java 文件
     * @param bundlePaths bundle的路径
     * @param parserConfiguration 解析配置器
     * @param visitorList 解析器
     * @param absolutePathBlackList 绝对路径的黑名单：用于跳过解析
     * @return
     */
    fun <A> parseParallelizedToCache(projectContext: ProjectContext, visitorList:List<JavaParserVisitor<A>>, arg:A?, absolutePathBlackList:Set<String> = emptySet()){
        val bundleRoots = projectContext.getBundleSourceRoots()
        for ( (bundleSourceRootPath,sourceRoot) in bundleRoots){
            // 忽略测试并忽略其它 arkmodule
            if(isTestBundle(bundleSourceRootPath)|| isArkModule(bundleSourceRootPath)){
                continue
            }

            // TODO：自己写并行解析，不用 JavaParser 自带的
            val parsedResults = sourceRoot.tryToParseParallelized()
            parsedResults.forEach {
                val cu = it.result.get()
                val absolutePath = cu.storage.get().path

                if(absolutePathBlackList.contains(absolutePath!!.toString())){
                    return@forEach
                }

                visitorList.forEach { visitor ->
                    visitor.parse(absolutePath,cu,arg)
                }
            }

        }
    }

    private fun isTestBundle(bundleSourceRootPath:String):Boolean{
        return bundleSourceRootPath.contains(StrUtil.join(FILE_SEPARATOR,"test","src","main","java"))
    }

    private fun isArkModule(bundleSourceRootPath:String):Boolean{
        return bundleSourceRootPath.contains("arkmodule")
    }

    fun <A> parseFromCache(projectContext: ProjectContext, visitorList:List<JavaParserVisitor<A>>, arg:A?, absolutePathBlackList:Set<String> = emptySet()){
        val bundleRoots = projectContext.getBundleSourceRoots()
        for ( (bundleSourceRootPath,sourceRoot) in bundleRoots){
            // 忽略测试并忽略其它 arkmodule
            if(isTestBundle(bundleSourceRootPath)|| isArkModule(bundleSourceRootPath)){
                continue
            }

            val parsedResults = sourceRoot.tryToParse()
            parsedResults.forEach {
                val cu = it.result.get()
                val absolutePath = cu.storage.get().path

                if(absolutePathBlackList.contains(absolutePath!!.toString())){
                    return@forEach
                }

                visitorList.forEach { visitor ->
                    visitor.parse(absolutePath,cu,arg)
                }
            }

        }
    }


    /**
     * 解析多个 java 文件，不修改，内存消耗小，不存缓存:TODO:优化成并行的
     * @param files java文件
     * @return
     */
    fun <A> parseOnly(files:List<File>, parserConfiguration: ParserConfiguration, visitorList:List<JavaParserVisitor<A>>, arg:A?){
        val parser = JavaParser(parserConfiguration)
        for (file in files){
            val inputStream = file.inputStream()
            // 不要使用 parser.parse(file).result.get() 方法解析文件，因为它没有关闭FileInputStream，会导致资源泄漏
            // JVM 没有及时清理这些流，可能会因为过多打开文件导致 CPU 和 RAM 占用居高，甚至无法再打开新的文件进行读写
            inputStream.use { input ->
                var cu:CompilationUnit? = parser.parse(input).result.get()
                val absolutePath = file.toPath()
                visitorList.forEach { visitor ->
                    visitor.parse(absolutePath,cu!!,arg)
                }
                cu = null
            }
        }
    }

    fun <A> parseAndSave(file:File,parser:JavaParser,visitorList:List<JavaParserVisitor<A>>,arg:A?){
        val inputStream = file.inputStream()
        // 不要使用 parser.parse(file).result.get() 方法解析文件，因为它没有关闭FileInputStream，会导致资源泄漏
        // JVM 没有及时清理这些流，可能会因为过多打开文件导致 CPU 和 RAM 占用居高，甚至无法再打开新的文件进行读写
        inputStream.use { input ->
            // 推断编码是为了避免保存后编码混乱
            val encoding = FileParseUtil.inferEncoding(file)
            val cu = parser.parse(input, Charset.forName(encoding)).result.get()
            val absolutePath = file.toPath()
            visitorList.forEach { visitor ->
                visitor.parse(absolutePath,cu,arg)
            }
            cu.setStorage(absolutePath, Charset.forName(encoding))
            cu.storage.get().save(printer)
        }
    }


}
