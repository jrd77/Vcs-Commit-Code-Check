package com.atzuche.order.accountrenterwzdepost.service;

import com.atzuche.order.accountrenterwzdepost.service.notservice.AccountRenterWzDepositCostDetailNoTService;
import com.atzuche.order.accountrenterwzdepost.service.notservice.AccountRenterWzDepositCostNoTService;
import com.atzuche.order.accountrenterwzdepost.service.notservice.AccountRenterWzDepositCostSettleDetailNoTService;
import com.atzuche.order.accountrenterwzdepost.vo.req.RenterWZDepositCostReqVO;
import com.autoyol.commons.web.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atzuche.order.accountrenterwzdepost.mapper.AccountRenterWzDepositCostMapper;
import org.springframework.util.Assert;


/**
 * 违章费用总表及其结算总表
 *
 * @author ZhangBin
 * @date 2019-12-11 17:58:17
 */
@Service
public class AccountRenterWzDepositCostService{

    @Autowired
    private AccountRenterWzDepositCostNoTService accountRenterWzDepositCostNoTService;
    @Autowired
    private AccountRenterWzDepositCostDetailNoTService accountRenterWzDepositCostDetailNoTService;

    /**
     * 查询实扣违章费用总和
     */
    public int getWZDepositCostAmt(String orderNo ,String memNo){
      return accountRenterWzDepositCostNoTService.getWZDepositCostAmt(orderNo,memNo);
    }

    /**
     * 违章费用资金进出
     */
    public void changeWZDepositCost(RenterWZDepositCostReqVO renterWZDepositCost){
       //1校验
        Assert.notNull(renterWZDepositCost, ErrorCode.PARAMETER_ERROR.getText());
        renterWZDepositCost.check();
        //2 更新违章押金总额
        accountRenterWzDepositCostNoTService.changeWZDepositCost(renterWZDepositCost);
        //3 记录违章费用账户费用流水
        accountRenterWzDepositCostDetailNoTService.insertCostDetail(renterWZDepositCost);
    }
}
