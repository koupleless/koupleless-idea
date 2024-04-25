package packageName.common.dal.mybatis;



import org.springframework.context.annotation.Bean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import com.alipay.sofa.koupleless.common.api.SpringBeanFinder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 模块复用基座数据源：https://koupleless.io/docs/tutorials/module-development/reuse-base-datasource/
 */

@Configuration
@MapperScan(basePackages = "{packageName}.common.dal.dao", sqlSessionFactoryRef = "mysqlSqlFactory")
public class MybatisConfig {
    //tips:不要初始化一个基座的DataSource，当模块被卸载的是，基座数据源会被销毁，transactionManager，transactionTemplate，mysqlSqlFactory被销毁没有问题
}
