package kcs.funding.fundingboost.domain.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP)
public class RefreshTokenConfig {
    @Value("${spring.data.redis.refresh-token.host}")
    private String host;

    @Value("${spring.data.redis.refresh-token.port}")
    private int port;

    @Bean
    public RedisConnectionFactory refreshTokenRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    @Qualifier("redisRefreshTokenTemplate")
    public RedisTemplate<String, Object> refreshTokenTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(refreshTokenRedisConnectionFactory());
        return redisTemplate;
    }
}
