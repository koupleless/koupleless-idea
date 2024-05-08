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
 * @date 2024/1/6 14:43
 */
class TransactionTemplateBeanReuserTest {

    private val cu = getCu()

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            
            import javax.sql.DataSource;
            import org.springframework.transaction.support.TransactionTemplate;
            
            @Configuration
            public class MybatisConfiguration {
                
                @Bean("mockTransactionTemplate")
                public TransactionTemplate defaultTransactionTemplate() {
                    TransactionTemplate transactionTemplate = new TransactionTemplate();
                    transactionTemplate.setTransactionManager(sourceTransactionManager());
                    return transactionTemplate;
                }
            }
        """)
    }

    @Test
    fun testDoParse(){
        // 准备数据
        val templateBeanInfo = MockKUtil.spyBeanInfo("mockTransactionTemplate",null)
        val methodSignature = JavaParserUtil.parseMethodSignature(JavaParserUtil.filterMethodByName(cu.getType(0),"defaultTransactionTemplate").first())
        templateBeanInfo.defineByMethod(methodSignature)

        val transactionTemplate = mockk<DBContext.TransactionTemplate>{
            every { beanInfo } returns templateBeanInfo
        }

        // 修改
        val modifier = TransactionTemplateBeanReuser("mockPath",transactionTemplate)
        modifier.doParse(Path.of("mockPath"),cu,null)

        // 校验 method 有 getBean 语句
        val method = JavaParserUtil.filterMethodByName(cu.getType(0),"defaultTransactionTemplate").first()
        assertTrue(JavaParserUtil.filterMethodCallStatInMethodBody(method,"getBaseBean").isNotEmpty())

        // 验证 method 没有参数
        assertTrue(method.parameters.isEmpty())
    }
}
