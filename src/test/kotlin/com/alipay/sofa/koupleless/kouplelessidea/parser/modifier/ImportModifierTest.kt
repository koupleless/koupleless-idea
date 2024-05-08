package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 16:00
 */
class ImportModifierTest {
    @Test
    fun testDoParse(){
        val modifier = ImportModifier()
        modifier.addImport(ImportDeclaration("com.mock.MockA",false,false))
        modifier.removeImport("com.mock.MockB")
        modifier.replacePartImportName("mock.MockC","mock.MockD")

        val cu = getCu()
        modifier.doParse(Path.of("mockPath"), cu,null)

        assertTrue(cu.imports.any { it.nameAsString== "com.mock.MockA" })
        assertTrue(cu.imports.none { it.nameAsString == "com.mock.MockB" })
        assertTrue(cu.imports.none{ it.nameAsString== "com.mock.MockC" })
        assertTrue(cu.imports.any { it.nameAsString== "com.mock.MockD" })
    }

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            
            import com.mock.MockB;
            import com.mock.MockC;
            
            public class MyClass {}
        """)
    }
}
