

package com.enhinck.redis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * <p>redis对象存储配置
 *
 * @author xiaomi（huenbin ）
 * @since 4/27/21 3:16 PM
 */
@Slf4j
@Configuration
public class ObjectRedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password:}")
    private String password;

    @Bean
    public RedissonClient getRedisson() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        if (StringUtils.isNotBlank(password)) {
            config.useSingleServer().setPassword(password);
        }

        return Redisson.create(config);
    }

    /**
     * 对象专用缓存
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean("objectRedisTemplate")
    @Primary
    public RedisTemplate iocRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = createCommon(redisConnectionFactory);
        redisTemplate.setValueSerializer(new FastJsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new FastJsonRedisSerializer());
        return redisTemplate;
    }

    /**
     * 统计数据专用缓存
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean("countRedisTemplate")
    public RedisTemplate countRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = createCommon(redisConnectionFactory);
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    private RedisTemplate createCommon(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setEnableDefaultSerializer(false);
        redisTemplate.setEnableTransactionSupport(false);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}