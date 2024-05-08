package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory
import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory.buildRelation
import com.alipay.sofa.koupleless.kouplelessidea.factory.FileWrapperTreeNodeFactory.buildRelationInOrder
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import org.junit.Test
import java.io.File
import kotlin.test.*


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/3/12 15:38
 */
class ModuleTreeUtilTest {
    @Test
    fun testContains(){
        val parentNode = FileWrapperTreeNode(File("mockBundleRoot"))
        ModuleTreeUtil.createEmptyBundle(parentNode,"mockBundle","com.mock")
        assertTrue(ModuleTreeUtil.contains(parentNode,"mockBundle"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"java"))
        assertFalse(ModuleTreeUtil.contains(parentNode,"abc"))

    }
    @Test
    fun testIsValidWhitelist(){
        val folder = MockKUtil.readFile("mockproj")
        // 验证无白名单情况
        assertFalse(ModuleTreeUtil.isValidWhitelist(folder,null))
        assertFalse(ModuleTreeUtil.isValidWhitelist(folder,emptySet()))
        // 验证白名单中只有自己的情况
        assertFalse(ModuleTreeUtil.isValidWhitelist(folder,setOf(folder.absolutePath)))
        // 验证白名单中有子文件的情况
        assertTrue(ModuleTreeUtil.isValidWhitelist(folder,setOf(folder.absolutePath,MockKUtil.readFile("mockproj/mockbase").absolutePath)))
    }

    @Test
    fun testImportBundleWithoutWhitelist(){
        val parentNode = FileWrapperTreeNodeFactory.createBundleRootWrapper("mockBundleRoot")
        val importedBundle = MockKUtil.readFile("mockproj/mockbase/app/bootstrap")
        ModuleTreeUtil.importBundle(parentNode,importedBundle,"new.mock.bootstrap",null)

        assertTrue(ModuleTreeUtil.contains(parentNode,"bootstrap"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"new.mock.bootstrap"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"Application.java"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"subvir.properties"))
    }

    @Test
    fun testImportBundleWithWhitelist(){
        val parentNode = FileWrapperTreeNodeFactory.createBundleRootWrapper("mockBundleRoot")
        val importedBundle = MockKUtil.readFile("mockproj/mockbase/app/core/model")
        val whitelist = setOf(
            MockKUtil.readFile("mockproj/mockbase/app/core/model/src/main/java/com/mock/core/model/MockModel.java").absolutePath,
            MockKUtil.readFile("mockproj/mockbase/app/core/model/src/main/vir/subvir/subvir.properties").absolutePath,
            MockKUtil.readFile("mockproj/mockbase/app/core/model/src/main/vir/subvir2").absolutePath
        )
        ModuleTreeUtil.importBundle(parentNode,importedBundle,"new.mock.core",whitelist)

        assertTrue(ModuleTreeUtil.contains(parentNode,"model"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"MockModel.java"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"new.mock.core"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"subvir.properties"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"subvir2.properties"))
        assertFalse(ModuleTreeUtil.contains(parentNode,"MockUser.java"))
        assertFalse(ModuleTreeUtil.contains(parentNode,"vir.properties"))
    }

    @Test
    fun testImportParentBundleWithoutWhitelist(){
        val parentNode = FileWrapperTreeNodeFactory.createBundleRootWrapper("mockBundleRoot")
        val importedBundle = MockKUtil.readFile("mockproj/mockmultimodule/app")
        ModuleTreeUtil.importBundle(parentNode,importedBundle,"new.mock",null)

        assertTrue(ModuleTreeUtil.contains(parentNode,"bootstrap"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"MockModel.java"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"new.mock"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"MockController.java"))
    }

