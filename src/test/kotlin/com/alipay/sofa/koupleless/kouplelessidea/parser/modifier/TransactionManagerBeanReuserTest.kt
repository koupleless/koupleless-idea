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
 * @date 2024/1/5 17:18
 */
class TransactionManagerBeanReuserTest {
    private val cu = getCu()

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            
            import javax.sql.DataSource;
            import org.springframework.jdbc.datasource.DataSourceTransactionManager;
            
            @Configuration
            public class MybatisConfiguration {
                
                @Bean("mockTransactionManager")
                public DataSourceTransactionManager sourceTransactionManager(DataSource mockDataSource) {
                    DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
                    dataSourceTransactionManager.setDataSource(mockDataSource);
                    return dataSourceTransactionManager;
                }
            }
        """)
    }

    @Test
    fun testDoParse(){
        // 准备数据
        val managerBeanInfo = MockKUtil.spyBeanInfo("mockTransactionManager",null)
        val methodSignature = JavaParserUtil.parseMethodSignature(JavaParserUtil.filterMethodByName(cu.getType(0),"sourceTransactionManager").first())
        managerBeanInfo.defineByMethod(methodSignature)

        val transactionManager = mockk<DBContext.PlatformTransactionManager>{
            every { beanInfo } returns managerBeanInfo
        }

        // 修改
        val modifier = TransactionManagerBeanReuser("mockPath",transactionManager)
        modifier.doParse(Path.of("mockPath"),cu,null)

        // 校验 method 有 getBean 语句
        val method = JavaParserUtil.filterMethodByName(cu.getType(0),"sourceTransactionManager").first()
        assertTrue(JavaParserUtil.filterMethodCallStatInMethodBody(method,"getBaseBean").isNotEmpty())

        // 验证 method 没有参数
        assertTrue(method.parameters.isEmpty())
    }
}
