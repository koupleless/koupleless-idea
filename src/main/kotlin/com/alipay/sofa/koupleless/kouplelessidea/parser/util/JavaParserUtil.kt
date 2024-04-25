package com.alipay.sofa.koupleless.kouplelessidea.parser.util

import com.alipay.sofa.koupleless.kouplelessidea.util.constant.SplitConstants
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.nodeTypes.NodeWithMembers
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.Type


/**
 * @description: java 静态扫描分析工具
 * @author lipeng
 * @date 2023/8/2 18:57
 */
object JavaParserUtil {
    /**
     * 抽取注解中key 对应的 value，如：@Service(key="value")
     * @param annotationExpr 注解
     * @param key
     * @return
     */
    fun getValueOfAnnotation(annotationExpr: NormalAnnotationExpr, key: String): String? {
        val pairs = annotationExpr.asNormalAnnotationExpr().pairs
        val pair = pairs.firstOrNull { pair -> key == pair.nameAsString } ?: return null
        return pair.value?.toString()?.removeSurrounding("\"")
    }

    fun getListValueOfAnnotation(annotationExpr: NormalAnnotationExpr, key: String): List<String> {
        val value = getValueOfAnnotation(annotationExpr, key)
        value?:return emptyList()
        return parseStrToList(value)
    }


    /**
     * 抽取注解中的 value，如：@Service("value")
     * @param annotationExpr 注解
     * @return
     */
    fun getValueOfSingleMemberAnnotation(annotationExpr: SingleMemberAnnotationExpr): String {
        return annotationExpr.memberValue.toString().removeSurrounding("\"")
    }

    fun getListValueOfSingleMemberAnnotation(annotationExpr: SingleMemberAnnotationExpr): List<String> {
        val value = getValueOfSingleMemberAnnotation(annotationExpr)
        return parseStrToList(value)
    }

    private fun parseStrToList(value:String):List<String>{
        if(value.isEmpty()) return emptyList()
        return value.removePrefix("{").removeSuffix("}").replace(("\"|\\s").toRegex(),"").split(",")
    }

    /**
     * 过滤出指定名字的注解列表
     * @param type :类型
     * @param targetAnnotations：目标注解名字
     * @return
     */
    fun filterAnnotations(type: BodyDeclaration<*>, targetAnnotations: Set<String>): List<AnnotationExpr> {
        return type.annotations.filter { annotationExpr -> targetAnnotations.contains(annotationExpr.nameAsString) }
    }

    fun filterAnnotations(type: Parameter, targetAnnotations: Set<String>): List<AnnotationExpr> {
        return type.annotations.filter { annotationExpr -> targetAnnotations.contains(annotationExpr.nameAsString) }
    }

    fun filterAnnotation(type: BodyDeclaration<*>, targetAnnotation: String):AnnotationExpr?{
        return type.annotations.firstOrNull { it.nameAsString == targetAnnotation}
    }

    /**
     * 过滤出 Import 字段
     * @param
     * @return
     */
    fun filterImports(cu:CompilationUnit, targetImports: Set<String>): List<ImportDeclaration> {
        return cu.imports.filter { importDeclaration -> targetImports.contains(importDeclaration.nameAsString) }
    }

    fun filterBeanMethodsByReturnType(type: NodeWithMembers<*>, returnTypes:Set<String>):List<MethodDeclaration>{
        return type.methods.filter{ method ->
            val isBeanMethod = filterAnnotations(method, setOf(SplitConstants.BEAN_ANNOTATION)).isNotEmpty()
            isBeanMethod
        }.filter {method->
            val returnType = parseFullName(method.type)
             returnTypes.contains(returnType)
        }
    }

    fun filterMethodByName(type: NodeWithMembers<*>, name:String):List<MethodDeclaration>{
        return type.getMethodsByName(name)
    }

    fun filterMethodBySignature(type: NodeWithMembers<*>, signature:String):List<MethodDeclaration>{
        return type.methods.filter { parseMethodSignature(it)== signature }
    }

    fun filterMethodCallStatInMethodBody(method: MethodDeclaration, statement:String):List<MethodCallExpr>{
        return method.body.get().findAll(MethodCallExpr::class.java).filter { it.nameAsString ==statement }
    }

    fun filterObjectCreationStatInMethodBody(method: MethodDeclaration,typeName:String):List<ObjectCreationExpr>{
        return method.body.get().findAll(ObjectCreationExpr::class.java).filter { it.typeAsString ==typeName }
    }

    fun filterFieldByName(type: NodeWithMembers<*>, fieldName:String):FieldDeclaration?{
        return type.fields.firstOrNull { parseFieldName(it) == fieldName }
    }

