package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 16:47
 */
class PackageModifierTest {
    @Test
    fun testDoParse(){
        val modifier = PackageModifier()
        modifier.packageName = "com.aaa"

        val cu = getCu()
        modifier.doParse(Path.of("mockPath"), cu, null)

        assertTrue(cu.packageDeclaration.isPresent)
        assertEquals("com.aaa", cu.packageDeclaration.get().nameAsString)
    }

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            public class MybatisConfiguration {
            }
        """)
    }
}
