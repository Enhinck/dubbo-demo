package com.enhinck.snowflake;

import com.sankuai.inf.leaf.IDGen;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>雪花id
 *
 * @author xiaomi（huenbin）
 * @since 5/8/21 5:34 PM
 */
@Data
@Slf4j
public class SnowflakeIdGen {
    private IDGen idGen;
    private static final String KEY = "a";

    public Long nextId() {
        Result r = idGen.get(KEY);
        if (r.getStatus() == Status.SUCCESS) {
            return r.getId();
        }
        log.error("服务器时间发生回拨");
        throw new RuntimeException("服务器时间发生回拨");
    }

}