    fun containsAnnotationWithName(type: BodyDeclaration<*>, targetAnnotations: Set<String>):Boolean{
        return filterAnnotations(type,targetAnnotations).isNotEmpty()
    }

    fun containsAnnotationWithName(type: Parameter, targetAnnotations: Set<String>):Boolean{
        return filterAnnotations(type,targetAnnotations).isNotEmpty()
    }

    fun filterAnnoAttributePair(anno:AnnotationExpr,attribute:String):MemberValuePair?{
        if(!anno.isNormalAnnotationExpr) return null
        return anno.asNormalAnnotationExpr().pairs.firstOrNull{ pair -> attribute == pair.nameAsString }
    }

//    fun getAnnoAttributeValueAsString(anno:AnnotationExpr, attribute:String):String?{
//        val pair = filterAnnoAttributePair(anno,attribute)
//        return pair?.value?.toString()?.removeSurrounding("\"")
//    }

    /**
     * 是否为类的声明
     * @param
     * @return
     */
    fun isClassDeclaration(compilationUnit: CompilationUnit):Boolean{
        if(compilationUnit.types.isEmpty()) return false
        val type = compilationUnit.types[0]
        return type is ClassOrInterfaceDeclaration && !type.isInterface
    }

    fun isAnnotation(compilationUnit: CompilationUnit):Boolean{
        if(compilationUnit.types.isEmpty()) return false
        val type = compilationUnit.types[0]
        return type is AnnotationDeclaration
    }

    fun isInterface(compilationUnit: CompilationUnit):Boolean{
        if(compilationUnit.types.isEmpty()) return false
        val type = compilationUnit.types[0]
        return type is ClassOrInterfaceDeclaration && type.isInterface
    }

    fun isValidJavaFile(cu:CompilationUnit):Boolean{
        return cu.packageDeclaration.isPresent && cu.types.isNotEmpty()
    }

    /**
     * 是否实现了接口
     * @param type :类型
     * @return
     */
    private fun hasInterfaces(type: TypeDeclaration<*>):Boolean{
        return type.isClassOrInterfaceDeclaration && !(type as ClassOrInterfaceDeclaration).isInterface && type.implementedTypes.isNotEmpty()
    }

    fun parsePackageName(cu: CompilationUnit):String{
        return cu.packageDeclaration.get().name.toString()
    }

    fun parseFullName(cu: CompilationUnit):String{
        return cu.getType(0).fullyQualifiedName.get()
    }

    fun parseClassName(cu:CompilationUnit):String{
        return cu.getType(0).nameAsString
    }

    fun parseAnnotation(cu:CompilationUnit):Set<String>{
        return cu.getType(0).annotations.map { it.nameAsString }.toSet()
    }

    /**
     * 解析接口的全限定名，如 A implements B, C<D>，返回 B 和 C 的全限定名
     * @param cu :
     * @return
     */
    fun parseImplements(cu:CompilationUnit):Set<String>{
        val type = cu.getType(0)
        if(!hasInterfaces(type)) return emptySet()

        val interfaceSet = mutableSetOf<String>()
        return (type as ClassOrInterfaceDeclaration).implementedTypes.mapNotNullTo(interfaceSet) { parseSplitFullNames(it).first() }
    }

    fun parseExtendClass(cu: CompilationUnit):Set<String>{
        val type = cu.getType(0)
        if(!type.isClassOrInterfaceDeclaration) return emptySet()
        return type.asClassOrInterfaceDeclaration().extendedTypes.mapNotNull { parseClassOrInterfaceType(it) }.toSet()
    }

    fun parseMethodSignature(method: MethodDeclaration):String{
        val methodName = method.nameAsString
        val params = method.parameters.joinToString(separator = ",", prefix = "(", postfix = ")") {
            parseFullName(it.type)
        }
        return methodName+params
    }

    private fun parseSplitFullNamesFromParser(type:Type):Set<String>{
        val described = type.resolve().describe()
        if(described.contains(">")){
            return described.substringBefore(">").replace(" ","").split(Regex("[<,]")).toSet()
        }
        return setOf(described)
    }

    private fun parseFullNameFromParser(type:Type):String{
        return type.resolve().describe()
    }

    fun isBasicTypeInJava(param: Parameter):Boolean{
        try {
            return parseFullName(param.type).startsWith("java")
        }catch (e:Exception){
            return false
        }
    }

    fun isBasicTypeInJava(variable: VariableDeclarator):Boolean{
        try {
            return parseFullName(variable.type).startsWith("java")
        }catch (e:Exception){
            return false
        }
    }

