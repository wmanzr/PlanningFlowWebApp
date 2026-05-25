package RUT.PlanningFlow.config.cache;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@EnableCaching
public class ApplicationRedisCacheConfiguration {

    public static final String CACHE_SKILLS_CATALOG = "skillsCatalog";
    public static final String CACHE_SKILL_CATEGORIES = "skillCategories";
    public static final String CACHE_SKILL_DETAILS = "skillDetails";
    public static final String CACHE_INTERNAL_RESOURCES = "internalResources";
    public static final String CACHE_RESOURCE_DETAILS = "resourceDetails";
    public static final String CACHE_LANDING_STATS = "landingStats";
    public static final String CACHE_USER_CARD = "userCard";
    public static final String CACHE_USERS_DIRECTORY = "usersDirectory";

    private static final String[] ALL_CACHE_NAMES = {
            CACHE_SKILLS_CATALOG,
            CACHE_SKILL_CATEGORIES,
            CACHE_SKILL_DETAILS,
            CACHE_INTERNAL_RESOURCES,
            CACHE_RESOURCE_DETAILS,
            CACHE_LANDING_STATS,
            CACHE_USER_CARD,
            CACHE_USERS_DIRECTORY
    };

    @Bean
    RedisSerializer<Object> planningFlowRedisCacheValueSerializer() {
        final PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("RUT.PlanningFlow.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.lang.")
                .build();
        return GenericJacksonJsonRedisSerializer.create(b -> b
                .enableSpringCacheNullValueSupport()
                .enableDefaultTyping(typeValidator));
    }

    @Bean
    @Primary
    CacheManager cacheManager(
            final ObjectProvider<RedisConnectionFactory> redisConnectionFactory,
            final RedisSerializer<Object> planningFlowRedisCacheValueSerializer
    ) {
        final RedisConnectionFactory factory = redisConnectionFactory.getIfAvailable();
        if (factory == null) {
            return new ConcurrentMapCacheManager(ALL_CACHE_NAMES);
        }

        final RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer(StandardCharsets.UTF_8)))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(planningFlowRedisCacheValueSerializer))
                .disableCachingNullValues();

        final RedisSerializer<Object> skillCategoriesValues = GenericJacksonJsonRedisSerializer.create(
                b -> b.enableSpringCacheNullValueSupport());
        final RedisCacheConfiguration skillCategories = defaults.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(skillCategoriesValues));

        final RedisCacheConfiguration landing = defaults.entryTtl(Duration.ofMinutes(20));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaults)
                .withCacheConfiguration(CACHE_SKILL_CATEGORIES, skillCategories)
                .withCacheConfiguration(CACHE_LANDING_STATS, landing)
                .transactionAware()
                .build();
    }
}