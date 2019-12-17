package com.atzuche.order.accountrenterdeposit.service;

import com.atzuche.order.accountrenterdeposit.entity.AccountRenterDepositEntity;
import com.atzuche.order.accountrenterdeposit.service.notservice.AccountRenterDepositDetailNoTService;
import com.atzuche.order.accountrenterdeposit.service.notservice.AccountRenterDepositNoTService;
import com.atzuche.order.accountrenterdeposit.vo.req.CreateOrderRenterDepositReqVO;
import com.atzuche.order.accountrenterdeposit.vo.req.PayedOrderRenterDepositDetailReqVO;
import com.atzuche.order.accountrenterdeposit.vo.req.PayedOrderRenterDepositReqVO;
import com.atzuche.order.accountrenterdeposit.vo.res.AccountRenterDepositResVO;
import com.autoyol.commons.web.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 租车押金状态及其总表
 *
 * @author ZhangBin
 * @date 2019-12-17 17:09:45
 */
@Service
public class AccountRenterDepositService{
    @Autowired
    private AccountRenterDepositDetailNoTService accountRenterDepositDetailNoTService;
    @Autowired
    private AccountRenterDepositNoTService accountRenterDepositNoTService;
    /**
     * 查询押金状态信息
     */
    public AccountRenterDepositResVO getAccountRenterDepositEntity(String orderNo, String memNo){
        Assert.notNull(orderNo, ErrorCode.PARAMETER_ERROR.getText());
        Assert.notNull(memNo, ErrorCode.PARAMETER_ERROR.getText());
        return accountRenterDepositNoTService.getAccountRenterDepositEntity(orderNo,memNo);
    }
    /**
     * 下单成功记录应付押金
     */
    public void insertRenterDeposit(CreateOrderRenterDepositReqVO createOrderRenterDepositReqVO){
        //1 校验
        Assert.notNull(createOrderRenterDepositReqVO, ErrorCode.PARAMETER_ERROR.getText());
        createOrderRenterDepositReqVO.check();
        accountRenterDepositNoTService.insertRenterDeposit(createOrderRenterDepositReqVO);
    }
    /**
     * 支付成功后记录实付押金信息 和押金资金进出信息
     */
    public void updateRenterDeposit(PayedOrderRenterDepositReqVO payedOrderRenterDeposit){
        //1 参数校验
        Assert.notNull(payedOrderRenterDeposit, ErrorCode.PARAMETER_ERROR.getText());
        payedOrderRenterDeposit.check();
        //2更新押金 实付信息
        accountRenterDepositNoTService.updateRenterDeposit(payedOrderRenterDeposit);
        //添加押金资金进出明细
        accountRenterDepositDetailNoTService.insertRenterDepositDetail(payedOrderRenterDeposit.getPayedOrderRenterDepositDetailReqVO());
    }

    /**
     * 支户头押金资金进出 操作
     */
    public void updateRenterDepositChange(PayedOrderRenterDepositDetailReqVO payedOrderRenterDepositDetail){
        //1 参数校验
        Assert.notNull(payedOrderRenterDepositDetail, ErrorCode.PARAMETER_ERROR.getText());
        payedOrderRenterDepositDetail.check();
        //2更新车辆押金  剩余押金 金额
        accountRenterDepositNoTService.updateRenterDepositChange(payedOrderRenterDepositDetail);
        //添加押金资金进出明细
        accountRenterDepositDetailNoTService.insertRenterDepositDetail(payedOrderRenterDepositDetail);
    }



}
