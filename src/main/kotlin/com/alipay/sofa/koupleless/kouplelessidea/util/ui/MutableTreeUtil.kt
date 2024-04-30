package com.alipay.sofa.koupleless.kouplelessidea.util.ui

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.FileWrapperTreeNode
import javax.swing.tree.DefaultMutableTreeNode


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/16 10:46
 */
object MutableTreeUtil {
    fun search(root: DefaultMutableTreeNode, text: String): DefaultMutableTreeNode? {
        val fileNode = root.userObject as FileWrapperTreeNode
        if(fileNode.getName().lowercase().contains(text)){
            return root
        }

        if(root.childCount==0){
            return null
        }

        for (child in root.children()){
            val searched = search(child as DefaultMutableTreeNode,text)
            if(searched!=null){
                return searched
            }
        }
        return null
    }
}
