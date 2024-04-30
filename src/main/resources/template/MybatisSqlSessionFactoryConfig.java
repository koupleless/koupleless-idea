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

public class MybatisConfig {
    //tips:不要初始化一个基座的DataSource，当模块被卸载的是，基座数据源会被销毁，transactionManager，transactionTemplate，mysqlSqlFactory被销毁没有问题

    @Bean(name = "mysqlSqlFactory")
    public SqlSessionFactoryBean mysqlSqlFactory() throws IOException {
        //数据源不能申明成模块spring上下文中的bean，因为模块卸载时会触发close方法

        SqlSessionFactoryBean mysqlSqlFactory = new SqlSessionFactoryBean();
        mysqlSqlFactory.setDataSource(dataSource);
        mysqlSqlFactory.setMapperLocations(resolveMapperLocations());
        return mysqlSqlFactory;
    }

    public Resource[] resolveMapperLocations() throws IOException {
        PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        ArrayList<Resource> resources = new ArrayList<>();

        for (String mapperLocation : mapperLocations) {
            Resource[] mappers = resourceResolver.getResources(mapperLocation);
            resources.addAll(Arrays.asList(mappers));
        }

        return resources.toArray(new Resource[0]);
    }
}
