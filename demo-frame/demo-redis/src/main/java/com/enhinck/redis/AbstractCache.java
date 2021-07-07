

package com.enhinck.redis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>redis对象存储
 *
 * @param <K>
 * @param <V>
 * @author xiaomi（huenbin ）
 * @since 4/27/21 3:16 PM
 */
public abstract class AbstractCache<K, V> implements ICache<K, V> {
    private long unsetInt = 0;

    @Resource
    private RedisConnectionFactory jedisConnectionFactory;

    /**
     * 缓存key前缀和真实key之间的分隔符
     */
    private String keySeparator = ":";

    private static final String KEY_TEMPLATE = "%s:%s";

    private final Map<String, RedisAtomicLong> redisCounterMap = new ConcurrentHashMap<>();

    protected RedisAtomicLong getRedisCounter(K key) {
        String realKey = this.getKeyPrefix() + "_COUNTER" + keySeparator + key;
        return getRedisAtomicLong(realKey);
    }

    //重载方法,根据不同key值存储
    protected RedisAtomicLong getRedisCounter(K key, String keyFlag) {
        String realKey = this.getKeyPrefix() + keyFlag + keySeparator + key;
        return getRedisAtomicLong(realKey);
    }

    private RedisAtomicLong getRedisAtomicLong(String realKey) {
        RedisAtomicLong redisCounter = redisCounterMap.get(realKey);
        if (redisCounter == null) {
            synchronized (redisCounterMap) {
                redisCounter = redisCounterMap.get(realKey);
                if (redisCounter == null) {
                    redisCounter = new RedisAtomicLong(realKey, jedisConnectionFactory);
                    redisCounterMap.put(realKey, redisCounter);
                }
            }
        }
        return redisCounter;
    }

    public static final int CAPACITY = 32;

    private Transformer<K, String> originalKeyToRealKeyTransformer =
            originalKey -> new StringBuilder(CAPACITY).append(getKeyPrefix()).append(keySeparator)
                    .append(originalKey).toString();

    private Transformer<String, K> realKeyToOriginalKeyTransformer = realKey -> restoreToOrigKey(realKey);


    /**
     * 只查redis
     *
     * @param key
     * @return
     */
    @Override
    public V getIfPresent(K key) {
        if (key == null) {
            return null;
        }

        String realKey = getRealKey(key);

        V value = getRedisTemplate().opsForValue().get(realKey);

        return value;
    }

    @Override
    public boolean isExistent(K key) {
        if (key == null) {
            return false;
        }

        String realKey = getRealKey(key);
        boolean result = getRedisTemplate().hasKey(realKey);
        if (!result) {
            V value = doLoadFromDatabase(realKey);
            if (value != null) {
                setToRedis(realKey, value);
                result = true;
            }
        }

        return result;
    }

