package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseJavaService
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import org.junit.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/1 17:18
 */
class ClassInfoVisitorTest {
    @Test
    fun testDoParseClass(){
        val url = this.javaClass.classLoader.getResource("parser/ClassInfoDemo.java")!!
        val file = File(url.toURI())
        val projContext = MockKUtil.mockProjectContext()
        ParseJavaService.parseOnly(listOf(file), ParserConfiguration(),listOf(ClassInfoVisitor),projContext)

        val classInfoList = projContext.classInfoContext.getAllClassInfo()
        assertTrue(classInfoList.isNotEmpty())

        val classInfo = classInfoList.first()
        assertEquals(classInfo.className,"ClassInfoDemo")
        assertEquals(classInfo.packageName,"com.demo")
        assertEquals(classInfo.fullName,"com.demo.ClassInfoDemo")
        assertTrue(!classInfo.isAnnotation)
        assertTrue(classInfo.annotations.size==1)
        assertTrue(classInfo.annotations.contains("Component"))
        assertTrue(classInfo.implements.size==2)
        assertTrue(classInfo.implements.contains("com.mock.MockProduct"))
        assertTrue(classInfo.implements.contains("com.mock.MockInfo"))
        assertTrue(classInfo.extendClass.size==1)
        assertTrue(classInfo.extendClass.contains("com.mock.BaseUtil"))
    }

    @Test
    fun testDoParseAnno(){
        val projContext = MockKUtil.mockProjectContext()

        ClassInfoVisitor.doParse(Path.of("mockPath"),getCustomAnnoCu(),projContext)

        assertTrue(projContext.classInfoContext.getAllClassInfo().size==1)
        val classInfo = projContext.classInfoContext.getAllClassInfo().first()
        assertTrue(classInfo.isAnnotation)
    }

    private fun getCustomAnnoCu():CompilationUnit{
        return StaticJavaParser.parse("""
            package com.demo;

            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            import org.springframework.web.bind.annotation.RestController;
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE)
            @RestController
            public @interface DemoController {}
        """.trimIndent())
    }


    @Test
    fun testDoParseWildType(){
        val projContext = MockKUtil.mockProjectContext()

        ClassInfoVisitor.doParse(Path.of("mockPath"),getWildTypeCu(),projContext)

        assertTrue(projContext.classInfoContext.getAllClassInfo().size==1)
        val classInfo = projContext.classInfoContext.getAllClassInfo().first()
        assertEquals(classInfo.extendClass.first(),"com.alipay.secbianque.core.analyze.compute.model.Strategy")
    }

    private fun getWildTypeCu():CompilationUnit{
        return StaticJavaParser.parse("""
            package com.alipay.secbianque.core.analyze.repository;

            import com.alipay.secbianque.core.analyze.compute.model.Strategy;
            
            public interface StrategyRepository extends Strategy<?> {
            }
        """.trimIndent())
    }
}
