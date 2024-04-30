package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser

import java.util.concurrent.ConcurrentHashMap


/**
 * @description: TODO
 * @author lipeng
 * @date 2023/9/11 17:28
 */
class ClassInfoContext(parent: ProjectContext) {
    private val classNameToClassInfo= ConcurrentHashMap<String, ClassInfo>()
    private val classPathToClassInfo = ConcurrentHashMap<String, ClassInfo>()
    private val parentContext = parent

    fun isEmpty():Boolean{
        return classNameToClassInfo.isEmpty()
    }

    fun containsClassName(fullClassName:String):Boolean{
        return classNameToClassInfo.containsKey(fullClassName)
    }

    fun containsPath(path:String):Boolean{
        return classPathToClassInfo.containsKey(path)
    }

    fun getClassInfoByName(fullClassName: String): ClassInfo?{
        return classNameToClassInfo[fullClassName]
    }

    fun getClassInfoByPath(javaPath:String): ClassInfo?{
        return classPathToClassInfo[javaPath]
    }

    fun clear(){
        classNameToClassInfo.clear()
        classPathToClassInfo.clear()
    }

    fun addClassInfo(classInfo: ClassInfo){
        if(classNameToClassInfo.containsKey(classInfo.fullName)) return
        classNameToClassInfo[classInfo.fullName] = classInfo
        classPathToClassInfo[classInfo.srcPath] = classInfo
    }

    fun getAllClassInfo():Set<ClassInfo>{
        return classNameToClassInfo.values.toSet()
    }

    /**
     * 更新 classInfo 的 path 和 newPackageName
     * @param
     * @return
     */
    fun updateClassInfo(srcPath:String,tgtPath:String){
        val classInfo = classPathToClassInfo[srcPath]
        classInfo?.also {
            it.move(tgtPath)
            classPathToClassInfo[tgtPath] = it
            classPathToClassInfo.remove(srcPath)
        }
    }

    fun clone(context: List<ClassInfo>) {
        clear()
        context.forEach {
            addClassInfo(it)
        }
    }

}