    @Override
    public boolean isNonExistent(K key) {
        return !isExistent(key);
    }

    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }

        String realKey = getRealKey(key);
        V value = getRedisTemplate().opsForValue().get(realKey);
        if (value == null) {
            value = doLoadFromDatabase(realKey);
            if (value != null) {
                setToRedis(realKey, value);
            }
        }

        return value;
    }

    @Override
    public void delete(K key) {
        if (key == null) {
            return;
        }
        getRedisTemplate().delete(getRealKey(key));
    }

    @Override
    public void deleteAll(Collection<K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        List<String> realKeys = (List<String>) CollectionUtils.collect(keys,
                originalKeyToRealKeyTransformer);

        getRedisTemplate().delete(realKeys);
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        String realKey = getRealKey(key);
        setToRedis(realKey, value);
    }

    @Override
    public List<V> getAllForList(Collection<K> keys) {
        Map<K, V> returnMap = getAll(keys);
        if (MapUtils.isNotEmpty(returnMap)) {
            List<V> returnList = new ArrayList<>(returnMap.size());
            for (K key : keys) {
                returnList.add(returnMap.get(key));
            }

            return returnList;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<K, V> getAll(Collection<K> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            List<String> realKeys = (List<String>) CollectionUtils.collect(keys,
                    originalKeyToRealKeyTransformer);

            // 从redis加载缓存
            Map<K, V> retMap = getFromRedis(realKeys, keys.size());

            // 从数据库加载并存到redis
            if (CollectionUtils.isNotEmpty(realKeys)) {
                getFromDatabase(realKeys, retMap);
            }

            return retMap;
        }


        return new HashMap<>();
    }

    private void getFromDatabase(List<String> realKeys, Map<K, V> retMap) {
        List<K> originalKeys = (ArrayList<K>) CollectionUtils.collect(realKeys,
                realKeyToOriginalKeyTransformer);

        Map<String, V> tempMap = doBatchLoadFromDatabase(originalKeys);
        if (MapUtils.isNotEmpty(tempMap)) {
            for (Entry<String, V> entry : tempMap.entrySet()) {
                K key = restoreToOrigKey(entry.getKey());
                retMap.put(key, entry.getValue());
            }

            setToRedis(tempMap);
        }
    }

    private Map<K, V> getFromRedis(List<String> realKeys, int size) {
        Map<K, V> retMap = new LinkedHashMap<>(size);
        List<V> values = getRedisTemplate().opsForValue().multiGet(realKeys);
        if (CollectionUtils.isNotEmpty(values)) {
            for (int index = (realKeys.size() - 1); index >= 0; index--) {
                String realKey = realKeys.get(index);
                V value = values.get(index);
                if (value != null) {
                    realKeys.remove(index);
                    retMap.put(restoreToOrigKey(realKey), value);
                }
            }
        }
        return retMap;
    }

    /**
     * 原子性自增，使用redis实现，可以用来做一些全局的数量限制，每次加1
     *
     * @param key
     * @return
     */
    @Override
    public Long atomicIncrement(K key) {
        return getRedisTemplate().opsForValue().increment(getRealKey(key), 1L);
    }

    /**
     * 原子性自增，使用redis实现，可以用来做一些全局的数量限制，每次增加指定的值。
     *
     * @param key
     * @param delta
     * @return
     */
    @Override
    public Long atomicIncrement(K key, long delta) {
        return getRedisTemplate().opsForValue().increment(getRealKey(key), delta);
    }


    @Autowired
    @Qualifier("objectRedisTemplate")
    RedisTemplate<String, V> objectRedisTemplate;

    /**
     * 获取操作redis缓存的对象，子类实现，如果不使用redis缓存，返回null即可。
     *
     * @return
     */
    protected RedisTemplate<String, V> getRedisTemplate() {
        return objectRedisTemplate;
    }

    /**
     * 获取缓存key前缀，子类实现。
     *
     * @return
     */
    protected abstract String getKeyPrefix();


    /**
     * 将string类型的key转回原始类型的key，子类实现
     *
     * @param strKey
     * @return
     */
    protected abstract K restoreToOriginalKey(String strKey);

    /**
     * 可以通过重写这个方法来设置redis缓存的失效时间，单位是秒
     *
     * @return
     */
    protected long redisExpireDuration() {
        return unsetInt;
    }

    /**
     * 从server读取单个对象，子类实现，沒有就返回null
     *
     * @param key
     * @return
     */
    protected abstract V loadFromDatabase(K key);

    /**
     * 从server读取多个对象，子类实现，沒有就返回null
     *
     * @param keys
     * @return
     */
    protected abstract Map<K, V> batchLoadFromDatabase(List<K> keys);

    private K restoreToOrigKey(String realKey) {
        return restoreToOriginalKey(StringUtils.substringAfterLast(realKey, keySeparator));
    }

    protected V doLoadFromDatabase(String key) {
        return loadFromDatabase(restoreToOrigKey(key));
    }

    protected Map<String, V> doBatchLoadFromDatabase(List<K> keys) {
        Map<K, V> tempMap = batchLoadFromDatabase(keys);
        Map<String, V> retMap = null;
        if (MapUtils.isNotEmpty(tempMap)) {
            retMap = new HashMap<>(tempMap.size());
            for (Entry<K, V> entry : tempMap.entrySet()) {
                if (entry.getValue() != null) {
                    retMap.put(getRealKey(entry.getKey()), entry.getValue());
                }
            }
        }

        return retMap;
    }

    protected String getRealKey(K key) {
        return String.format(KEY_TEMPLATE, getKeyPrefix(), key);
    }

    private void setToRedis(String key, V value) {
        if (redisExpireDuration() > unsetInt) {
            getRedisTemplate().opsForValue().set(key, value, redisExpireDuration(),
                    TimeUnit.SECONDS);
        } else {
            getRedisTemplate().opsForValue().set(key, value);
        }
    }

    private void setToRedis(Map<String, V> entryMap) {
        if (redisExpireDuration() > unsetInt) {
            for (Entry<String, V> entry : entryMap.entrySet()) {
                getRedisTemplate().opsForValue().set(entry.getKey(), entry.getValue(),
                        redisExpireDuration(), TimeUnit.SECONDS);
            }
        } else {
            getRedisTemplate().opsForValue().multiSet(entryMap);
        }
    }

    public void deleteAll() {
        String realKey = getRealKey((K) "*");
        Set<String> keys = getRedisTemplate().keys(realKey);
        getRedisTemplate().delete(keys);
    }

    public void update(K key, V value) {
        String realKey = getRealKey(key);
        getRedisTemplate().opsForValue().set(realKey, value);
    }


    public List<String> keys() {
        List<String> list = new ArrayList<>();
        String realKey = getRealKey((K) "*");
        Set<String> keys = getRedisTemplate().keys(realKey);
        keys.forEach(key -> {
            list.add(key.split(keySeparator)[1]);
        });
        return list;
    }
}