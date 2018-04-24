package com.smartoa.service.config;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartoa.common.constant.Constants;

/**
 * REDIS配置
 */
@Configuration
@EnableCaching
@SuppressWarnings({"rawtypes", "unchecked"})
public class RedisManager {
	@Value("${spring.redis.host}")
	private String host;
	@Value("${spring.redis.port}")
	private int port;
	// 设置为0,永不超时
	private int timeout = 0;
	protected static RedisManager rm = new RedisManager();

	private RedisTemplate redisTemplate;

	@Bean
	public KeyGenerator wiselyKeyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for (Object obj : params) {
					sb.append(obj.toString());
				}
				return sb.toString();
			}
		};

	}

	@Bean(name = "redisCacheManager")
	public RedisCacheManager redisCacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);

		// Number of seconds before expiration. Defaults to unlimited (0)
		// cacheManager.setDefaultExpiration(10); //设置key-value超时时间
		return cacheManager;
	}

	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setHostName(host);
		factory.setPort(port);
		factory.setTimeout(timeout); // 设置连接超时时间

		return factory;

	}

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
		StringRedisTemplate template = new StringRedisTemplate(factory);
		setSerializer(template); // 设置序列化工具，这样ReportBean不需要实现Serializable接口
		template.afterPropertiesSet();

		this.redisTemplate = template;
		return template;
	}

	private void setSerializer(StringRedisTemplate template) {
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		template.setValueSerializer(jackson2JsonRedisSerializer);
	}

	/**
	 *
	 * @param key
	 * @param o
	 */
	public void setValues(String key, Object o) {
		redisTemplate.opsForValue().set(key, o);

	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 *
	 * @param key
	 * @param o
	 * @param timeout
	 */
	public void setValues(String key, Object o, Long timeout) {

		redisTemplate.opsForValue().set(key, o);
		redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);

	}

	/**
	 *
	 * @param key
	 */
	public void delValues(String key) {
		redisTemplate.delete(key);
	}

	/**
	 * 保存Hash数据
	 *
	 * @param key
	 *            key
	 * @param map
	 *            Hash数据
	 * @param timeout
	 *            有效时间（单位耗秒）
	 */
	public void putHash(String key, Map<String, Object> map, long timeout) {
		redisTemplate.opsForHash().putAll(key, map);
		redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * 保存Hash数据
	 *
	 * @param key
	 *            key
	 * @param map
	 *            Hash数据
	 */
	public void putHash(String key, Map<String, Object> map) {
		redisTemplate.opsForHash().putAll(key, map);
	}
	
    /**
     * 保存Hash数据
     * 
     * @param key
     * @param hashKey
     * @param value
     */
    public void putHash(String key, String hashKey, Object value){
    	redisTemplate.opsForHash().put(key, hashKey, value);
    }

	/**
	 * 获取Hash数据
	 *
	 * @param key
	 *            key
	 * @return
	 */
	public Map<String, Object> getHash(String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	 /**
     * 根据key、hashKey获取Map的值
     * 
     * @param key
     * @param hashKey
     * @return
     */
    public Object getHash(String key, String hashKey){
    	return redisTemplate.opsForHash().get(key, hashKey);
    }
    
	public void putSet(String key, Object... value) {
		redisTemplate.opsForSet().add(key, value);
	}

	public void putSet(String key, Object value, long timeout) {
		redisTemplate.opsForSet().add(key, value);
		redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
	}

	public Object getSet(String key) {
		return redisTemplate.opsForSet().pop(key);
	}

	public boolean isMember(String key, Object vk) {
		return redisTemplate.opsForSet().isMember(key, vk);
	}

	/**
	 * 从缓存中拿到用户
	 *
	 * @param uid
	 * @return
	 */
	public Map<String, Object> getUser(String uid) {
		return (Map<String, Object>) redisTemplate.opsForValue().get(Constants.REDIS_PREFIX.USER.desc() + uid);

	}

	/**
	 * 修改缓存中的用户
	 *
	 * @param uid
	 * @param gold
	 * @return
	 */
	public void updateUser(String uid, int gold) throws Exception {
		Map<String, Object> map = (Map<String, Object>) redisTemplate.opsForValue()
				.get(Constants.REDIS_PREFIX.USER.desc() + uid);
		redisTemplate.expire(Constants.REDIS_PREFIX.USER.desc() + uid, timeout, TimeUnit.MILLISECONDS);
		map.put("gold", gold);
		this.setValues(Constants.REDIS_PREFIX.USER.desc() + uid, map, Constants.APP_USER_LOGIN_TIME);

	}

	/**
	 * 修改缓存中的用户
	 *
	 * @param uid
	 * @param o
	 * @return
	 */
	public void updateUser(String uid, Map<String, Object> o) throws Exception {
		redisTemplate.expire(Constants.REDIS_PREFIX.USER.desc() + uid, timeout, TimeUnit.MILLISECONDS);
		this.setValues(Constants.REDIS_PREFIX.USER.desc() + uid, o, Constants.APP_USER_LOGIN_TIME);
	}
}