    @Test
    fun testImportParentBundleWithWhitelist(){
        // case1: 只选择 core/model 文件夹,不选择 core 文件夹
        val parentNode1 = FileWrapperTreeNodeFactory.createBundleRootWrapper("mockBundleRoot")
        val importedBundle1 = MockKUtil.readFile("mockproj/mockmultimodule/app")
        val whitelist1 = setOf(
            MockKUtil.readFile("mockproj/mockmultimodule/app/bootstrap").absolutePath,
            MockKUtil.readFile("mockproj/mockmultimodule/app/core/model").absolutePath
        )
        ModuleTreeUtil.importBundle(parentNode1,importedBundle1,"new.mock",whitelist1)

        assertTrue(ModuleTreeUtil.contains(parentNode1,"bootstrap"))
        assertTrue(ModuleTreeUtil.contains(parentNode1,"model"))
        assertTrue(ModuleTreeUtil.contains(parentNode1,"new.mock"))
        assertTrue(ModuleTreeUtil.contains(parentNode1,"MockModel.java"))
        assertFalse(ModuleTreeUtil.contains(parentNode1,"core"))
        assertFalse(ModuleTreeUtil.contains(parentNode1,"service"))
        assertFalse(ModuleTreeUtil.contains(parentNode1,"web"))


        // case2: 只选择 core/model 文件夹,也选择 core 文件夹
        val parentNode2 = FileWrapperTreeNodeFactory.createBundleRootWrapper("mockBundleRoot")
        val importedBundle2 = MockKUtil.readFile("mockproj/mockmultimodule/app")
        val whitelist2 = setOf(
            MockKUtil.readFile("mockproj/mockmultimodule/app/bootstrap").absolutePath,
            MockKUtil.readFile("mockproj/mockmultimodule/app/core").absolutePath,
            MockKUtil.readFile("mockproj/mockmultimodule/app/core/model").absolutePath
        )
        ModuleTreeUtil.importBundle(parentNode2,importedBundle2,"new.mock",whitelist2)
        assertTrue(ModuleTreeUtil.contains(parentNode2,"bootstrap"))
        assertTrue(ModuleTreeUtil.contains(parentNode2,"core"))
        assertTrue(ModuleTreeUtil.contains(parentNode2,"new.mock"))
        assertTrue(ModuleTreeUtil.contains(parentNode2,"model"))
        assertFalse(ModuleTreeUtil.contains(parentNode2,"service"))
        assertTrue(ModuleTreeUtil.contains(parentNode2,"MockModel.java"))
        assertFalse(ModuleTreeUtil.contains(parentNode2,"web"))

    }


    @Test
    fun testImportResourceDirWithoutWhitelist(){
        val parentNode = FileWrapperTreeNodeFactory.createResourceRootWrapper("resources")
        val importedResourceDir = MockKUtil.readFile("mockproj/mockbase/app/bootstrap/src/main/resources/config")

        ModuleTreeUtil.importResourceDir(parentNode,importedResourceDir,null)
        assertTrue(ModuleTreeUtil.contains(parentNode,"config"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"application.properties"))
        assertFalse(ModuleTreeUtil.contains(parentNode,"spring"))
    }

    @Test
    fun testImportResourceDirWithWhitelist(){
        // case1: 只有子文件
        val parentNode1 = FileWrapperTreeNodeFactory.createResourceRootWrapper("resources")
        val importedResourceDir1 = MockKUtil.readFile("mockproj/mockbase/app/bootstrap/src/main/resources/config")
        val whitelist1 = setOf(
            MockKUtil.readFile("mockproj/mockbase/app/bootstrap/src/main/resources/config/application.properties").absolutePath
        )

        ModuleTreeUtil.importResourceDir(parentNode1,importedResourceDir1,whitelist1)
        assertTrue(ModuleTreeUtil.contains(parentNode1,"application.properties"))
        assertFalse(ModuleTreeUtil.contains(parentNode1,"config"))
        assertFalse(ModuleTreeUtil.contains(parentNode1,"spring"))

        // case2: 只有子文件夹的情况
        val parentNode2 = FileWrapperTreeNodeFactory.createResourceRootWrapper("resources")
        val importedResourceDir2 = MockKUtil.readFile("mockproj/mockbase/app/bootstrap/src/main/resources/conf")
        val whitelist2 = setOf(
            importedResourceDir2.absolutePath,
            MockKUtil.readFile("mockproj/mockbase/app/bootstrap/src/main/resources/conf/subconf").absolutePath
        )

        ModuleTreeUtil.importResourceDir(parentNode2,importedResourceDir2,whitelist2)
        assertTrue(ModuleTreeUtil.contains(parentNode2,"submock.properties"))
        assertFalse(ModuleTreeUtil.contains(parentNode2,"config"))
        assertFalse(ModuleTreeUtil.contains(parentNode2,"mock.properties"))
    }

    @Test
    fun testImportPackageWithoutWhitelist(){
        val parentNode = FileWrapperTreeNodeFactory.createPackageRootWrapper("mock")
        val importedPackageDir = MockKUtil.readFile("mockproj/mockbase/app/core/service/src/main/java/com/mock/core/service")

        ModuleTreeUtil.importPackage(parentNode,"mock",importedPackageDir,null)
        assertTrue(ModuleTreeUtil.contains(parentNode,"MockFacadeImpl.java"))
        assertTrue(ModuleTreeUtil.contains(parentNode,"MockFacade.java"))
    }

