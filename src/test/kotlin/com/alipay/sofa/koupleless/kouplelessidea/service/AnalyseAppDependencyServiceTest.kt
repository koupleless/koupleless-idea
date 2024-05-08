package com.alipay.sofa.koupleless.kouplelessidea.service

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnOrByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ApplicationContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.AnalyseAppDependencyService
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/3/14 21:40
 */
class AnalyseAppDependencyServiceTest {

    @Test
    fun testAnalyse(){
        val splitModuleContext = mockSplitContextSplitContext()
        val service = AnalyseAppDependencyService()
        val root0 = service.analyse(splitModuleContext,MockKUtil.spyFile("ModuleClassPath0"))
        assertTrue(root0.isEmpty())

        val root1 = service.analyse(splitModuleContext,MockKUtil.spyFile("ModuleClassPath1"))
        // ModuleClass1 依赖类 ModuleClass2，依赖Bean moduleClass3
        val dependOnSubTree = root1[0] as FileDependOnOrByTreeNode
        assertEquals(0, dependOnSubTree.dependByAppClassNum)
        assertEquals(0, dependOnSubTree.dependByAppBeanNum)
        assertEquals(0, dependOnSubTree.dependByClassNum)
        assertEquals(1, dependOnSubTree.dependByBeanNum)
        assertTrue(dependOnSubTree.children.any{it.isClass && it.file.path == "ModuleClassPath2"})
        assertTrue(dependOnSubTree.children.any{it.isBean && it.file.path == "ModuleClassPath3"})

        // ModuleClass1 被类 ModuleClass3 依赖，被Bean moduleClass3 依赖
        val dependBySubTree = root1[1] as FileDependOnOrByTreeNode
        assertTrue(dependBySubTree.children.any{it.isClass && it.file.path == "ModuleClassPath3"})
        assertTrue(dependBySubTree.children.any{it.isBean && it.file.path == "ModuleClassPath3"})
    }

    @Test
    fun testUpdate(){
        val splitModuleContext = mockSplitContextSplitContext()
        val service = AnalyseAppDependencyService()

        val root1 = service.analyse(splitModuleContext,MockKUtil.spyFile("ModuleClassPath1"))
        val moduleClass1 = root1[0] as FileDependOnOrByTreeNode
        val moduleClass2 = moduleClass1.children.first { it.isClass && it.file.path == "ModuleClassPath2" } as FileDependOnOrByTreeNode
        val moduleClass3 = moduleClass1.children.first { it.isBean && it.file.path == "ModuleClassPath3" } as FileDependOnOrByTreeNode

        splitModuleContext.moduleContext.files.clear()

        service.update(splitModuleContext, listOf(moduleClass1, moduleClass2, moduleClass3))
        // class1 被 class3 以 Bean 的方式依赖
        assertEquals(1, moduleClass1.dependByAppBeanNum)
        assertEquals(0, moduleClass1.dependByAppClassNum)

        // class2 被 class1 以类的方式依赖
        assertEquals(1,moduleClass2.dependByAppClassNum)

        // class3 被 class1 以 Bean 的方式依赖
        assertEquals(1,moduleClass3.dependByAppBeanNum)
    }

    private fun mockSplitContextSplitContext(): SplitModuleContext{
        val splitModuleContext = mockk<SplitModuleContext>{
            every { appContext } returns ApplicationContext(this)
            every { moduleContext } returns ModuleContext(this)
        }
        val moduleClass1 = MockKUtil.spyClassInfo("com.mock.ModuleClass1","ModuleClassPath1")
        val moduleBean1 = MockKUtil.spyBeanInfo("moduleClass1","com.mock.ModuleClass1")
        moduleBean1.filePath = "ModuleClassPath1"
        splitModuleContext.appContext.classInfoContext.addClassInfo(moduleClass1)
        splitModuleContext.moduleContext.files.add(MockKUtil.spyFile("ModuleClassPath1"))
        splitModuleContext.appContext.beanContext.addBeanInfo(moduleBean1)


        val moduleClass2 = MockKUtil.spyClassInfo("com.mock.ModuleClass2","ModuleClassPath2")
        splitModuleContext.appContext.classInfoContext.addClassInfo(moduleClass2)
        splitModuleContext.moduleContext.files.add(MockKUtil.spyFile("ModuleClassPath2"))


        val moduleClass3 = MockKUtil.spyClassInfo("com.mock.ModuleClass3","ModuleClassPath3")
        val moduleBean3 = MockKUtil.spyBeanInfo("moduleClass3","com.mock.ModuleClass3")
        moduleBean3.filePath = "ModuleClassPath3"
        splitModuleContext.appContext.classInfoContext.addClassInfo(moduleClass3)
        splitModuleContext.moduleContext.files.add(MockKUtil.spyFile("ModuleClassPath3"))
        splitModuleContext.appContext.beanContext.addBeanInfo(moduleBean3)

        moduleClass1.referClass["com.mock.ModuleClass2"]=moduleClass2
        moduleClass2.referByClass["com.mock.ModuleClass1"]=moduleClass1

        moduleBean1.beanDependBy.add(moduleBean3)
        moduleBean1.beanDependOn["field3"] = mockk<BeanRef>{
            every { parsedRef } returns mutableSetOf(moduleBean3)
        }

        moduleBean3.beanDependBy.add(moduleBean1)
        moduleBean3.beanDependOn["field1"] = mockk<BeanRef>{
            every { parsedRef } returns mutableSetOf(moduleBean1)
        }
        return splitModuleContext
    }
}
