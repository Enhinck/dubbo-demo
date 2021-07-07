package com.enhinck.demo.dubbo;

import com.enhinck.client.dubbo.IStoreService;
import com.enhinck.demo.service.StoreService;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-07-05 15:46
 */
@Slf4j
@DubboService
@Service
public class StoreServiceProvider implements IStoreService {

    @Resource
    StoreService storeService;

    @Override
    public void saveStore(Long orderId) {

        log.info("xid:{}", RootContext.getXID());

        storeService.saveStore(orderId);
    }
}