    @Test
    fun testImportPackageWithWhitelist(){
        // case1: 只有子文件夹
        val parentNode1 = FileWrapperTreeNodeFactory.createPackageRootWrapper("mock")
        val importedPackageDir1 = MockKUtil.readFile("mockproj/mockbase/app/core/service/src/main/java/com/mock/core/service")
        val whitelist1 = setOf(
            MockKUtil.readFile("mockproj/mockbase/app/core/service/src/main/java/com/mock/core/service/impl").absolutePath
        )
        ModuleTreeUtil.importPackage(parentNode1,"mock",importedPackageDir1,whitelist1)
        assertTrue(ModuleTreeUtil.contains(parentNode1,"MockFacadeImpl.java"))
        assertEquals("mock.impl",parentNode1.getName())
        assertFalse(ModuleTreeUtil.contains(parentNode1,"MockFacade.java"))

        // case2: 有自己和子文件的情况
        val parentNode2 = FileWrapperTreeNodeFactory.createPackageRootWrapper("mock")
        val importedPackageDir2 = MockKUtil.readFile("mockproj/mockbase/app/core/service/src/main/java/com/mock/core/service")
        val whitelist2 = setOf(
            importedPackageDir2.absolutePath,
            MockKUtil.readFile("mockproj/mockbase/app/core/service/src/main/java/com/mock/core/service/impl").absolutePath,
            MockKUtil.readFile("mockproj/mockbase/app/core/service/src/main/java/com/mock/core/service/MockFacade.java").absolutePath
        )
        ModuleTreeUtil.importPackage(parentNode2,"mock",importedPackageDir2,whitelist2)
        assertTrue(ModuleTreeUtil.contains(parentNode2,"MockFacadeImpl.java"))
        assertEquals("mock.service",parentNode2.getName())
        assertTrue(ModuleTreeUtil.contains(parentNode2,"MockFacade.java"))
    }

    @Test
    fun testAddPackageWithExpectPackageName(){
        val packageNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("a.b.c")
        ModuleTreeUtil.addPackageWithExpectPackageName(packageNode,"a.s.d")
        assertEquals("a",packageNode.getName())

        assertEquals(2,packageNode.children.size)
        assertTrue(ModuleTreeUtil.contains(packageNode,"b.c"))
        assertTrue(ModuleTreeUtil.contains(packageNode,"s.d"))
    }

    @Test
    fun testModifyNodeName(){
        val node = FileWrapperTreeNode(File("abc"))
        assertFalse(ModuleTreeUtil.modifyNodeName(node,""))
        ModuleTreeUtil.modifyNodeName(node,"aaa")
        assertEquals("aaa",node.getName())
    }

    @Test
    fun testRemovePackage(){
        // case1: 删除的节点只有一层 package 名称
        val comNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("com")
        val mockNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("mock")
        val exampleNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("example")
        buildRelationInOrder(listOf(comNode,mockNode,exampleNode))

        ModuleTreeUtil.removePackage(mockNode)
        assertEquals(exampleNode,comNode.children.first())
        assertEquals(1,comNode.children.size)

        // case2: 删除的节点有多层 package 名称
        val parentNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("com")
        val subNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("mock.example")
        buildRelation(parentNode,subNode)
        ModuleTreeUtil.removePackage(subNode)
        assertEquals(subNode,parentNode.children.first())
        assertEquals("mock",subNode.getName())
    }

    @Test
    fun testRemoveWholePackage(){
        // case1: 删除的节点只有一层 package 名称
        val comNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("com")
        val mockNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("mock")
        buildRelation(comNode,mockNode)
        ModuleTreeUtil.removeWholePackage(mockNode)
        assertTrue(comNode.children.isEmpty())

        // case2: 删除的节点有多层 package 名称
        val parentNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("com")
        val subNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("mock.example")
        val childNode = FileWrapperTreeNodeFactory.createVirtualPackageWrapper("abc")
        buildRelationInOrder(listOf(parentNode,subNode,childNode))
        ModuleTreeUtil.removeWholePackage(subNode)
        assertTrue(subNode.children.isEmpty())
        assertEquals("mock",subNode.getName())
    }

    @Test
    fun testAddSubPackages(){
        val packageRoot = FileWrapperTreeNodeFactory.createPackageRootWrapper("mock")
        ModuleTreeUtil.addSubPackages(packageRoot, listOf("common", "core", "biz", "web"))

        assertEquals(4,packageRoot.children.size)
    }


    @Test
    fun testGetSubPackageByName(){
        val packageRoot = FileWrapperTreeNodeFactory.createPackageRootWrapper("mock")
        ModuleTreeUtil.addSubPackages(packageRoot, listOf("common", "core", "biz", "web"))

        assertNotNull(ModuleTreeUtil.getSubPackageByName(packageRoot,"core"))
        assertNull(ModuleTreeUtil.getSubPackageByName(packageRoot,"abc"))
    }

