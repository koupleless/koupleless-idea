package com.alipay.sofa.koupleless.kouplelessidea.parser.modifier

import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/12/26 10:18
 */
class MybatisConfigReuserTest {
    @Test
    fun testDoParse(){
        val modifier = MybatisConfigReusier("mockPath",MockKUtil.mockContentPanel())
        modifier.newBasePackages.add("com.module.mapper")
        val cu = getCu()
        modifier.doParse(Path.of("mockPath"), cu, null)

        val mapperScanAnno = JavaParserUtil.filterAnnotations(cu.getType(0),setOf(SplitConstants.MAPPER_SCAN_ANNOTATION)).first()
        val basePackageNode = JavaParserUtil.filterAnnoAttributePair(mapperScanAnno,"basePackages")
        assertNotNull(basePackageNode)
        assertEquals("{\"com.module.mapper\"}",basePackageNode.value.toString().replace(" ",""))
    }

    private fun getCu(): CompilationUnit {
        return StaticJavaParser.parse("""
            package com.example;
            
            @Configuration
            @MapperScan(basePackages = "com.base.mapper", sqlSessionFactoryRef = "sqlSessionFactoryBeanForSingle")
            public class MybatisConfiguration {

            }
        """)
    }
}
