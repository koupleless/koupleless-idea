package com.alipay.sofa.koupleless.kouplelessidea.util.constant

import com.alipay.sofa.koupleless.kouplelessidea.model.ArchetypeInfo
import com.intellij.ui.ColoredListCellRenderer
import javax.swing.JList


/**
 * @description: 拆分模块常值
 * @author lipeng
 * @date 2023/9/26 11:38
 */
interface SplitConstants {

    companion object{
        /**
         * 分析 xml 中 bean依赖，从 attributes 中引用: ref, value-ref, key-ref
         */
        val BEAN_REF_XML_ATTRIBUTES = listOf("ref","value-ref","key-ref")
        /**
         * 分析 xml 中 bean依赖，从 elements 中引用: ref, idref (注入该bean的name)
         */
        val BEAN_REF_XML_ELEMENTS = listOf("ref","idref")
        /**
         * 分析 xml 中 bean依赖，从 elements 的 attribute 中引用: bean
         */
        const val BEAN_REF_XML_ATTRIBUTE_IN_ELEMENT = "bean"


        /**
         * 模块拆分需要扫描修改的注解
         */
        val AUTOWIRED_ANNOTATIONS = setOf("Autowired","Resource","Qualifier")

        val MYBATIS_SQL_ANNOTATION_IMPORTS = setOf("org.apache.ibatis.annotations.Delete","org.apache.ibatis.annotations.Insert","org.apache.ibatis.annotations.Select","org.apache.ibatis.annotations.Update")

        val AUTOWIRED_BY_NAME_ANNOTATIONS = setOf("Qualifier")

        val AUTOWIRED_BY_TYPE_FIRST_ANNOTATIONS = setOf("Autowired")

        val AUTOWIRED_BY_NAME_FIRST_ANNOTATIONS = setOf("Resource")

        val SET_METHOD_ANNOTATIONS = setOf("Setter")

        val ARGS_CONSTRUCTOR_ANNOTATIONS = setOf("RequiredArgsConstructor")


        const val AUTOWIRED_FROM_BASE_ANNOTATION = "AutowiredFromBase"
        /**
         * 模板默认版本
         */
        val SINGLE_BUNDLE_TEMPLATE_ARCHETYPE = ArchetypeInfo("com.alipay.sofa.koupleless","koupleless-common-module-archetype","1.1.0")

        /**
         * 生成Bean的注解，暂不支持扫描在方法上的@Bean
         */
        val BEAN_ANNOTATIONS = setOf("Component","Controller","RestController","Service","Repository","Configuration")

        const val MAPPER_SCAN_ANNOTATION = "MapperScan"

        const val BEAN_ANNOTATION = "Bean"

        /**
         * 模块默认文件名称
         */
        const val AUTO_SPLIT_MODULE_XML = "auto_split_module.xml"
        const val MYBATIS_CONFIGURATION = "MybatisConfig.java"
        const val MYBATIS_EXTRA_CONFIGURATION = "MybatisExtraConfig.java"

        /**
         * 补充服务
         */
        const val ENCODING_PATTEN = "ENCODING_PATTEN"
        const val ANALYSE_MODULE_BEAN_DEPENDENCY_WITH_TGT_BASE_BY_NAME_ONLY_PATTEN= "ANALYSE_MODULE_BEAN_DEPENDENCY_WITH_TGT_BASE_BY_NAME_ONLY_PATTEN"
        const val SOFA3_TO_SOFA_BOOT_PATTEN = "SOFA3_TO_SOFA_BOOT_PATTEN"

        /**
         * 整合阶段 config
         */
        const val MODULE_MYBATIS_MAPPER_LOCATION_CONFIG = "MODULE_MYBATIS_MAPPER_LOCATION_CONFIG"
        const val MODULE_MYBATIS_DIR_CONFIG = "MODULE_MYBATIS_DIR_CONFIG"
        const val MODULE_GENERATED_MYBATIS_CLASS = "MODULE_GENERATED_MYBATIS_CLASS"

        /**
         * 树的最大层数
         */
        const val MAX_TREE_NODE_LAYER = 50

        /**
         * 模块视图树的节点占位符
         */
        const val PROJ_NAME_PLACEHOLDER = "{projName}"
        const val MODULE_NAME_PLACEHOLDER = "{moduleName}"
        const val PACKAGE_NAME_PLACEHOLDER = "{packageName}"

        /**
         * 节点名称
         */
        const val BEAN_XML_NODE = "BEAN"
        const val SOFA_SERVICE_NODE = "SOFA_SERVICE"
        const val SOFA_REFERENCE_NODE = "SOFA_REFERENCE"

        /**
         *
         */
        const val METHOD_BEAN = "METHOD_BEAN"


        const val MAPPER_BEAN = "MAPPER_BEAN"

        /**
         * SqlSessionFactoryBean 相关类
         */
        val SQL_SESSION_FACTORY_BEAN = setOf("org.mybatis.spring.SqlSessionFactoryBean","com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean")

        /**
         * PlatformTransactionManager 相关类
         */
        val PLATFORM_TRANSACTION_MANAGER = setOf("org.springframework.jdbc.datasource.DataSourceTransactionManager")

        /**
         * TransactionTemplate 相关类
         */
        val TRANSACTION_TEMPLATE = setOf("org.springframework.transaction.support.TransactionTemplate")

        /**
         * SqlSessionTemplate 相关类
         */
        val SQL_SESSION_TEMPLATE = setOf("org.mybatis.spring.SqlSessionTemplate")

        /**
         * DataSource 相关类
         */
        val DATA_SOURCE = setOf("com.alipay.zdal.client.jdbc.ZdalDataSource","javax.sql.DataSource","org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource","org.springframework.jdbc.datasource.AbstractDataSource")

        /**
         * 模块化属性
         */
        const val MODULAR_MODULE_NAME = "Module-Name"

        /**
         * 拆分的代码模板
         */
        const val SPLIT_SPRING_TEMPLATE_RESOURCE = "template/auto_split_module.xml"
        const val MYBATIS_CONFIG_TEMPLATE_RESOURCE = "template/MybatisConfig.java"
        const val MYBATIS_SQL_SESSION_FACTORY_CONFIG_TEMPLATE_RESOURCE = "template/MybatisSqlSessionFactoryConfig.java"
        const val MYBATIS_EXTRA_CONFIG_TEMPLATE_RESOURCE = "template/MybatisExtraConfig.java"

        /**
         * 数据源复用的默认 mapper_location 路径
         */
        const val DEFAULT_XML_MAPPER_LOCATION = "classpath:mapper/*.xml"

        /**
         * 数据源复用的默认 mybatis-config.xml 路径
         */
        const val DEFAULT_MYBATIS_CONFIG_CLASSPATH_LOCATION = "mybatis/mybatis-config.xml"

        const val DEFAULT_REUSE_MYBATIS_FACTORY_XML_BEAN_METHOD = "mysqlSqlFactory"

        const val DEFAULT_XML_TO_JAVA_FAKE_PACKAGE_NAME = "fake.package.name"

        /**
         * properties 无须合并的黑名单 key
         */
        val PROPERTY_BLACK_KEY = setOf("spring.application.name","domain.name","inner.domain","loggingRoot","log_root","logging.config")
    }

