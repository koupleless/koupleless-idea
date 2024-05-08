package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 15:54
 */
class FieldModifierTest {
    @Test
    fun testDoParse(){
        val modifier = FieldModifier()
        modifier.removeAnnotation("fieldA",setOf("MockB"))
        modifier.addAnnotation("fieldA",MarkerAnnotationExpr("MockC"))
        modifier.removeField("fieldB")

        val cu = getCu()
        modifier.doParse(Path.of("mockPath"), cu,null)

        val fieldA = cu.getType(0).getFieldByName("fieldA").get()
        assertEquals(1,fieldA.annotations.size)
        assertEquals("MockC",fieldA.annotations[0].nameAsString)

        val fieldB = cu.getType(0).getFieldByName("fieldB")
        assertTrue(fieldB.isEmpty)
    }

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            
            import com.mock.MyField;
            
            public class MyClass {
                @MockB
                private MyField fieldA;
               
                private MyField fieldB;
            }
        """)
    }
}
