package com.enhinck.demo.service.impl;

import com.enhinck.swagger.dubbo.IGatheringService;
import com.enhinck.demo.service.PayTccAction;
import com.enhinck.demo.service.TransformService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-07-06 15:35
 */
@Service
public class TransformServiceImpl implements TransformService {
    @Autowired
    PayTccAction payTccAction;
    @DubboReference
    IGatheringService gatheringTccAction;
    @Override
    @GlobalTransactional
    public boolean transform(Long sourceId, Long targetId, Integer amount) {
        boolean pay = payTccAction.prepare(null, amount, sourceId);
        boolean gather = gatheringTccAction.prepare(null, amount, targetId);
        if (pay && gather) {
            return true;
        }
        return false;
    }
}
