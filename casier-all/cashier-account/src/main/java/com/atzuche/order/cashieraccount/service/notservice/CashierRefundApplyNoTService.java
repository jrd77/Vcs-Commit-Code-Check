package com.atzuche.order.cashieraccount.service.notservice;

import com.atzuche.order.cashieraccount.entity.CashierRefundApplyEntity;
import com.atzuche.order.cashieraccount.enums.CashierRefundApplyStatus;
import com.atzuche.order.cashieraccount.exception.CashierRefundApplyException;
import com.atzuche.order.cashieraccount.vo.req.CashierRefundApplyReqVO;
import com.atzuche.order.cashieraccount.vo.req.CashierRefundCallBackReqVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.atzuche.order.cashieraccount.mapper.CashierRefundApplyMapper;



/**
 * 退款申请表
 *
 * @author ZhangBin
 * @date 2019-12-11 11:17:59
 */
@Service
public class CashierRefundApplyNoTService {
    @Autowired
    private CashierRefundApplyMapper cashierRefundApplyMapper;


    /**
     * 记录待退款信息
     * @param cashierRefundApplyReq
     */
    public void insertRefundDeposit(CashierRefundApplyReqVO cashierRefundApplyReq) {
        CashierRefundApplyEntity cashierRefundApplyEntity = new CashierRefundApplyEntity();
        BeanUtils.copyProperties(cashierRefundApplyReq,cashierRefundApplyEntity);
        cashierRefundApplyEntity.setStatus(CashierRefundApplyStatus.RECEIVED_REFUND.getCode());
        int result = cashierRefundApplyMapper.insert(cashierRefundApplyEntity);
        if(result==0){
            throw new CashierRefundApplyException();
        }
    }


    /**
     * 退款回调信息
     */
    public void updateRefundDepositSuccess(CashierRefundCallBackReqVO cashierRefundCallBackReqVO) {
        //1 校验
        //2 回调退款是否成功
        //3 更新退款成功
        //4 成功之后push 或者 短信
    }
}
