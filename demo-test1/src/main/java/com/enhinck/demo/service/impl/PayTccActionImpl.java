package com.enhinck.demo.service.impl;

import com.enhinck.demo.service.PayTccAction;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>write your description here
 *
 * @author xiaomi（huenbin）
 * @since 2021-07-06 15:28
 */
@Slf4j
@Service
public class PayTccActionImpl implements PayTccAction {
    @Override
    public boolean prepare(BusinessActionContext actionContext, Integer amount, Long accountId) {
        if (amount > 500) {
            throw new RuntimeException("账户：" + accountId + "余额剩余不足" + amount + "元");
        }
        log.info(" 冻结账号accountId:{} 冻结资金:{}元", accountId, amount);
        return true;
    }

    @Override
    public boolean commit(BusinessActionContext actionContext) {
        Integer amount = (Integer) actionContext.getActionContext("amount");
        Long accountId = Long.valueOf(actionContext.getActionContext("accountId").toString());
        log.info(" 账号accountId:{} 转出资金:{}元", accountId, amount);
        return true;
    }

    @Override
    public boolean rollback(BusinessActionContext actionContext) {
        // 取消冻结
        Integer amount = (Integer) actionContext.getActionContext("amount");
        Long accountId = Long.valueOf(actionContext.getActionContext("accountId").toString());
        log.info(" 账号accountId:{} 取消冻结资金:{}元", accountId, amount);
        return true;
    }
}
