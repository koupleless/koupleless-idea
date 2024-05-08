package com.alipay.sofa.koupleless.kouplelessidea.pipelineplugin.construct

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.FileUtil.del
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.SplitModuleContext
import com.alipay.sofa.koupleless.kouplelessidea.service.splitmodule.pipelineplugin.construct.ClearTemplatePlugin
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import io.mockk.*
import org.junit.Test
import java.io.File


/**
 * @description:
 * @author lipeng
 * @date 2024/2/20 00:18
 */
class ClearTemplatePluginTest {

    @Test
    fun testDoProcessWithSingle(){
        val splitModuleContext = mockSplitModuleContextWithSingle()

        mockkStatic(FileUtil::class){
            every { del(any<File>()) } returns true
            ClearTemplatePlugin.doProcess(splitModuleContext)
            verify (exactly = 2){ del(any<File>()) }
        }
    }



    private fun mockSplitModuleContextWithSingle(): SplitModuleContext {
        val splitModuleContext = mockk<SplitModuleContext>{
            every { moduleContext } returns mockk<ModuleContext>{
                every { moduleTemplateType } returns SplitConstants.Labels.SINGLE_BUNDLE_TEMPLATE.tag
                every { packageName } returns "com.singlemodule"
                every { getModulePath() } returns MockKUtil.getTestResourcePath("mockproj/mocksinglemodule")
            }
        }
        return splitModuleContext
    }
}