    @Test
    fun testGetAllSrcPathToNewPath(){
        val rootNode = FileWrapperTreeNodeFactory.createPackageRootWrapper("root")
        val javaFileNode = FileWrapperTreeNodeFactory.createVirtualFileWrapper("SRC.java")
        javaFileNode.newPath = "TGT.java"
        buildRelation(rootNode,javaFileNode)
        val result = ModuleTreeUtil.getAllSrcPathToNewPath(rootNode)
        assertEquals(1,result.size)
        assertEquals("TGT.java",result[javaFileNode.srcFile.absolutePath])
    }


    @Test
    fun testGetAbsoluteNodePath(){
        val rootNode = FileWrapperTreeNodeFactory.createPackageRootWrapper("root")
        val parentNode = FileWrapperTreeNodeFactory.createVirtualFolderWrapper("parent")
        val subNode = FileWrapperTreeNodeFactory.createVirtualFolderWrapper("sub")
        buildRelationInOrder(listOf(rootNode,parentNode,subNode))

        assertEquals(3,ModuleTreeUtil.getAbsoluteNodePath(rootNode,subNode.srcFile.absolutePath, emptyList())!!.size)
        assertNull(ModuleTreeUtil.getAbsoluteNodePath(parentNode,"abc", emptyList()))
    }


    @Test
    fun testIsParentBundle(){
        assertFalse(ModuleTreeUtil.isParentBundle(null))

        val fileNode = FileWrapperTreeNodeFactory.createFileWrapper("file")
        assertFalse(ModuleTreeUtil.isParentBundle(fileNode))

        val bundleNode = FileWrapperTreeNodeFactory.createBundleWrapper("bundle")
        assertFalse(ModuleTreeUtil.isParentBundle(bundleNode))
    }

    @Test
    fun testIsSimpleBundle(){
        assertFalse(ModuleTreeUtil.isSimpleBundle(null))

        val bundleNode = FileWrapperTreeNodeFactory.createBundleWrapper("bundle")
        assertTrue(ModuleTreeUtil.isSimpleBundle(bundleNode))
    }

    @Test
    fun testIsVirtualBundle(){
        assertFalse(ModuleTreeUtil.isVirtualBundle(null))

        val bundleNode = FileWrapperTreeNodeFactory.createBundleWrapper("bundle")
        assertFalse(ModuleTreeUtil.isVirtualBundle(bundleNode))

        val virtualBundleNode = FileWrapperTreeNodeFactory.createVirtualBundleWrapper("bundle")
        assertTrue(ModuleTreeUtil.isVirtualBundle(virtualBundleNode))
    }

    @Test
    fun testIsFolder(){
        assertFalse(ModuleTreeUtil.isFolder(null))
        val fileNode = FileWrapperTreeNodeFactory.createFileWrapper("file")
        assertFalse(ModuleTreeUtil.isFolder(fileNode))
        val folderNode = FileWrapperTreeNodeFactory.createFolderWrapper("folder")
        assertTrue(ModuleTreeUtil.isFolder(folderNode))
    }


    @Test
    fun testIsJavaFile(){
        assertFalse(ModuleTreeUtil.isJavaFile(null))

        val fileNode = FileWrapperTreeNodeFactory.createFileWrapper("file.java")
        assertTrue(ModuleTreeUtil.isJavaFile(fileNode))

        val xmlFileNode = FileWrapperTreeNodeFactory.createFileWrapper("file.xml")
        assertFalse(ModuleTreeUtil.isJavaFile(xmlFileNode))
    }

    @Test
    fun testContainsUserFile(){
        val bundle = FileWrapperTreeNodeFactory.createBundleWrapper("bundle")
        assertFalse(ModuleTreeUtil.containsUserFile(bundle))

        val customFile = FileWrapperTreeNodeFactory.createVirtualFileWrapper("mock")
        buildRelation(bundle,customFile)
        assertTrue(ModuleTreeUtil.containsUserFile(bundle))
    }

    @Test
    fun testAllowToAddVirtualNormalFolder(){
        assertFalse(ModuleTreeUtil.allowToAddVirtualNormalFolder(null))

        val folder = FileWrapperTreeNodeFactory.createFolderWrapper("folder")
        assertTrue(ModuleTreeUtil.allowToAddVirtualNormalFolder(folder))

        val packageNode = FileWrapperTreeNodeFactory.createPackageRootWrapper("packageNode")
        assertFalse(ModuleTreeUtil.allowToAddVirtualNormalFolder(packageNode))
    }

}
