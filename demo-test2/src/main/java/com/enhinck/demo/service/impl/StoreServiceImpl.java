package com.enhinck.demo.service.impl;

import com.enhinck.demo.service.StoreService;
import com.enhinck.demo.mapper.StoreDO;
import com.enhinck.demo.mapper.StoreMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-07-05 15:51
 */
@Service
public class StoreServiceImpl implements StoreService {

    @Resource
    StoreMapper storeMapper;

    @Override
    public void saveStore(Long orderId) {
        StoreDO storeDO = new StoreDO();
        storeDO.setOrderId(orderId);
        storeMapper.insert(storeDO);
        Integer.valueOf("0XX");
    }
}
