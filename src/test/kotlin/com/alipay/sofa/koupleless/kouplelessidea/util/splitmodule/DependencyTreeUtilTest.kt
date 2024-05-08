package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependByTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependOnTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileDependencyTreeNode
import org.junit.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/3/17 12:35
 */
class DependencyTreeUtilTest {
    @Test
    fun testIsSubRoot(){
        assertFalse(DependencyTreeUtil.isSubRoot(null))

        val subRoot = FileDependencyTreeNode.DefaultSubRootNode(File("subRoot"))
        assertTrue(DependencyTreeUtil.isSubRoot(subRoot))

        val node = FileDependencyTreeNode.DefaultDependByRootNode()
        assertFalse(DependencyTreeUtil.isSubRoot(node))
    }

    @Test
    fun testIsDependByRoot(){
        assertTrue(DependencyTreeUtil.isDependByRoot(FileDependencyTreeNode.DefaultDependByRootNode()))
        assertFalse(DependencyTreeUtil.isDependByRoot(FileDependencyTreeNode.DefaultDependOnRootNode()))
    }

    @Test
    fun testIsDependOnRoot(){
        assertTrue(DependencyTreeUtil.isDependOnRoot(FileDependencyTreeNode.DefaultDependOnRootNode()))
        assertFalse(DependencyTreeUtil.isDependOnRoot(FileDependencyTreeNode.DefaultDependByRootNode()))
    }

    @Test
    fun testIsDependByNode(){
        assertFalse(DependencyTreeUtil.isDependByNode(null))
        assertTrue(DependencyTreeUtil.isDependByNode(FileDependByTreeNode(File("dependByNode"))))
        assertFalse(DependencyTreeUtil.isDependByNode(FileDependOnTreeNode(File("dependOnNode"))))
    }

    @Test
    fun testIsDependOnNode(){
        assertFalse(DependencyTreeUtil.isDependOnNode(null))
        assertFalse(DependencyTreeUtil.isDependOnNode(FileDependByTreeNode(File("dependByNode"))))
        assertTrue(DependencyTreeUtil.isDependOnNode(FileDependOnTreeNode(File("dependOnNode"))))
    }

    @Test
    fun testIsDependOnOrByNode(){
        assertFalse(DependencyTreeUtil.isDependOnOrByNode(null))
        assertTrue(DependencyTreeUtil.isDependOnOrByNode(FileDependByTreeNode(File("dependByNode"))))
        assertTrue(DependencyTreeUtil.isDependOnOrByNode(FileDependOnTreeNode(File("dependOnNode"))))
        assertFalse(DependencyTreeUtil.isDependOnOrByNode(FileDependencyTreeNode.DefaultDependOnRootNode()))
    }
}
