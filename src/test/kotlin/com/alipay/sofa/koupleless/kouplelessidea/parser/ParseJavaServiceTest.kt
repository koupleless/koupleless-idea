package com.alipay.sofa.koupleless.kouplelessidea.parser

import cn.hutool.core.util.RuntimeUtil
import cn.hutool.core.util.RuntimeUtil.execForStr
import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/3/16 09:59
 */
class ParseJavaServiceTest {
    @Test
    fun testInitParserConfiguration(){
        mockkStatic(RuntimeUtil::class){
            mockkObject(ParseJavaService){
                every { ParseJavaService.parseDependentJar(any()) } returns listOf(MockKUtil.readFile("mockMvnRepository/com/alipay/sofa/web/base-web-single-host-facade/0.0.1-SNAPSHOT/base-web-single-host-facade-0.0.1-SNAPSHOT.jar"))
                ParseJavaService.initParserConfiguration(MockKUtil.getTestResourcePath("mockproj/mockbase"))
            }
        }
    }

    @Test
    fun testParseDependentJar(){
        mockkStatic(RuntimeUtil::class){
            every {  execForStr(any<String>(),any<String>(),any<String>(),any<String>())} returns MockKUtil.getTestResourcePath("mockMvnRepository")
            val jars = ParseJavaService.parseDependentJar(MockKUtil.getTestResourcePath("mockproj/mockbase"))
            assertEquals(MockKUtil.getTestResourcePath("mockMvnRepository/com/alipay/sofa/web/base-web-single-host-facade/0.0.1-SNAPSHOT/base-web-single-host-facade-0.0.1-SNAPSHOT.jar"),jars.first().absolutePath)
        }
    }
}