    fun parseFieldName(n: FieldDeclaration):String{
        return n.getVariable(0).nameAsString
    }

    fun parseType(n: FieldDeclaration):String{
        return parseFullName(n.getVariable(0).type)
    }

    fun parseFieldTypes(n:FieldDeclaration):Set<String>{
        return parseSplitFullNames(n.getVariable(0).type)
    }

    fun parseType(param:Parameter):String{
        return parseFullName(param.type)
    }

    fun isClassOrInterfaceType(type:Type):Boolean{
        if(!type.isClassOrInterfaceType) return false

        val typeArguments = type.asClassOrInterfaceType().typeArguments
        if(typeArguments.isPresent){
            return typeArguments.get().all {isClassOrInterfaceType(it)}
        }
        return true
    }

    fun parseSplitFullNames(type:Type):Set<String>{
        return if(isClassOrInterfaceType(type)){
            parseSplitClassOrInterfaceType(type.asClassOrInterfaceType())
        }else{
            parseSplitFullNamesFromParser(type)
        }
    }

    fun parseFullName(anno: AnnotationExpr):String{
        val parseFromImport =  solveFromImport(anno.findCompilationUnit().get(),anno.nameAsString)
        if(null!=parseFromImport) return parseFromImport
        return anno.resolve().className
    }

    fun parseFullName(type:Type):String{
        return if(isClassOrInterfaceType(type)){
            parseClassOrInterfaceType(type.asClassOrInterfaceType())
        }else{
            parseFullNameFromParser(type)
        }
    }

    private fun parseSplitClassOrInterfaceType(type: ClassOrInterfaceType):Set<String>{
        val subSimpleTypes = getSubTypes(type)
        val parsedSubSimpleType = subSimpleTypes.map {subType -> solveFromImport(subType) }.mapNotNull { it }.toSet()

        // 简单解析完成
        if(parsedSubSimpleType.size==subSimpleTypes.size){
            return parsedSubSimpleType
        }

        try {
            return parseSplitFullNamesFromParser(type)
        } catch (e: Exception) {
            // log
            return parsedSubSimpleType
        }
    }

    private fun parseClassOrInterfaceType(type: ClassOrInterfaceType):String{
        val subSimpleTypes = getSubTypes(type)
        val parsedSubSimpleType = subSimpleTypes.map {subType -> solveFromImport(subType) }.mapNotNull { it }.toList()

        // 简单解析完成
        if(parsedSubSimpleType.size==subSimpleTypes.size){
            return parsedSubSimpleType.joinToString(separator = "<")+">".repeat(parsedSubSimpleType.size-1)
        }

        try {
            return parseFullNameFromParser(type)
        } catch (e: Exception) {
            // log
            return parsedSubSimpleType.joinToString(separator = "<")+">".repeat(parsedSubSimpleType.size-1)
        }
    }

    private fun solveFromImport(type:ClassOrInterfaceType):String?{
        val cu  = type.findCompilationUnit().get()
        val imports = cu.imports
        val name = type.nameAsString
        val possibleImport = imports.filter {
            val importedName = it.nameAsString.substringAfterLast(".").removeSuffix(";")
            importedName == name
        }.map { it.nameAsString }
        // 如果唯一
        if(possibleImport.size==1){
            return possibleImport.first().substringAfter("import").removeSuffix(";").trim()
        }
        return null
    }

    fun solveFromImport(cu:CompilationUnit,className:String):String?{
        val imports = cu.imports
        val possibleImport = imports.filter {
            val importedName = it.nameAsString.substringAfterLast(".").removeSuffix(";")
            importedName == className
        }.map { it.nameAsString }
        // 如果唯一
        if(possibleImport.size==1){
            return possibleImport.first().substringAfter("import").removeSuffix(";").trim()
        }
        return null
    }

    private fun getSubTypes(type: ClassOrInterfaceType):List<ClassOrInterfaceType> {
        val res = mutableListOf<ClassOrInterfaceType>()
        res.add(type)
        if(type.typeArguments.isPresent){
            type.typeArguments.get().forEach {
                if(it.isClassOrInterfaceType) {
                    res.addAll(getSubTypes(it.asClassOrInterfaceType()))
                }
            }
        }
        return res
    }

    fun isSimpleType(type:Type):Boolean{
        return type.isClassOrInterfaceType && !type.asClassOrInterfaceType().nameAsString.contains("<")
    }

    fun isSimpleType(n: FieldDeclaration):Boolean{
        return isSimpleType(n.getVariable(0).type)
    }

}
