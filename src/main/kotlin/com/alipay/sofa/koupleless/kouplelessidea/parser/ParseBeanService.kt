package com.alipay.sofa.koupleless.kouplelessidea.parser

import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanInfo
import com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser.BeanRef
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.parseFieldName
import com.alipay.sofa.koupleless.kouplelessidea.parser.util.JavaParserUtil.parseType
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.AUTOWIRED_ANNOTATIONS
import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants.Companion.BEAN_ANNOTATIONS
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import java.util.*


/**
 * @description: Bean 静态扫描解析工具
 * @author lipeng
 * @date 2023/8/7 20:57
 */
object ParseBeanService {
    /**
     * 获取实现类的beanName，如传入以下 ServiceImpl 的 TypeDeclaration，会解析得到 beanName 为"XXX"
     * *****
     * @Service("XXX")
     * Class ServiceImpl
     * *****
     *
     * @param type 项目路径
     * @return
     */
    fun getBeanName(type: TypeDeclaration<*>):String{
        var beanName = ""

        val beanAnnotations = JavaParserUtil.filterAnnotations(type, BEAN_ANNOTATIONS)

        if(beanAnnotations.isNotEmpty()){
            // 默认 beanName
            beanName = defaultBeanNameOfClass(type.nameAsString)

            // 配置 beanName: bean 注解中可以指定 value 作为 beanName
            for(annotationExpr in beanAnnotations){
                when(annotationExpr){
                    // 如：@Component("XXX")
                    is SingleMemberAnnotationExpr -> beanName =
                        JavaParserUtil.getValueOfSingleMemberAnnotation(annotationExpr)
                    // 如：@Component(value = "XXX")
                    is NormalAnnotationExpr -> beanName = (JavaParserUtil.getValueOfAnnotation(annotationExpr, "value")?: beanName)
                }
            }
        }
        return beanName
    }

    /**
     * // @Bean("myBean1")
     * // @Bean({"myBean1", "myBean2"})
     * @param
     * @return
     */
    fun parseBeanName(method: MethodDeclaration):Set<String>{
        val beanAnno = JavaParserUtil.filterAnnotations(method, setOf(SplitConstants.BEAN_ANNOTATION)).firstOrNull()
        beanAnno ?: return emptySet()

        val beanNames = mutableSetOf<String>()

        when(beanAnno){
            // 如：@Bean, 默认 beanName 为方法名
            is MarkerAnnotationExpr -> {
                beanNames.add(method.nameAsString)
            }
            // 如：@Bean("myBean1") 或者 @Bean({"myBean1", "myBean2"})
            is SingleMemberAnnotationExpr -> {
                beanNames.addAll(JavaParserUtil.getListValueOfSingleMemberAnnotation(beanAnno))
            }
            // 如：@Bean(name="myBean1") 或者 @Bean(name={"myBean1", "myBean2"})
            // 如：@Bean(value="myBean1") 或者 @Bean(value={"myBean1", "myBean2"})
            is NormalAnnotationExpr -> {
                val nameFromValue = JavaParserUtil.getListValueOfAnnotation(beanAnno, "value")
                val nameFromName = JavaParserUtil.getListValueOfAnnotation(beanAnno, "name")
                beanNames.addAll(nameFromValue)
                beanNames.addAll(nameFromName)
                if(nameFromName.isEmpty()&&nameFromValue.isEmpty()){
                    beanNames.add(method.nameAsString)
                }
            }

        }

        return beanNames
    }


    /**
     * 解析字段声明的 beanName，如传入以下 serviceImpl 的 FieldDeclaration，会解析得到 beanName 为"serviceImpl"
     * *****
     * @Autowired
     * Service serviceImpl
     * *****
     * @param n 字段的声明
     * @return
     */
    private fun parseBeanName(n: FieldDeclaration): String{
        val annotations = JavaParserUtil.filterAnnotations(n, AUTOWIRED_ANNOTATIONS)

        val qualifier = annotations.firstOrNull { annotationExpr -> "Qualifier" == annotationExpr.nameAsString }
        val parsedFromQualifier = parseQualifierBeanName(qualifier)
        if(null!= parsedFromQualifier) return parsedFromQualifier

        val resource = annotations.firstOrNull { annotationExpr -> "Resource" == annotationExpr.nameAsString }
        val parsedFromResource = parseResourceBeanName(resource)
        if(null!=parsedFromResource) return parsedFromResource

        return defaultBeanNameOfField(n)
    }

    fun parseBeanName(param: Parameter): String{
        val annotations = JavaParserUtil.filterAnnotations(param, AUTOWIRED_ANNOTATIONS)
        if(annotations.isEmpty()){
            return param.nameAsString
        }
        val qualifier = annotations.firstOrNull { annotationExpr -> "Qualifier" == annotationExpr.nameAsString }
        val parsedFromQualifier = parseQualifierBeanName(qualifier)
        if(null!= parsedFromQualifier) return parsedFromQualifier

        val resource = annotations.firstOrNull { annotationExpr -> "Resource" == annotationExpr.nameAsString }
        val parsedFromResource = parseResourceBeanName(resource)
        if(null!=parsedFromResource) return parsedFromResource

        return param.nameAsString
    }

    fun parseBeanType(param: Parameter):String{
        return JavaParserUtil.parseFullName(param.type)
    }

