package com.enhinck.redis;

import lombok.Data;

/**
 * <p>redis对象wrapper
 *
 * @author xiaomi（huenbin ）
 * @since 4/27/21 3:16 PM
 */
@Data
public class ObjectWrapper {
    private byte[] object;

    private Class<?> clazz;

    public ObjectWrapper() {
    }

    public ObjectWrapper(byte[] object, Class<?> clazz) {
        this.object = object;
        this.clazz = clazz;
    }

    public byte[] getObject() {
        return object;
    }

    public void setObject(byte[] object) {
        this.object = object;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
}