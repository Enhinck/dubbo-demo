package com.enhinck.redis;


import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * <p>缓存接口
 *
 * @param <K>
 * @param <V>
 * @author xiaomi（huenbin ）
 * @since 4/27/21 3:16 PM
 */
public interface ICache<K, V> {
    /**
     * 从缓存中获取一个对象，如果对象不存在则只返回null，不从server重新加载数据。
     *
     * @param key
     * @return
     */
    V getIfPresent(K key);

    /**
     * 判断一个key值是否存在
     */
    boolean isExistent(K key);

    /**
     * 判断一个key值是否不存在
     */
    boolean isNonExistent(K key);

    /**
     * 从缓存中获取一个value，如果不存在会返回null
     *
     * @param key
     * @return
     */
    V get(K key);

    void delete(K key);

    void deleteAll(Collection<K> keys);

    void put(K key, V value);

    Map<K, V> getAll(Collection<K> keys);

    List<V> getAllForList(Collection<K> keys);

    /**
     * 原子性自增，使用redis实现，可以用来做一些全局的数量限制，每次加1
     * 注意：自增操作和get操作不能一起使用！
     *
     * @param key
     * @return
     */
    Long atomicIncrement(K key);

    /**
     * 原子性自增，使用redis实现，可以用来做一些全局的数量限制，每次增加指定的值。
     * 注意：自增操作和get操作不能一起使用！
     *
     * @param key
     * @param delta
     * @return
     */
    Long atomicIncrement(K key, long delta);
}