    /**
     * 拆分模式
     */
    enum class SplitModeEnum {
        /**
         * 复制
         */
        COPY,
        /**
         * 移动
         */
        MOVE
    }

    enum class SOFAFrameworkEnum {
        SOFA3,
        SOFA_BOOT,
        UN_SUPPORT
    }

    enum class Labels(val tag: String, val text: String){
        SINGLE_BUNDLE_TEMPLATE("SINGLE_BUNDLE_TEMPLATE","单bundle模版"),
        ADD_MODULE("ADD_MODULE","点击新增模块"),
        MONO_MODE("MONO_MODE","共库模式"),
        INDEPENDENT_MODE("INDEPENDENT_MODE","独立仓库模式"),
        COPY_MODE("COPY_MODE","复制模式"),
        MOVE_MODE("MOVE_MODE","移动模式"),
        AUTO_MODIFY("AUTO_MODIFY","自动修改"),
        NOT_AUTO_MODIFY("NOT_AUTO_MODIFY","不自动修改"),
        KEEP_SOFA3("KEEP_SOFA3","保持原项目SOFA3框架"),
        TO_SOFA_BOOT("TO_SOFA_BOOT","适配为已选基座SOFABoot框架（不保证完全适配）");

        companion object DefaultListCellRenderer : ColoredListCellRenderer<Any>() {
            override fun customizeCellRenderer(list: JList<out Any>, value: Any?, index: Int, selected: Boolean, hasFocus: Boolean) {
                append((value as Labels).text)
            }
        }
    }
}
