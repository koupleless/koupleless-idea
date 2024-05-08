package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.SimpleName
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 16:08
 */
class MethodModifierTest {
    @Test
    fun testDoParse(){
        val modifier = MethodModifier()
        val cu = getCu()

        // 立即删除
        val methodToRemove1 = cu.getType(0).getMethodsByName("myMethod1").first()
        modifier.removeMethodNow(methodToRemove1)
        assertEquals(0,cu.getType(0).getMethodsByName("myMethod1").size)

        // 立即添加
        assertEquals(0, cu.getType(0).getMethodsByName("myMethod3").size)
        val methodToAdd1 = MethodDeclaration()
        methodToAdd1.name = SimpleName("myMethod3")
        modifier.addMethodNow(cu,methodToAdd1)
        assertEquals(1, cu.getType(0).getMethodsByName("myMethod3").size)

        // 延迟删除 与 延迟添加
        val methodToRemove2 = cu.getType(0).getMethodsByName("myMethod2").first()
        modifier.removeMethodLater(methodToRemove2)
        assertEquals(1,cu.getType(0).getMethodsByName("myMethod2").size)
        val methodToAdd2 = MethodDeclaration()
        methodToAdd2.name = SimpleName("myMethod4")
        modifier.addMethodLater(methodToAdd2)
        assertEquals(0,cu.getType(0).getMethodsByName("myMethod4").size)

        modifier.doParse(Path.of("mockPath"), cu,null)
        assertEquals(0,cu.getType(0).getMethodsByName("myMethod2").size)
        assertEquals(1,cu.getType(0).getMethodsByName("myMethod4").size)
    }

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            
            public class MyClass {
                public void myMethod1(){}
                public void myMethod2(){}
            }
        """)
    }
}
