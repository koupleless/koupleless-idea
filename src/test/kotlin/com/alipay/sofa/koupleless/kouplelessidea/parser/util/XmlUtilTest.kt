package com.alipay.sofa.koupleless.kouplelessidea.parser.util

import com.alipay.sofa.koupleless.kouplelessidea.util.MockKUtil
import org.junit.Test
import kotlin.test.assertEquals


/**
 * @description: TODO
 * @author lipeng
 * @date 2024/3/17 10:23
 */
class XmlUtilTest {
    @Test
    fun testParseXMLEncoding(){
        assertEquals("UTF-8",XmlUtil.parseXMLEncoding(MockKUtil.readFile("mockproj/mockbase/app/bootstrap/src/main/resources/module_mapping.xml")))
    }
}
