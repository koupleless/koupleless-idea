package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/5 17:16
 */
class SqlSessionTemplateBeanReuserTest {

    private val cu = getCu()

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            import org.mybatis.spring.SqlSessionTemplate;
            import org.apache.ibatis.session.SqlSessionFactory;
            
            @Configuration
            public class MybatisConfiguration {
                
                @Bean
                public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
                    return new SqlSessionTemplate(sqlSessionFactory);
                }
            }
        """)
    }

    @Test
    fun testDoParse() {
        // 准备数据
        val templateBeanInfo = MockKUtil.spyBeanInfo("mockSqlSessionTemplateBean","org.mybatis.spring.SqlSessionTemplate")
        val methodSignature = JavaParserUtil.parseMethodSignature(JavaParserUtil.filterMethodByName(cu.getType(0),"sqlSessionTemplate").first())
        templateBeanInfo.defineByMethod(methodSignature)

        val sqlSessionTemplate = mockk<DBContext.SqlSessionTemplate>{
            every { beanInfo } returns templateBeanInfo
        }

        // 修改
        val modifier = SqlSessionTemplateBeanReuser("mockPath",sqlSessionTemplate)
        modifier.doParse(Path.of("mockPath"),cu,null)

        // 验证 method 有 getBean 语句
        val method = JavaParserUtil.filterMethodByName(cu.getType(0),"sqlSessionTemplate").first()
        assertTrue(JavaParserUtil.filterMethodCallStatInMethodBody(method,"getBaseBean").isNotEmpty())

        // 验证 method 没有参数
        assertTrue(method.parameters.isEmpty())
    }
}
