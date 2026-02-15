package vn.vibeteam.vibe.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                                                                              .allowIfBaseType(Object.class)
                                                                              .build();

        ObjectMapper objectMapper = JsonMapper.builder()
                                              .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                                              .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                              .activateDefaultTyping(
                                                      typeValidator,
                                                      DefaultTyping.NON_FINAL,
                                                      JsonTypeInfo.As.PROPERTY
                                              )
                                              .build();

        GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                                                                  .entryTtl(Duration.ofMinutes(10))
                                                                  .disableCachingNullValues()
                                                                  .serializeKeysWith(
                                                                          RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                                                                  .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaults)
                                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                                                                              .allowIfBaseType(Object.class)
                                                                              .build();

        ObjectMapper objectMapper = JsonMapper.builder()
                                              .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                                              .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                              .activateDefaultTyping(
                                                      typeValidator,
                                                      DefaultTyping.NON_FINAL,
                                                      JsonTypeInfo.As.PROPERTY
                                              )
                                              .build();

        // JSON Serializer
        GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // String Serializer
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//        template.setValueSerializer(stringRedisSerializer);
//        template.setHashValueSerializer(stringRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}