    fun parseBeanRef(param: Parameter, parentBeanInfo:BeanInfo):BeanRef{
        val beanTypeToParse = parseBeanType(param)
        val autowiredMode = parseAutowiredMode(param)
        val beanNameToParse = parseBeanName(param)
        val beanRef = BeanRef(fieldName = null, fieldType = null,parentBeanInfo,beanNameToParse,beanTypeToParse,autowiredMode)
        beanRef.definedInMethod = true
        return beanRef
    }

    fun parseBeanRef(field: FieldDeclaration, beanInfo: BeanInfo): BeanRef {
        val beanNameToParse = parseBeanName(field)
        val fieldName = parseFieldName(field)
        val fieldType = parseType(field)
        val autowired = parseAutowiredMode(field)
        return BeanRef(fieldName, fieldType, beanInfo, beanNameToParse, fieldType, autowired)
    }

    fun parseBeanRef(method: MethodDeclaration, beanInfo: BeanInfo): BeanRef {
        val beanNameToParse = parseBeanName(method).first()
        val beanTypeToParse = JavaParserUtil.parseFullName(method.type)
        return BeanRef(null,null,beanInfo,beanNameToParse,beanTypeToParse,BeanRef.AutowiredMode.NAME_FIRST)
    }

    fun parseAutowiredMode(n: FieldDeclaration): BeanRef.AutowiredMode?{
        val autowiredByName = JavaParserUtil.containsAnnotationWithName(n, SplitConstants.AUTOWIRED_BY_NAME_ANNOTATIONS)
        val autowiredByTypeFirst = JavaParserUtil.containsAnnotationWithName(
            n,
            SplitConstants.AUTOWIRED_BY_TYPE_FIRST_ANNOTATIONS
        ) && !JavaParserUtil.containsAnnotationWithName(n, SplitConstants.AUTOWIRED_BY_NAME_ANNOTATIONS)
        // TODO：注意，这里没有对Resource做Type的提取，未来可做。
        val autowiredByNameFirst =
            JavaParserUtil.containsAnnotationWithName(n, SplitConstants.AUTOWIRED_BY_NAME_FIRST_ANNOTATIONS)

        val autowired = if(autowiredByName){
            BeanRef.AutowiredMode.BY_NAME
        }else if(autowiredByTypeFirst){
            BeanRef.AutowiredMode.TYPE_FIRST
        }else if(autowiredByNameFirst){
            BeanRef.AutowiredMode.NAME_FIRST
        }else // TODO: 这里应该打印日志
        {
            null
        }
        return autowired
    }

    private fun parseAutowiredMode(param: Parameter):BeanRef.AutowiredMode{
        val autowiredByName = JavaParserUtil.containsAnnotationWithName(param, SplitConstants.AUTOWIRED_BY_NAME_ANNOTATIONS)
        val autowiredByNameFirst = JavaParserUtil.containsAnnotationWithName(param, SplitConstants.AUTOWIRED_BY_NAME_FIRST_ANNOTATIONS)

        val autowired = if(autowiredByName){
            BeanRef.AutowiredMode.BY_NAME
        }else if(autowiredByNameFirst){
            BeanRef.AutowiredMode.NAME_FIRST
        } else{
            BeanRef.AutowiredMode.TYPE_FIRST
        }
        return autowired
    }

    private fun parseQualifierBeanName(qualifier: AnnotationExpr?):String?{
        qualifier?:return null
        // @Qualifier：@Qualifier(value = "XXX") 或 @Qualifier("XXX")，以 value 中的值为 beanName
        val beanName = when(qualifier){
            is SingleMemberAnnotationExpr ->  JavaParserUtil.getValueOfSingleMemberAnnotation(qualifier)
            is NormalAnnotationExpr ->  JavaParserUtil.getValueOfAnnotation(qualifier, "value")!!
            else -> null
        }
        return beanName
    }


    private fun parseResourceBeanName(resource: AnnotationExpr?):String?{
        resource?:return null
        val beanName = when(resource){
            // @Resource：@Resource(name = "XXX")，以 name 中的值为 beanName
            is SingleMemberAnnotationExpr ->  JavaParserUtil.getValueOfSingleMemberAnnotation(resource)
            else -> null
        }
        return beanName
    }

    /**
     * 获取字段声明的默认 beanName，如传入 serviceImpl 的 FieldDeclaration，会解析得到默认的 beanName "serviceImpl"
     * *****
     * Service serviceImpl
     * *****
     * @param n 字段声明
     * @return
     */
    fun defaultBeanNameOfField(n: FieldDeclaration): String{
        return parseFieldName(n)
    }

    /**
     * 获取 className 的默认 beanName，默认 beanName 为首字母小写的类全名（如果第二个字母是大写，那么首字母无须小写）
     * 如传入 className = ServiceImpl，会解析得到默认的 beanName "serviceImpl"
     * *****
     * @Service("XXX")
     * Class ServiceImpl
     * *****
     * @param className 类名
     * @return
     */
    fun defaultBeanNameOfClass(className:String):String{
        return if(className.length>1 && Character.isLowerCase(className[1])) className.replaceFirstChar { it.lowercase(Locale.getDefault()) } else className
    }

    fun defaultBeanNameOfQualifiedName(qualifiedName: String?):String?{
        if(StrUtil.isEmpty(qualifiedName)){
            return null
        }
        val className = qualifiedName!!.substringAfterLast(".")
        return defaultBeanNameOfClass(className)
    }
}
