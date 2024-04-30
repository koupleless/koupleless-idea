package com.alipay.sofa.koupleless.kouplelessidea.parser.visitor.javaparser

import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.ProjectContext
import com.alipay.sofa.koupleless.kouplelessidea.parser.ParseBeanService
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.containsAnnotationWithName
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.filterAnnotations
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.isBasicTypeInJava
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.isClassDeclaration
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.isSimpleType
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.parseFieldName
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.parseFieldTypes
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.AUTOWIRED_ANNOTATIONS
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.SET_METHOD_ANNOTATIONS
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ThisExpr
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import java.nio.file.Path
import java.util.*


/**
 * @description: 解析 java中 bean 依赖哪些 bean
 * @author lipeng
 * @date 2023/9/26 12:07
 */
object BeanDependedOnVisitor:JavaParserVisitor<ProjectContext>() {

    override fun doParse(absolutePath: Path?, compilationUnit: CompilationUnit, arg: ProjectContext?) {
        val classFullName = JavaParserUtil.parseFullName(compilationUnit)
        val beanContext = arg!!.beanContext
        val beanInfo = beanContext.getBeanInfoByClassName(classFullName)
        if(beanInfo!!.definedByXML){
            parseFieldForXMLBean(compilationUnit,beanInfo)
        }else{
            parseFieldForAnnotatedBean(compilationUnit,beanInfo)
        }
    }

    override fun checkPreCondition(absolutePath: Path?,compilationUnit: CompilationUnit,arg:ProjectContext?):Boolean{
        if(!isClassDeclaration(compilationUnit)) return false

        val classFullName = JavaParserUtil.parseFullName(compilationUnit)
        return arg!!.beanContext.containsClassName(classFullName)
    }


    /**
     * 对于 xml 定义的 bean，解析 Field 中依赖的其它 bean
     * 0. 解析有注解的 Field
     * 1. 如果 autowire = "no"，直接不用解析了
     * 2. 其它模式：解析所有非自动注入且 未解析的 field 是否有 setMethod，按照bean的autowiredMode，记录该 field 的信息
     * 2.1 收集所有非自动注入且 未解析的 field
     * 2.2 解析这些属性的SetMethod: 以set开头，变量只有一个，不是简单类型，设置了属性
     * 2.3 记录 BeanRef：
     * 2.3.1 如果 autowire = "byType"，记录变量类型
     * 2.3.2 如果 autowire = "byName"，记录方法名称中 set 后的值，小写第一个字母（除非第二个字母也是大写），为 beanName
     *
     * @param
     * @return
     */
    private fun parseFieldForXMLBean(cu: CompilationUnit, beanInfo: BeanInfo){
        parseAnnotatedField(cu,beanInfo)

        val autowire = beanInfo.beanXmlAutowiredMode
        if(BeanRef.AutowiredMode.NO == autowire){
            return
        }

        if(BeanRef.AutowiredMode.NOT_MATCHED == autowire){
            // TODO:log
            return
        }

        val fieldNameToParse = collectFieldToParse(cu,beanInfo)
        val allFieldWithSetter = containsAnnotationWithName(cu.getType(0), SET_METHOD_ANNOTATIONS)
        if(allFieldWithSetter){
            parseBeanFromSetter(cu,fieldNameToParse,beanInfo)
        }else{
            parseBeanFromSetMethod(cu,fieldNameToParse,beanInfo)
        }
    }



