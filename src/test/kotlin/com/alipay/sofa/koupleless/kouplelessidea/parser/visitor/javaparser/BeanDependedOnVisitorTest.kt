package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/2 16:47
 */
class BeanDependedOnVisitorTest {

    private lateinit var cu:CompilationUnit

    @Before
    fun setUp() {
        cu = readCu()
    }

    private fun readCu():CompilationUnit{
        return MockKUtil.readCu("parser/BeanDependOnDemo.java")
    }
    @Test
    fun testParseFieldForAnnotatedBean(){
        val projContext = MockKUtil.mockProjectContext()
        val beanInfo = MockKUtil.spyBeanInfo("beanDependOnDemo","com.alipay.mock.web.BeanDependOnDemo")
        projContext.beanContext.addBeanInfo(beanInfo)

        BeanDependedOnVisitor.parse(null,cu.clone(),projContext)

        // 依赖了 3 个 bean
        assertEquals(3,beanInfo.beanDependOn.size)

        // Autowired 的 UserInfo userInfo;
        val userInfoBeanRef = beanInfo.beanDependOn["userInfo"]
        assertNotNull(userInfoBeanRef)
        assertEquals("userInfo",userInfoBeanRef.beanNameToParse)
        assertEquals("com.mock.module.UserInfo",userInfoBeanRef.beanTypeToParse)

        // @Qualifier("myAClass") 的 MyClass myClass;
        val myClassBeanRef = beanInfo.beanDependOn["myClass"]
        assertNotNull(myClassBeanRef)
        assertEquals("myAClass",myClassBeanRef.beanNameToParse)
        assertEquals("com.mock.outside.MyClass",myClassBeanRef.beanTypeToParse)

        // @Resource("aResource") 的 AResource resource;
        val aResourceBeanRef = beanInfo.beanDependOn["resource"]
        assertNotNull(aResourceBeanRef)
        assertEquals("aResource",aResourceBeanRef.beanNameToParse)
        assertEquals("com.mock.module.AResource",aResourceBeanRef.beanTypeToParse)
    }


    @Test
    fun testParseFieldForXMLBeanWithSetMethod(){
        val projContext = MockKUtil.mockProjectContext()
        val beanInfo = MockKUtil.spyBeanInfo("beanDependOnDemo","com.alipay.mock.web.BeanDependOnDemo")
        beanInfo.definedByXML = true
        projContext.beanContext.addBeanInfo(beanInfo)

        val xmlCu = cu.clone()
        xmlCu.getType(0).annotations.removeIf { anno-> anno.nameAsString == "Controller" }

        // 情况1: 不自动注入 beanInfo
        beanInfo.beanXmlAutowiredMode = BeanRef.AutowiredMode.NO
        BeanDependedOnVisitor.parse(null,xmlCu,projContext)
        assertEquals(3,beanInfo.beanDependOn.size)
        assertFalse(beanInfo.beanDependOn.containsKey("template"))

        // 情况2: byType 自动注入 beanInfo
        beanInfo.beanDependOn.clear()
        beanInfo.beanXmlAutowiredMode = BeanRef.AutowiredMode.BY_TYPE
        BeanDependedOnVisitor.parse(null,xmlCu,projContext)
        assertEquals(4,beanInfo.beanDependOn.size)
        assertTrue(beanInfo.beanDependOn.containsKey("template"))

        // 情况3: byName 自动注入 beanInfo
        beanInfo.beanDependOn.clear()
        beanInfo.beanXmlAutowiredMode = BeanRef.AutowiredMode.BY_NAME
        BeanDependedOnVisitor.parse(null,xmlCu,projContext)
        assertEquals(4,beanInfo.beanDependOn.size)
        assertTrue(beanInfo.beanDependOn.containsKey("template"))
    }



    @Test
    fun testParseFieldForXMLBeanWithSetter(){
        val projContext = MockKUtil.mockProjectContext()
        val beanInfo = MockKUtil.spyBeanInfo("beanDependOnDemo","com.alipay.mock.web.BeanDependOnDemo")
        beanInfo.definedByXML = true
        projContext.beanContext.addBeanInfo(beanInfo)

        val xmlCu = cu.clone()
        xmlCu.getType(0).annotations.removeIf { anno-> anno.nameAsString == "Controller" }
        xmlCu.getType(0).annotations.add(MarkerAnnotationExpr("Setter"))
        JavaParserUtil.filterMethodByName(xmlCu.getType(0),"setTemplate").forEach {
            it.remove()
        }

        // 情况1: 不自动注入 beanInfo
        beanInfo.beanXmlAutowiredMode = BeanRef.AutowiredMode.NO
        BeanDependedOnVisitor.doParse(null,xmlCu,projContext)
        assertEquals(3,beanInfo.beanDependOn.size)
        assertFalse(beanInfo.beanDependOn.containsKey("template"))

        // 情况2: byType 自动注入 beanInfo
        beanInfo.beanDependOn.clear()
        beanInfo.beanXmlAutowiredMode = BeanRef.AutowiredMode.BY_TYPE
        BeanDependedOnVisitor.doParse(null,xmlCu,projContext)
        assertEquals(4,beanInfo.beanDependOn.size)
        assertTrue(beanInfo.beanDependOn.containsKey("template"))

        // 情况3: byName 自动注入 beanInfo
        beanInfo.beanDependOn.clear()
        beanInfo.beanXmlAutowiredMode = BeanRef.AutowiredMode.BY_NAME
        BeanDependedOnVisitor.doParse(null,xmlCu,projContext)
        assertEquals(4,beanInfo.beanDependOn.size)
        assertTrue(beanInfo.beanDependOn.containsKey("template"))
    }
}
