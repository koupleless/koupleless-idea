package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule

import com.alipay.sofa.koupleless.kouplelessidea.model.ArchetypeInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.BaseData

data class ModuleDescriptionInfo(
    val srcBaseInfo: BaseData?,
    val tgtBaseLocation: String?,
    val name: String?,
    val groupId: String?,
    val artifactId:String?,
    val packageName:String?,
    val templateType:String?,
    val template: ArchetypeInfo?,
    val mode:String?,
    val location:String?,
    val splitToOtherBase:Boolean?
)

