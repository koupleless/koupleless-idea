package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/6 19:49
 */
class PackageModifier: JavaParserVisitor<Void>(){
    var packageName:String? = null

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        if(shouldModify(compilationUnit)){
            compilationUnit.setPackageDeclaration(packageName!!)
        }
    }

    private fun shouldModify(cu: CompilationUnit):Boolean{
        return packageName != null && !cu.packageDeclaration.isEmpty
    }
}
