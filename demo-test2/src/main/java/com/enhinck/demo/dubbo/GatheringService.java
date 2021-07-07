package com.enhinck.demo.dubbo;

import com.enhinck.client.dubbo.IGatheringService;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-07-06 16:02
 */
@Slf4j
@Service
@DubboService
public class GatheringService implements IGatheringService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean prepare(BusinessActionContext actionContext, Integer amount, Long accountId) {
        if (accountId > 1000) {
            throw new RuntimeException("错误的账户");
        }
        log.info(" 账号accountId:{} 预入账资金:{}元", accountId, amount);
        return true;
    }

    @Override
    public boolean commit(BusinessActionContext actionContext) {
        Integer amount = (Integer) actionContext.getActionContext("amount");
        Long accountId = Long.valueOf(actionContext.getActionContext("accountId").toString());
        log.info(" 账号accountId:{} 正式入账资金:{}元", accountId, amount);
        return true;
    }

    @Override
    public boolean rollback(BusinessActionContext actionContext) {
        Integer amount = (Integer) actionContext.getActionContext("amount");
        Long accountId = Long.valueOf(actionContext.getActionContext("accountId").toString());
        log.info(" 账号accountId:{} 取消预入账资金:{}元", accountId, amount);
        return true;
    }
}
