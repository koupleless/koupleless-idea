package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser.JavaParserVisitor
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.expr.Name
import java.nio.file.Path


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/6 20:35
 */
class ImportModifier: JavaParserVisitor<Void>() {
    val importsToAdd:MutableMap<String,ImportDeclaration> = mutableMapOf()
    private val importsToRemove:MutableSet<String> = mutableSetOf()
    val importsToReplacePartName:MutableMap<String,String> = mutableMapOf()

    fun addImport(importDeclaration: ImportDeclaration){
        importsToAdd[importDeclaration.nameAsString] = importDeclaration
    }

    fun removeImport(importName:String){
        importsToRemove.add(importName)
    }

    fun replacePartImportName(srcStr:String,tgtStr:String){
        importsToReplacePartName[srcStr] = tgtStr
    }

    fun getAllImportsToAdd():Map<String,ImportDeclaration>{
        return importsToAdd
    }

    fun getAllImportsToRemove():Set<String>{
        return importsToRemove
    }

    fun getAllImportsToReplacePartName():Map<String,String>{
        return importsToReplacePartName
    }

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: Void?) {
        compilationUnit.imports.removeIf { importsToRemove.equals(it.nameAsString) }

        importsToRemove.forEach {
            compilationUnit.imports.removeIf { importDeclaration -> importDeclaration.nameAsString.equals(it) }
        }

        importsToReplacePartName.forEach {(srcStr,tgtStr)->
            compilationUnit.imports.forEach {importStr ->
                if(importStr.nameAsString.contains(srcStr)){
                    importStr.name = Name(importStr.nameAsString.replace(srcStr,tgtStr))
                }
            }
        }

        importsToAdd.forEach {(name,importDeclarationToAdd)->
            if(compilationUnit.imports.none { importDeclaration -> importDeclaration.nameAsString.equals(name) }){
                compilationUnit.imports.add(importDeclarationToAdd)
            }
        }
    }
}
