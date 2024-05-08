package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/1/6 15:09
 */
class ModuleTemplateUtilTest {
    @Test
    fun testBuildSingleBundleTemplateTree(){
        val template = SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag
        val moduleName = "testModule"
        val packageName = "com.mock.module"

        val root = ModuleTemplateUtil.buildModuleTree(template, moduleName, packageName)
        assertNotNull(root)
        assertEquals(moduleName,root.getName())

        val generatedPackageName = ModuleTreeUtil.getPackageRoot(root)?.getName()
        assertEquals(packageName, generatedPackageName)
    }

    @Test
    fun testBuildNullTemplateTree(){
        val moduleName = "testModule"
        val packageName = "com.mock.module"
        val root = ModuleTemplateUtil.buildModuleTree("null", moduleName, packageName)
        assertNotNull(root)
        assertEquals(moduleName,root.getName())

        val generatedPackageName = ModuleTreeUtil.getPackageRoot(root)?.getName()
        assertEquals(packageName, generatedPackageName)
    }
}
