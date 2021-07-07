package com.enhinck.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;


/**
 * <p>redis对象序列化方法
 *
 * @param <T> 类型
 * @author xiaomi（huenbin ）
 * @since 4/27/21 3:16 PM
 */
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return null;
        }

        return JSON.toJSONBytes(new ObjectWrapper(JSON.toJSONBytes(t), t.getClass()));
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        Object data = JSON.parse(bytes);
        if (data instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) data;
            try {
                return JSON.parseObject(jsonObject.getBytes("object"),
                        Class.forName(jsonObject.getString("clazz")));
            } catch (ClassNotFoundException e) {
                return null;
            }
        } else if (data instanceof Integer) {
            return JSON.parseObject(bytes, Long.class);
        } else {
            return JSON.parseObject(bytes, data.getClass());
        }
    }
}