package com.alipay.sofa.koupleless.kouplelessidea.model

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory
import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory.buildRelationInOrder
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.ModuleDescriptionInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil.getAllJavaNode
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/3/14 10:06
 */
class ModuleContextTest {
    @Test
    fun testUpdateAndReset(){
        // testUpdate
        val moduleDescriptionInfo = ModuleDescriptionInfo(
            BaseData().app("mockbase"),
            null,
            "mockbase",
            "mockgroupId",
            "mockartifactId",
            "com.mock.abs",
            SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag,
            SplitConstants.SINGLE_BUNDLE_TEMPLATE_ARCHETYPE,
            SplitConstants.Labels.MONO_MODE.tag,
            "moduleLocation",
            false
        )


        val moduleContext = mockModuleContext()
        val root = mockFileWrapperRoot()
        val filePath = MockKUtil.getTestResourcePath("mockproj/mockbase/app/web/src/main/java/com/mock/web/MockController.java")
        moduleContext.update(moduleDescriptionInfo, root)
        assertEquals(1, moduleContext.getAllAbsolutePaths().size)
        assertEquals(1, moduleContext.getJavaFiles().size)
        assertTrue(moduleContext.containsFile(filePath))

        // testReset
        moduleContext.reset()
        assertFalse(moduleContext.containsFile(filePath))
    }


    @Test
    fun testGetSrcPathToTgtPath(){
        val moduleContext = mockModuleContext()
        val javaFilePath = MockKUtil.getTestResourcePath("mockproj/mockbase/app/web/src/main/java/com/mock/web/MockController.java")
        val root = mockFileWrapperRoot()
        moduleContext.root = root

        val javaFileNode = getAllJavaNode(root).first()
        javaFileNode.newPath = "mockTgt.java"

        // getSrcPathToTgtPath
        val srcToTgt = moduleContext.getSrcPathToTgtPath()
        assertEquals("mockTgt.java",srcToTgt[javaFilePath])

        // getTgtPath
        val tgtPath = moduleContext.getTgtPath(javaFilePath)
        assertEquals("mockTgt.java",tgtPath)
    }

    @Test
    fun testGetTgtPath(){
        val moduleContext = mockModuleContext()
        moduleContext.moduleLocation = "mockLocation"
        moduleContext.name = "moduleName"
        val javaFilePath = MockKUtil.getTestResourcePath("mockproj/mockbase/app/web/src/main/java/com/mock/web/MockController.java")

        val root = mockFileWrapperRoot()
        moduleContext.root = root

        assertEquals(StrUtil.join(FileUtil.FILE_SEPARATOR,"mockLocation","src","main","java","com","mock","abc","MockController.java"),moduleContext.getTgtPath(javaFilePath))
    }

    @Test
    fun testGetTgtPackageName(){
        val moduleContext = mockModuleContext()
        moduleContext.moduleLocation = "mockLocation"
        moduleContext.name = "moduleName"
        val javaFilePath = MockKUtil.getTestResourcePath("mockproj/mockbase/app/web/src/main/java/com/mock/web/MockController.java")

        val root = mockFileWrapperRoot()
        moduleContext.root = root

        assertEquals("com.mock.abc",moduleContext.getTgtPackageName(javaFilePath))
    }

    private fun mockModuleContext():ModuleContext{
        val splitContext = mockk<SplitModuleContext>()
        return ModuleContext(splitContext)
    }

    private fun mockFileWrapperRoot(): FileWrapperTreeNode {
        val moduleRoot = FileWrapperTreeNodeFactory.createModuleRootWrapper("moduleName")
        val srcRoot = FileWrapperTreeNodeFactory.createFolderWrapper("src")
        val mainRoot = FileWrapperTreeNodeFactory.createFolderWrapper("main")
        val javaRoot = FileWrapperTreeNodeFactory.createFolderWrapper("java")
        val packageRoot = FileWrapperTreeNodeFactory.createPackageRootWrapper("com.mock.abc")
        val javaFile = MockKUtil.getTestResourcePath("mockproj/mockbase/app/web/src/main/java/com/mock/web/MockController.java")
        val javaFileNode = FileWrapperTreeNodeFactory.createVirtualFileWrapper(javaFile)
        buildRelationInOrder(listOf(moduleRoot,srcRoot,mainRoot,javaRoot, packageRoot, javaFileNode))
        return moduleRoot
    }

}
