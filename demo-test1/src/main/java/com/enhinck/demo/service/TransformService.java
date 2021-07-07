package com.enhinck.demo.service;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-07-06 15:34
 */
public interface TransformService {
    boolean transform(Long sourceId, Long targetId, Integer amount);
}
