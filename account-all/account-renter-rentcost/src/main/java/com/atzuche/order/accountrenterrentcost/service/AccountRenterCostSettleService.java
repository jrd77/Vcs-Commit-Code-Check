package com.atzuche.order.accountrenterrentcost.service;

import com.atzuche.order.accountrenterrentcost.service.notservice.AccountRenterCostDetailNoTService;
import com.atzuche.order.accountrenterrentcost.service.notservice.AccountRenterCostSettleNoTService;
import com.atzuche.order.accountrenterrentcost.vo.req.AccountRenterCostDetailReqVO;
import com.autoyol.commons.web.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 租客费用及其结算总表
 *
 * @author ZhangBin
 * @date 2019-12-13 16:49:57
 */
@Service
public class AccountRenterCostSettleService{
    @Autowired
    private AccountRenterCostSettleNoTService accountRenterCostSettleNoTService;
    @Autowired
    private AccountRenterCostDetailNoTService accountRenterCostDetailNoTService;

    /**
     * 查询订单 已付租车费用
     */
    public int getCostPaidRent(String orderNo,String memNo) {
        Assert.notNull(orderNo, ErrorCode.PARAMETER_ERROR.getText());
        Assert.notNull(memNo, ErrorCode.PARAMETER_ERROR.getText());
        return accountRenterCostSettleNoTService.getCostPaidRent(orderNo,memNo);
    }

    /**
     * 收银台支付成功  实收租车费用落库
     */
    public void insertRenterCostDetail(AccountRenterCostDetailReqVO accountRenterCostDetailReqVO){
        //1 参数校验
        Assert.notNull(accountRenterCostDetailReqVO, ErrorCode.PARAMETER_ERROR.getText());
        accountRenterCostDetailReqVO.check();
        //2租车费用明细落库
        accountRenterCostDetailNoTService.insertAccountRenterCostDetail(accountRenterCostDetailReqVO);
    }



}
