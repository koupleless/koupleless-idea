package com.alipay.sofa.koupleless.kouplelessidea.parser.util

import org.apache.commons.configuration2.YAMLConfiguration
import java.io.File
import java.io.IOException


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/10 14:16
 */
object ConfigurationUtil {
    fun readYaml(filePath:String): YAMLConfiguration{
        try {
            val yamlConfig = YAMLConfiguration()
            val inputStream = File(filePath).inputStream()
            inputStream.use {
                yamlConfig.read(it)
            }
            return yamlConfig
        } catch (e: IOException) {
            throw RuntimeException("ERROR, MavenPomUtil:buildPomModel for $filePath", e)
        }
    }
}
