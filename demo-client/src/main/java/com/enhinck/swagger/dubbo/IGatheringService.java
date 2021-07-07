package com.enhinck.swagger.dubbo;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC
public interface IGatheringService {
    /**
     * Prepare boolean.
     *
     * @param actionContext
     * @param amount
     * @param accountId
     * @return
     */
    @TwoPhaseBusinessAction(name = "GatheringTccAction", commitMethod = "commit", rollbackMethod = "rollback")
    boolean prepare(BusinessActionContext actionContext, @BusinessActionContextParameter(paramName = "amount") Integer amount, @BusinessActionContextParameter(paramName = "accountId") Long accountId);

    /**
     * Commit boolean.
     *
     * @param actionContext the action context
     * @return the boolean
     */
    boolean commit(BusinessActionContext actionContext);

    /**
     * Rollback boolean.
     *
     * @param actionContext the action context
     * @return the boolean
     */
    boolean rollback(BusinessActionContext actionContext);
}
