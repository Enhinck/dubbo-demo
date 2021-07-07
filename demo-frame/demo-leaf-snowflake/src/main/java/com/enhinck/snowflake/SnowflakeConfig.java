package com.enhinck.snowflake;

import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.snowflake.SnowflakeIDGenImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * <p>雪花id配置
 *
 * @author xiaomi（huenbin）
 * @since 5/8/21 5:34 PM
 */
@Configuration
@Order(0)
public class SnowflakeConfig {
    @Value("${dubbo.registry.address}")
    private String zkserverLists;

    @Bean
    public SnowflakeIdGen snowflakeIdGen() {
        return getInstance(zkserverLists);
    }

    public static final Long TWEPOCH = 1619798400000L;

    public SnowflakeIdGen getInstance(String zkServer) {
        String[] serverport = zkServer.split(",")[0].split(":");
        IDGen idGen = new SnowflakeIDGenImpl(serverport[0], Integer.valueOf(serverport[1]), TWEPOCH);
        SnowflakeIdGen snowflakeIdGen = new SnowflakeIdGen();
        snowflakeIdGen.setIdGen(idGen);
        return snowflakeIdGen;
    }

}