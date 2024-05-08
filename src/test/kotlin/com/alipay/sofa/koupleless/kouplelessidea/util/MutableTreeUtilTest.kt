package com.alipay.sofa.koupleless.kouplelessidea.util

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule.ModuleTemplateUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.ui.MutableTreeUtil
import org.junit.Test
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.test.assertNotNull
import kotlin.test.assertNull


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/3/17 10:12
 */
class MutableTreeUtilTest {
    @Test
    fun testSearch(){
        val moduleTree = ModuleTemplateUtil.buildModuleTree("SINGLE_BUNDLE_TEMPLATE","module","com.mock.mo")
        val root = buildDefaultMutableTree(moduleTree)
        assertNotNull(MutableTreeUtil.search(root, "com.mock.mo"))
        assertNull(MutableTreeUtil.search(root, "com.mock.mo.notExist"))
    }

    private fun buildDefaultMutableTree(root: FileWrapperTreeNode): DefaultMutableTreeNode {
        val tree = DefaultMutableTreeNode(root)
        root.children.sortBy{it.getName()}
        for (child in root.children){
            tree.add(buildDefaultMutableTree(child))
        }
        return tree
    }
}
