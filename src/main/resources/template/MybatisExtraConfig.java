package packageName.common.dal.mybatis;

import org.springframework.context.annotation.Configuration;
import com.alipay.sofa.koupleless.common.api.SpringBeanFinder;

@Configuration
public class MybatisExtraConfig {
    //tips:不要初始化一个基座的DataSource，当模块被卸载的是，基座数据源会被销毁，transactionManager，transactionTemplate，mysqlSqlFactory被销毁没有问题
}
