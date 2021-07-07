package com.enhinck.demo.service.impl;

import com.enhinck.swagger.dubbo.IStoreService;
import com.enhinck.demo.mapper.OrderDO;
import com.enhinck.demo.mapper.OrderMapper;
import com.enhinck.demo.service.OrderService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-07-05 15:24
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    OrderMapper orderMapper;
    @DubboReference
    IStoreService iStoreService;

    @GlobalTransactional
    @Override
    public void save(String param) {
        log.info("xid:{}", RootContext.getXID());
        OrderDO orderDO = new OrderDO();
        orderDO.setName(param);
        orderMapper.insert(orderDO);
        iStoreService.saveStore(orderDO.getId());
    }
}
