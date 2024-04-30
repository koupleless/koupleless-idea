package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.FileParseUtil
import java.io.File


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/11 17:28
 */
class ClassInfo(classFile: File) {
    val file = classFile
    lateinit var fullName:String
    lateinit var packageName:String
    lateinit var className:String
    var newPackageName:String?=null
    var newPath:String?=null
    var needRefactor = false
    var isAnnotation = false
    var isInterface = false
    val annotations = mutableSetOf<String>()
    var extendClass = mutableSetOf<String>()
    val implements = mutableSetOf<String>()

    val srcPath:String = file.absolutePath

    // 被引用的xml文件
    val referByXML = mutableListOf<XMLPropertyPos>()

    // 引用的JavaClass, key 为引用的类的fullClassName
    val referClass = mutableMapOf<String, ClassInfo>()

    // 被JavaClass引用, key 为被引用的类的fullClassName
    val referByClass = mutableMapOf<String, ClassInfo>()

    fun addReferByXML(xmlPropertyPos: XMLPropertyPos){
        referByXML.add(xmlPropertyPos)
    }

    fun addReferBy(classInfo: ClassInfo){
        referByClass[classInfo.fullName] = classInfo
    }

    fun addRefer(classInfo: ClassInfo){
        referClass[classInfo.fullName] = classInfo
    }

    fun getNewFullName():String?{
        newPackageName?:return null
        return "$newPackageName.$className"
    }

    fun getPath():String{
        newPath?:return srcPath
        return newPath!!
    }

    fun move(tgtPath:String){
        val tgtPackageName = FileParseUtil.parsePackageName(tgtPath)
        requireNotNull(tgtPackageName){"$tgtPath 不是合法的 java 文件路径：该路径应该在 src/main/java 下"}
        newPath = tgtPath
        newPackageName = tgtPackageName
        needRefactor = (newPackageName != packageName)
    }
}
