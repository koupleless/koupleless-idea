package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.DBContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertNotNull


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/4 17:01
 */
class SqlSessionFactoryBeanReuserTest {
    private val cu = getCu()

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
            
            
            @Configuration
            public class MybatisConfiguration {
            
                @Autowired
                private DataSource mockDataSource;
                
                @Bean("mockSqlSessionFactory")
                public MybatisSqlSessionFactoryBean setSqlSessionFactory() {
                    MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();                   
                    bean.setDataSource(mockDataSource);
                    bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
                    return bean;
                }
            }
        """)
    }

    @Test
    fun testDoParse(){
        val modifier = SqlSessionFactoryBeanReuser("MockPath",MockKUtil.mockContentPanel())
        val beanInfo = MockKUtil.spyBeanInfo("mockBeanName","org.mybatis.spring.SqlSessionFactoryBean")

        // 验证复用 beanInfo
        val cuWithoutModular = cu.clone()
        val dbSource = DBContext.MybatisDataSource(beanInfo)
        modifier.setDataSource(dbSource)

        modifier.doParse(Path.of("MockPath"),cuWithoutModular,null)
        val setSqlSessionFactoryMethod = cuWithoutModular.getType(0).getMethodsByName("setSqlSessionFactory").first()
        val getBeanStat = JavaParserUtil.filterMethodCallStatInMethodBody(setSqlSessionFactoryMethod, "getBaseBean").firstOrNull()
        assertNotNull(getBeanStat)

        // 验证配置了 mapperLocation
        val resolveMapperLocationMethod = cuWithoutModular.getType(0).getMethodsByName("resolveMapperLocations").firstOrNull()
        assertNotNull(resolveMapperLocationMethod)
    }
}
