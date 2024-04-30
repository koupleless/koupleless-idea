package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import cn.hutool.core.io.FileUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.github.javaparser.ast.CompilationUnit
import java.io.File
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/28 14:29
 */
open class JavaFileModifier(filePath: String): JavaParserVisitor<Void>() {
    protected val filePath: String

    val packageModifier = PackageModifier()

    val importModifier = ImportModifier()

    val classModifier = ClassModifier()

    val fieldModifier = FieldModifier()

    val methodModifier = MethodModifier()

    var resourceToCopy:String? = null

    var absolutePathToCopy:String? = null

    init {
        this.filePath = filePath
    }

    fun activate(){
        if(File(filePath).exists()) return

        if (absolutePathToCopy!=null){
            FileUtil.copyFile(absolutePathToCopy, filePath)
            return
        }

        if(resourceToCopy!=null){
            val templateUrl = this.javaClass.classLoader.getResource(resourceToCopy)
            val templateFileInputStream = templateUrl!!.openStream()
            templateFileInputStream.use {
                FileUtil.writeFromStream(templateFileInputStream, filePath)
            }
            return
        }
    }

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        packageModifier.parse(absolutePath,compilationUnit,arg)

        importModifier.parse(absolutePath,compilationUnit,arg)

        classModifier.parse(absolutePath,compilationUnit,arg)

        fieldModifier.parse(absolutePath,compilationUnit,arg)

        methodModifier.parse(absolutePath,compilationUnit,arg)
    }
}