    /**
     * 从 set方法 中解析 bean：
     * 1. set方法是以set开头，入参只有一个，入参不是简单类型，并设置了类属性的方法；
     * 2. 解析 beanName/beanType 和 属性名，并记录。
     * @param
     * @return
     */
    private fun parseBeanFromSetMethod(cu: CompilationUnit, fieldsToParse:MutableMap<String,String>, beanInfo: BeanInfo){
        val type = cu.getType(0)
        type.methods.forEach {
            if(!isSetMethod(it)) return@forEach
            val fieldNamesToParse = fieldsToParse.keys
            val fieldName= parseWrittenFieldName(it,fieldNamesToParse) ?: return@forEach

            when(val autowired = beanInfo.beanXmlAutowiredMode){
                BeanRef.AutowiredMode.BY_NAME ->{
                    val beanNameToParse = parseBeanNameFromSetMethod(it)
                    val beanClassTypeToParse = JavaParserUtil.parseType(it.parameters[0])
                    val fieldType = fieldsToParse[fieldName]!!
                    val beanRef = BeanRef(fieldName,fieldType,beanInfo,beanNameToParse,beanClassTypeToParse,autowired)
                    beanInfo.beanDependOn[fieldName] = beanRef
                }
                BeanRef.AutowiredMode.BY_TYPE ->{
                    val beanNameToParse = parseBeanNameFromSetMethod(it)
                    val beanClassTypeToParse = JavaParserUtil.parseType(it.parameters[0])
                    val fieldType = fieldsToParse[fieldName]!!
                    val beanRef = BeanRef(fieldName,fieldType,beanInfo,beanNameToParse,beanClassTypeToParse,autowired)
                    beanInfo.beanDependOn[fieldName] = beanRef
                }
                else ->{
                    // TODO:log
                }
            }
        }
    }


    /**
     * 从有@Setter的整体类中，解析 Bean：因为该类有@Setter注解，因此每个属性都有SetMethod，解析 beanName/beanType 和 属性名，并记录
     * @param
     * @return
     */
    private fun parseBeanFromSetter(cu: CompilationUnit, fieldsToParse:MutableMap<String,String>, beanInfo: BeanInfo){
        val type = cu.getType(0)
        type.fields.forEach {
            val fieldName = parseFieldName(it)
            if(fieldsToParse.contains(fieldName) && isSimpleType(it) && !isBasicTypeInJava(it.getVariable(0))){
                when(val autowired = beanInfo.beanXmlAutowiredMode){
                    BeanRef.AutowiredMode.BY_NAME ->{
                        val fieldType = fieldsToParse[fieldName]!!
                        val beanRef = BeanRef(fieldName,fieldType,beanInfo,beanNameToParse = fieldName,null,autowired)
                        beanInfo.beanDependOn[fieldName] = beanRef
                    }
                    BeanRef.AutowiredMode.BY_TYPE ->{
                        val beanClassTypeToParse = parseFieldTypes(it).first()
                        // 记录 beanNameToParse 是用于新旧基座不同时，新基座只以 beanId 查询 bean 的调用关系
                        val fieldType = fieldsToParse[fieldName]!!
                        val beanRef = BeanRef(fieldName,fieldType,beanInfo,beanNameToParse = fieldName,beanClassTypeToParse,autowired)
                        beanInfo.beanDependOn[fieldName] = beanRef
                    }
                    else ->{
                        // TODO:log
                    }
                }
            }
        }
    }

    /**
     * 是否为 SetMethod
     * @param
     * @return
     */
    private fun isSetMethod(n: MethodDeclaration):Boolean{
        val methodName = n.nameAsString
        val parameters = n.parameters
        return methodName.startsWith("set") && parameters.size==1 && isSimpleType(parameters.first().type) && !isBasicTypeInJava(parameters[0])
    }

    /**
     * 解析set方法名称中的beanName：记录方法名称中 set 后的值，小写第一个字母（除非第二个字母也是大写），为 beanName
     * @param 
     * @return 
     */
    private fun parseBeanNameFromSetMethod(method:MethodDeclaration):String{
        val methodName = method.nameAsString
        val fieldName = methodName.substringAfter("set")
        return if(fieldName.length >1 && Character.isLowerCase(fieldName[1])) fieldName.replaceFirstChar { it.lowercase(Locale.getDefault()) } else fieldName
    }

    /**
     * 解析set方法中被写入的属性名称：解析方法内容中的赋值语句，如果被赋值的 target 为需要的属性名称，则解析成功，返回属性名称，否则返回null
     * @param
     * @return
     */
    private fun parseWrittenFieldName(n: MethodDeclaration, propertyNames:MutableSet<String>):String?{
        var propertyName:String? = null
        n.accept(object : VoidVisitorAdapter<Void>() {
            override fun visit(n: AssignExpr, arg: Void?) {
                if(n.target is NameExpr){
                    val variableName = (n.target as NameExpr).nameAsString
                    if(propertyNames.contains(variableName)){
                        propertyName = variableName
                    }
                }

                if(n.target is FieldAccessExpr && (n.target as FieldAccessExpr).scope is ThisExpr){
                    val variableName = (n.target as FieldAccessExpr).nameAsString
                    if(propertyNames.contains(variableName)){
                        propertyName = variableName
                    }
                }
            }
        },null)
        return propertyName
    }


