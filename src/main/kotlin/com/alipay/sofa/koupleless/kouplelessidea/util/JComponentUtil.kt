package com.alipay.sofa.koupleless.kouplelessidea.util

import java.awt.Component
import javax.swing.JPanel


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/10/20 15:55
 */
object JComponentUtil {
    fun disableComponent(component: Component){
        component.isEnabled=false
        if(component is JPanel){
            component.components.forEach {
                disableComponent(it)
            }
        }
    }

    fun enableComponent(component: Component){
        component.isEnabled=true
        if(component is JPanel){
            component.components.forEach {
                enableComponent(it)
            }
        }
    }
}
