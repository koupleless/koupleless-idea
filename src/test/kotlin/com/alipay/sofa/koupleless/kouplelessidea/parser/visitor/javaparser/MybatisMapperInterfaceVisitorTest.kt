package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.*
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/28 18:20
 */
class MybatisMapperInterfaceVisitorTest {

    private lateinit var cu: CompilationUnit

    private lateinit var visitor: MybatisMapperInterfaceVisitor

    @Before
    fun setUp() {
        cu = readCu()
        visitor = MybatisMapperInterfaceVisitor
    }

    private fun readCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            
            import com.mock.DO;
            import org.apache.ibatis.annotations.Insert;

            public interface DOMapper {
                @Insert({ "insert into mock_table (id)", "values (#{id,jdbcType=BIGINT})" })
                int insert(DO record);
            }
        """)
    }

    @Test
    fun testDoParse(){
        val projContext = mockk<ProjectContext>{
            every { projectPath } returns "mockProjPath"
            every { name }  returns "mockProjName"
            every { beanContext } returns BeanContext(this)
            every { classInfoContext } returns mockk<ClassInfoContext>{
                every { getClassInfoByPath(any()) } returns mockk<ClassInfo>{
                    every { fullName } returns "com.demo.DOMapper"
                }
            }
            every { analyseConfig } returns AnalyseConfig()
            every { configContext } returns ConfigContext(this)
        }

        visitor.doParse(Path.of("mockPath"),cu,projContext)

        assertEquals(projContext.configContext.dbContext.mapperInterfaces.size,1)
        assertNotNull(projContext.configContext.dbContext.mapperInterfaces["com.demo.DOMapper"])
    }
}
