package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.construct

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.ConstructModulePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MavenPomUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTemplateUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTreeUtil
import io.mockk.*
import org.apache.maven.model.Model
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/2/20 00:18
 */
class ConstructModulePluginTest {
    @Test
    fun testDoProcessWithSingleBundle(){
        val splitModuleContext = mockSplitModuleContextWithSingleBundle()

        mockkObject(MavenPomUtil){
            val pomSlot = slot<Model>()
            every { MavenPomUtil.writePomModel(any<File>(),capture(pomSlot)) } returns Unit
            ConstructModulePlugin.doProcess(splitModuleContext)

            val pom = pomSlot.captured
            assertTrue(pom.dependencies.any { it.artifactId == "outside-model-a" })

            val modulePath = splitModuleContext.moduleContext.getModulePath()
            assertTrue(File(StrUtil.join(FileUtil.FILE_SEPARATOR,modulePath,"src","main","java","com","singlemodule","mock","MockModel.java")).exists())
            assertTrue(File(StrUtil.join(FileUtil.FILE_SEPARATOR,modulePath,"src","main","java","com","singlemodule","MockModel.java")).exists())
            assertTrue(File(StrUtil.join(FileUtil.FILE_SEPARATOR,modulePath,"src","main","mock","MockModel.java")).exists())
            FileUtil.del(StrUtil.join(FileUtil.FILE_SEPARATOR,modulePath,"src","main","java","com","singlemodule","mock"))
            FileUtil.del(StrUtil.join(FileUtil.FILE_SEPARATOR,modulePath,"src","main","java","com","singlemodule","MockModel.java"))
            FileUtil.del(StrUtil.join(FileUtil.FILE_SEPARATOR,modulePath,"src","main","mock"))
            FileUtil.del(StrUtil.join(FileUtil.FILE_SEPARATOR,modulePath,"src","main","resources"))
        }
    }

    private fun mockSplitModuleContextWithSingleBundle(): SplitModuleContext {
        val rootNode = buildModuleTreeForSingleBundle()
        val modulePath = MockKUtil.getTestResourcePath("mockproj/mocksinglemodule")
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns spyk(ModuleContext(this)){
                every { getModulePath() } returns modulePath
                every { root } returns rootNode
                every { projectPath } returns modulePath
            }
            every { splitMode } returns SplitConstants.SplitModeEnum.COPY
        }
        splitModuleContext.moduleContext.moduleTemplateType = SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag

        return splitModuleContext
    }

    private fun buildModuleTreeForSingleBundle(): FileWrapperTreeNode {
        val root = ModuleTemplateUtil.buildModuleTree(SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag,"mocksinglemodule","com.singlemodule")
        val packageRoot = ModuleTreeUtil.getPackageRoot(root)!!
        val classFile  = FileWrapperTreeNodeFactory.createVirtualFileWrapper(MockKUtil.getTestResourcePath("mockproj/mockbase/app/core/model/src/main/java/com/mock/core/model/MockModel.java"))
        FileWrapperTreeNodeFactory.buildRelation(packageRoot, classFile)

        val newPackage = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("mock")
        val classInNewPackage = FileWrapperTreeNodeFactory.createVirtualFileWrapper(MockKUtil.getTestResourcePath("mockproj/mockbase/app/core/model/src/main/java/com/mock/core/model/MockModel.java"))
        FileWrapperTreeNodeFactory.buildRelationInOrder(listOf(packageRoot,newPackage,classInNewPackage) )

        val mainRoot = ModuleTreeUtil.getMainRootNode(root)!!
        val dirInMain = FileWrapperTreeNodeFactory.createVirtualFolderWrapper("mock")
        val fileInDir = FileWrapperTreeNodeFactory.createVirtualFileWrapper(MockKUtil.getTestResourcePath("mockproj/mockbase/app/core/model/src/main/java/com/mock/core/model/MockModel.java"))
        FileWrapperTreeNodeFactory.buildRelationInOrder(listOf(mainRoot,dirInMain,fileInDir))
        return root
    }
}
