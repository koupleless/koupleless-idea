package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 15:46
 */
class ClassModifierTest {
    @Test
    fun testDoParse(){
        val modifier = ClassModifier()
        modifier.removeAnnotation("MockA")
        modifier.addAnnotation(MarkerAnnotationExpr("SofaService"))
        val cu = getCu()
        modifier.doParse(Path.of("mockPath"), cu,null)

        assertEquals(1,cu.getType(0).annotations.size)
        assertEquals("SofaService",cu.getType(0).annotations[0].nameAsString)
    }

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            @MockA
            public class MyClass {}
        """)
    }
}
