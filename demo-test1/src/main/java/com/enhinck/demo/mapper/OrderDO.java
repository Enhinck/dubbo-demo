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
 * @since 2021-07-05 15:27
 */
@Data
@TableName("test_order")
public class OrderDO extends Model<OrderDO> {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
}
