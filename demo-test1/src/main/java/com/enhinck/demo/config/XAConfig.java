package com.enhinck.demo.config;

import io.seata.rm.datasource.xa.DataSourceProxyXA;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * <p>XA 模式  配合 @GlobalTransactional  与AT模式区别
 *
 * @author xiaomi（huenbin ）
 * @since 2021-07-08 16:57
 */
//@Configuration
public class XAConfig {

    @Bean("dataSource")
    public DataSource dataSource(DataSource druidDataSource) {
        // DataSourceProxy for AT mode
        // return new DataSourceProxy(druidDataSource);

        // DataSourceProxyXA for XA mode
        return new DataSourceProxyXA(druidDataSource);
    }

}
