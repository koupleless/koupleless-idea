package com.alipay.sofa.koupleless.kouplelessidea.util.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ClassInfoContext


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/11/11 19:48
 */
object ClassInfoUtil {
    fun getClassInfoByName(className:String,classInfoContexts:List<ClassInfoContext>): ClassInfo?{
        classInfoContexts.forEach {
            if(it.containsClassName(className)){
                return it.getClassInfoByName(className)!!
            }
        }
        return null
    }

    fun getClassInfoByPath(path:String,classInfoContexts: List<ClassInfoContext>): ClassInfo?{
        classInfoContexts.forEach {
             if(it.containsPath(path)){
                 return it.getClassInfoByPath(path)!!
             }
        }
        return null
    }
}
