package com.alipay.sofa.koupleless.kouplelessidea.model.splitmodule.staticparser


/**
 * @description: 用于记录 Bean 对其它 Bean 的引用。其它 Bean 需要经过解析才能得到，解析方式：
 * 1. xml 中，定义 bean 的引用，如：
 * <bean id="person" class="com.example.SpringBoot.Person">
 *    <property name="cat" ref="cat"></property>
 * </bean>
 * 2. java 代码中，通过注解进行引用，如：
 * @Autowired
 * private OrderRepository orderRepository;
 * 3. xml 中，定义了自动注入模式，java 代码中通过 set方法进行注入，如：
 * <bean id="person" class="com.example.SpringBoot.Person" autowire="byName">
 * </bean>
 * <bean id="cat" class="com.example.SpringBoot.Cat"></bean>
 * public class Person {
 *      private Cat cat;
 *
 *      public void setCat(Cat cat) {
 *         this.cat = cat;
 *     }
 * }
 * @author lipeng
 * @date 2023/9/26 15:17
 */
class BeanRef(fieldName:String?,fieldType: String?,parentBean: BeanInfo){
    var fieldType:String?
    val fieldName:String?
    val parentBean: BeanInfo
    var beanNameToParse:String?=null
    var beanTypeToParse:String?=null
    /**
     * 四种方式：byType, byName, TypeFirst, NameFirst, no
     */
    var autowire: AutowiredMode = AutowiredMode.TYPE_FIRST


    /**
     * 解析的 Ref 结果：所有 beanName -> beanInfo
     */
    val parsedRef = mutableSetOf<BeanInfo>()
    /**
     * TODO：后续这个字段和 beanNameToParse 融合
     */
    var beanNameDefinedInXML:MutableSet<String>? = null

    var definedInXML = false

    var definedInMethod = false

    init {
        this.fieldName=fieldName
        this.fieldType=fieldType
        this.parentBean = parentBean
    }

    constructor(fieldName:String?, fieldType:String?, parentBean: BeanInfo, beanNameToParse:String?, beanTypeToParse:String?, autowire: AutowiredMode?) : this(fieldName,fieldType,parentBean) {
        this.beanTypeToParse = beanTypeToParse
        this.beanNameToParse = beanNameToParse
        this.autowire = autowire?: AutowiredMode.NOT_MATCHED
    }

    constructor(fieldName: String, fieldType:String?, parentBean: BeanInfo, beanNameDefinedInXML: MutableSet<String>, autowire: AutowiredMode?):this(fieldName,fieldType,parentBean){
        this.beanNameDefinedInXML= beanNameDefinedInXML
        this.autowire = autowire?: AutowiredMode.NOT_MATCHED
        this.definedInXML = true
    }

    enum class AutowiredMode(mod:String){

        BY_TYPE("byType"),
        BY_NAME("byName"),
        TYPE_FIRST("byTypeFirst"),
        NAME_FIRST("byNameFirst"),
        NO("no"),
        NOT_MATCHED("noMatched");

        private val mode = mod


        companion object{
            fun getByMode(mode:String): AutowiredMode {
                values().forEach {
                    if(it.mode == mode){
                        return it
                    }
                }

                return NOT_MATCHED
            }
        }
    }

    fun parsedAs(beanInfo: BeanInfo){
        // parentBean 依赖 beanInfo
        parsedRef.add(beanInfo)

        // beanInfo 被 parentBean 依赖
        beanInfo.beanDependBy.add(parentBean)
    }

    fun getBeanType():String?{
        return beanTypeToParse?:fieldType
    }
}