    /**
     * 对于注解定义的 bean，解析 Field 需要的 bean
     * 如：@Component
     * public class Person {
     *     @Autowired
     *     private Cat cat;
     * }
     * 其中，Person 为注解定义的 bean，需要的bean为 cat。
     * @param
     * @return
     */
    private fun parseFieldForAnnotatedBean(cu: CompilationUnit, beanInfo: BeanInfo){
        parseAnnotatedField(cu,beanInfo)
        parseConstructor(cu,beanInfo)
    }

    private fun parseConstructor(cu: CompilationUnit, beanInfo: BeanInfo) {
        // 1. 如果有 @RequiredArgsConstructor 注解，那么读取所有的 final 字段
        // @RequiredArgsConstructor 是 Lombok 提供的一个注解，它的作用是为被注解的类生成一个包含所有必需字段的构造函数。
        // 这个构造函数会将所有标记为 final 或有 @NonNull 的字段作为参数，并且在构造函数中进行初始化。
        if(containsAnnotationWithName(cu.getType(0), SplitConstants.ARGS_CONSTRUCTOR_ANNOTATIONS)){
            val type = cu.getType(0)
            type.fields.forEach {field->
                if((field.isFinal || containsAnnotationWithName(field,setOf("NonNull")))&& isSimpleType(field) && !isBasicTypeInJava(field.getVariable(0))){
                    val beanRef = ParseBeanService.parseBeanRef(field,beanInfo)
                    beanRef.autowire = BeanRef.AutowiredMode.BY_TYPE
                    val fieldName = parseFieldName(field)
                    beanInfo.beanDependOn[fieldName] = beanRef
                }
            }
            return
        }
    }

    /**
     * 收集需要解析的属性名：1. 没有自动注入注解 2. 而且未在xml中解析
     * @param
     * @return
     */
    private fun collectFieldToParse(cu: CompilationUnit, beanInfo: BeanInfo):MutableMap<String,String>{
        val fieldsToParse = mutableMapOf<String,String>()

        // 1. 记录没有注解的属性名
        val type = cu.getType(0)
        type.fields.forEach {
            if(!containsAnnotationWithName(it, AUTOWIRED_ANNOTATIONS) && isSimpleType(it)){
                val fieldName = parseFieldName(it)
                val fieldType = JavaParserUtil.parseType(it)
                fieldsToParse[fieldName] = fieldType
            }
        }


        // TODO:之后移到别的地方去：给 xml 中解析过的属性名，添加属性类型
        beanInfo.beanDependOn.filter { (_,beanRef)-> beanRef.definedInXML }.forEach { (_, beanRef) ->
            beanRef.fieldType = fieldsToParse[beanRef.fieldName!!]
        }

        // 2. 移除已经在 xml 中解析过的属性名
        val parsedFieldName = beanInfo.beanDependOn.filter { (_,beanRef)-> beanRef.definedInXML }.map { it.key }.toSet()
        parsedFieldName.forEach {
            fieldsToParse.remove(it)
        }
        return fieldsToParse
    }

    /**
     * 如果是以注解形式注册的 bean， 记录自动注入注解的 field：className, fieldName, autowired, parsedRef=set(ref), parsed
     * @param
     * @return
     */
    private fun parseAnnotatedField(cu: CompilationUnit, beanInfo: BeanInfo){
        val type = cu.getType(0)
        type.fields.forEach {
            val annotations = filterAnnotations(it, AUTOWIRED_ANNOTATIONS)

            if (annotations.isNotEmpty()) {
                val beanRef = ParseBeanService.parseBeanRef(it,beanInfo)
                val fieldName = parseFieldName(it)
                beanInfo.beanDependOn[fieldName] = beanRef
            }
        }
    }
}
