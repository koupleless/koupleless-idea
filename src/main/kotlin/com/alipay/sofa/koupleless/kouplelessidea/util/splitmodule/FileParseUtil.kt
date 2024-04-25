package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.FileUtil.FILE_SEPARATOR
import cn.hutool.core.util.StrUtil
import com.ibm.icu.text.CharsetDetector
import com.intellij.util.containers.addIfNotNull
import java.io.BufferedInputStream
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths


/**
 * @description: 工具类
 * @author lipeng
 * @date 2023/8/2 15:21
 */
object FileParseUtil {
    fun parsePackageName(filePath:String?):String?{
        filePath?:return null
        // java 文件且在 src/main/java 目录下
        if(filePath.endsWith(".java") && filePath.contains(StrUtil.join(FILE_SEPARATOR,"src","main","java"))){
            return filePath.substringAfterLast(StrUtil.join(FILE_SEPARATOR,"src","main","java","")).substringBeforeLast(FILE_SEPARATOR).replace(FILE_SEPARATOR,".")
        }
        return null
    }

    fun parseDefaultPackageName(root:File):String{
        // packageNameList：选定路径下 多个 bundle 的 packageName
        val mainDirList = root.walk().filter { it.isDirectory && "main" == it.name }
        val packageNameList = mutableListOf<String>()
        for(mainDir in mainDirList){
            val javaFiles = mainDir.walk().filter { it.isFile && "java" == it.extension }
            packageNameList.addIfNotNull(parsePackageName(javaFiles.firstOrNull()?.absolutePath))
            packageNameList.addIfNotNull(parsePackageName(javaFiles.lastOrNull()?.absolutePath))
        }

        // 如果该路径下没有 java 文件
        if(packageNameList.isEmpty()){
            return parsePackageName(root.walk().firstOrNull { it.isDirectory && FileUtil.isDirEmpty(it)}?.absolutePath)
                ?:""
        }

        // 把 packageNameList 的最短重合子串作为包名
        var bundlePackage = packageNameList.first()
        for(packageName in packageNameList){
            val minLength = if (bundlePackage.length<packageName.length) bundlePackage.length else packageName.length
            for (i in 0 until minLength){
                if(bundlePackage[i] != packageName[i]){
                    bundlePackage = bundlePackage.substring(0,i)
                    break
                }
            }
        }
        return bundlePackage.removeSuffix(".")
    }

    fun parsePackageRoot(bundle:File):File{
        val parsedPackageName = parseDefaultPackageName(bundle)
        return File(StrUtil.join(FILE_SEPARATOR,bundle.path,"src","main","java",parsedPackageName.replace(".", FILE_SEPARATOR)))
    }


    fun parseResourceRoot(bundlePath:String):File{
        return File(StrUtil.join(FILE_SEPARATOR,bundlePath,"src","main","resources"))
    }

    fun parseJavaRoot(bundlePath:String):File{
        return File(StrUtil.join(FILE_SEPARATOR,bundlePath,"src","main","java"))
    }

    fun parseMainRoot(bundlePath: String):File{
        return File(StrUtil.join(FILE_SEPARATOR,bundlePath,"src","main"))
    }

    fun parsePomByBundle(bundlePath:String):File{
        return File(parsePomPathByBundle(bundlePath))
    }

    fun parsePomPathByBundle(bundlePath:String):String{
        return StrUtil.join(FILE_SEPARATOR,bundlePath,"pom.xml")
    }

    fun parsePomByFile(filePath:String):String{
        return parsePomPathByBundle(parseBundlePath(filePath))
    }


    fun parseBundlePath(path:String):String{
        // 是 bundle
        if(isBundle(path)) return path

        // 是 pom.xml
        if(path.endsWith("pom.xml")) return path.removeSuffix("${FILE_SEPARATOR}pom.xml")

        // 是资源文件
        if(inResourceRoot(File(path))){
            return path.substringBeforeLast(StrUtil.join(FILE_SEPARATOR,"src","main","resources")).removeSuffix(FILE_SEPARATOR)
        }

        // 是 java 文件
        return path.substringBeforeLast(StrUtil.join(FILE_SEPARATOR,"src","main","java")).removeSuffix(FILE_SEPARATOR)
    }

    fun parseAllSubPoms(projPath:String):List<File>{
        // 如果是多 bundle：
        val appFolder = getAppFolder(projPath)
        if(appFolder.exists()){
            return appFolder.walk().filter { it.name=="pom.xml" }.toList()
        }

        // 如果是单 bundle：
        if(isBundle(projPath)){
            return listOf(parsePomByBundle(projPath))
        }
        return emptyList()
    }

