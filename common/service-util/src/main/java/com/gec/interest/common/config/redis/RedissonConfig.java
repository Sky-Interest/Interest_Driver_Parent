package com.gec.interest.common.config.redis;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * redisson配置信息
 */
@Data
@Configuration
@ConfigurationProperties("spring.data.redis")
public class RedissonConfig {

    private String host;

    private String password;

    private String port;

    private int timeout = 3000;
    private static String ADDRESS_PREFIX = "redis://";


    /**
     * 自动装配
     */
    @Bean
    RedissonClient redissonSingle() {
        //创建Redisson的配置对象Config
        Config config = new Config();
        if (!StringUtils.hasText(host)) {
            throw new RuntimeException("host is  empty");
        }
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(ADDRESS_PREFIX + this.host + ":" + port)
                .setTimeout(this.timeout);
        if (StringUtils.hasText(this.password)) {
            serverConfig.setPassword(this.password);
        }
        //创建Redisson的客户端对象
        return Redisson.create(config);
    }
}

