package com.alipay.sofa.koupleless.kouplelessidea.util

import cn.hutool.core.util.RuntimeUtil
import com.intellij.execution.ExecutionException

/**
 * @description: 执行命令
 * @author lipeng
 * @date 2023/6/16 23:44
 */
object CommandUtil {
    fun execSync(command: String){
        val process = RuntimeUtil.exec("/bin/bash", "-c", "-l",command)
        if(0!=process.waitFor()){
            throw ExecutionException("执行失败")
        }
    }
}