    /**
     * 解析 bundlePath 下的第一层子bundle
     * @param
     * @return
     */
//    fun parseSubBundles(bundlePath: String):List<File>{
//        if(!isBundle(bundlePath)) return emptyList()
//        val bundle = File(bundlePath)
//        return bundle.listFiles()?.filter { isBundle(it.absolutePath) }?.toList()?: emptyList()
//    }

    fun isBundle(bundlePath: String):Boolean{
        return parsePomByBundle(bundlePath).exists()
    }

    /**
     * path 相对于 rootPath 的相对路径
     * @param null
     * @return
     */
    fun parseRelativePath(rootPath:String,path:String):String{
        return Paths.get(rootPath).relativize(Paths.get(path)).toString()
    }

    /**
     * 获取所有的子bundles：根据pom.xml来判断
     * @param
     * @return
     */
    fun parseAllSubBundles(projPath: String):List<File>{
        return parseAllSubPoms(projPath).map { it.parentFile }
    }

    fun parseAllPoms(projPath: String):List<File>{
        return File(projPath).walk().filter { it.isFile && it.name=="pom.xml" }.toList()
    }

    fun parseBootstrapPom(projPath: String):File{
        val pom = File(StrUtil.join(FILE_SEPARATOR,projPath,"app","bootstrap","pom.xml"))
        if(pom.exists()){
            return pom
        }
        return File(StrUtil.join(FILE_SEPARATOR,projPath,"pom.xml"))
    }

    fun getAppFolder(projPath: String):File{
        return File(StrUtil.join(FILE_SEPARATOR,projPath,"app"))
    }

    fun listDirectory(file:File):List<File>{
        val dirs = file.listFiles()?.filter { it.isDirectory }
        return dirs ?: emptyList()
    }

    fun listValidFiles(file:File):List<File>{
        val files = file.listFiles()?.filter { it.isFile && isValidFile(it) }
        return files ?: emptyList()
    }

    fun isValidFile(file: File):Boolean{
        return file.isFile && !file.name.startsWith(".") && file.extension!="iml"
    }

    private fun inResourceRoot(file: File):Boolean{
        return file.absolutePath.contains(StrUtil.join(FILE_SEPARATOR,"src","main","resources"))
    }

    fun isResourceDir(file:File):Boolean{
        return file.isDirectory && inResourceRoot(file)
    }

    fun isResourceFile(file:File):Boolean{
        return file.isFile && inResourceRoot(file)
    }

    fun folderContainsFile(folder:File, file:File):Boolean{
        return file.absolutePath.contains(folder.absolutePath)
    }

    fun fileInFolder(file:String,folder:String):Boolean{
        return file.startsWith(folder) && file.substringAfter(folder).startsWith(FILE_SEPARATOR)
    }

    fun isNormalFolder(folder: File):Boolean{
        return folder.isDirectory && !isBundle(folder.absolutePath) && !isResourceDir(folder) && !isPackage(folder)
    }

    fun inferEncoding(file: File):String{
        val detector = CharsetDetector()
        val inputStream = file.inputStream()
        val encoding = inputStream.use {
            val bufferedInputStream = BufferedInputStream(inputStream)
            bufferedInputStream.use {
                detector.setText(bufferedInputStream)
                detector.detect()?.name
            }
        }

        return encoding?: Charset.defaultCharset().name()
    }

    fun parseJavaFiles(bundle: File): List<File> {
        if (!bundle.exists()) return emptyList()

        return bundle.walk().filter { it.isFile && "java" == it.extension }.toList()
    }

    /**
     * 如果存在子文件夹是 bundle，则父文件夹为 parentBundle
     * @param
     * @return
     */
    fun isParentBundle(bundlePath: String):Boolean{
        val bundleFile = File(bundlePath)
        if(!bundleFile.exists()) return false
        val anyChildrenIsBundle = bundleFile.listFiles()?.any { isBundle(it.absolutePath) }?:false
        return anyChildrenIsBundle
    }

    /**
     * 是否为包：在 src/main/java路径下的都为 package
     * @param
     * @return 
     */
    fun isPackage(file:File):Boolean{
        if(!file.isDirectory) return false
        return file.absolutePath.contains(StrUtil.join(FILE_SEPARATOR,"src","main","java"))
    }

    fun isFileInJavaRoot(file: File): Boolean {
        return file.isFile && file.absolutePath.contains(StrUtil.join(FILE_SEPARATOR,"src","main","java"))
    }
}
