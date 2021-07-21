package com.enhinck.demo.mapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-07-05 15:48
 */
@Data
@TableName("test_store")
public class StoreDO extends Model<StoreDO> {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
}
