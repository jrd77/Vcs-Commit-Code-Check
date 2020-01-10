package com.atzuche.order.accountrenterwzdepost.service.notservice;

import com.atzuche.order.accountrenterwzdepost.entity.AccountRenterWzDepositEntity;
import com.atzuche.order.accountrenterwzdepost.exception.AccountRenterWZDepositException;
import com.atzuche.order.accountrenterwzdepost.exception.PayOrderRenterWZDepositException;
import com.atzuche.order.accountrenterwzdepost.mapper.AccountRenterWzDepositMapper;
import com.atzuche.order.accountrenterwzdepost.vo.req.CreateOrderRenterWZDepositReqVO;
import com.atzuche.order.accountrenterwzdepost.vo.req.PayedOrderRenterDepositWZDetailReqVO;
import com.atzuche.order.accountrenterwzdepost.vo.req.PayedOrderRenterWZDepositReqVO;
import com.atzuche.order.accountrenterwzdepost.vo.res.AccountRenterWZDepositResVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * 违章押金状态及其总表
 *
 * @author ZhangBin
 * @date 2019-12-11 17:58:17
 */
@Service
public class AccountRenterWzDepositNoTService {
    @Autowired
    private AccountRenterWzDepositMapper accountRenterWzDepositMapper;


    /**
     * 查询违章信息
     * @param orderNo
     * @param memNo
     * @return
     */
    public AccountRenterWZDepositResVO getAccountRenterWZDeposit(String orderNo, String memNo) {
        AccountRenterWZDepositResVO  result = new AccountRenterWZDepositResVO();
        AccountRenterWzDepositEntity accountRenterDepositEntity = accountRenterWzDepositMapper.selectByOrderAndMemNo(orderNo,memNo);
        if(Objects.nonNull(accountRenterDepositEntity)){
            BeanUtils.copyProperties(accountRenterDepositEntity,result);
        }
        return result;
    }

    /**
     * 订单下单成功 记录 应收违章押金
     * @param createOrderRenterWZDepositReq
     */
    public void insertRenterWZDeposit(CreateOrderRenterWZDepositReqVO createOrderRenterWZDepositReq) {
        AccountRenterWzDepositEntity accountRenterDepositEntity = new AccountRenterWzDepositEntity ();
        BeanUtils.copyProperties(createOrderRenterWZDepositReq,accountRenterDepositEntity);
        accountRenterDepositEntity.setFreeDepositType(createOrderRenterWZDepositReq.getFreeDepositType().getCode());
        accountRenterDepositEntity.setYingshouDeposit(createOrderRenterWZDepositReq.getYingfuDepositAmt());
        int result = accountRenterWzDepositMapper.insertSelective(accountRenterDepositEntity);
        if(result==0){
            throw new AccountRenterWZDepositException();
        }
    }
    /**
     * 更新违章押金 实付信息
     * @param payedOrderWZRenterDeposit
     */
    public void updateRenterDeposit(PayedOrderRenterWZDepositReqVO payedOrderWZRenterDeposit) {
        AccountRenterWzDepositEntity accountRenterDepositEntity = accountRenterWzDepositMapper.selectByOrderAndMemNo(payedOrderWZRenterDeposit.getOrderNo(),payedOrderWZRenterDeposit.getMemNo());
        BeanUtils.copyProperties(payedOrderWZRenterDeposit,accountRenterDepositEntity);
        int result = accountRenterWzDepositMapper.updateByPrimaryKeySelective(accountRenterDepositEntity);
        if(result==0){
            throw new PayOrderRenterWZDepositException();
        }
    }
    /**
     * 更新违章押金 剩余金额
     * @param payedOrderRenterWZDepositDetail
     */
    public void updateRenterWZDepositChange(PayedOrderRenterDepositWZDetailReqVO payedOrderRenterWZDepositDetail) {
        AccountRenterWzDepositEntity accountRenterDepositEntity = accountRenterWzDepositMapper.selectByOrderAndMemNo(payedOrderRenterWZDepositDetail.getOrderNo(),payedOrderRenterWZDepositDetail.getMemNo());
        if(Objects.isNull(accountRenterDepositEntity)){
            throw new PayOrderRenterWZDepositException();
        }
        if(payedOrderRenterWZDepositDetail.getAmt() + accountRenterDepositEntity.getShouldReturnDeposit()<0){
            //可用 剩余押金 不足
            throw new PayOrderRenterWZDepositException();
        }
        AccountRenterWzDepositEntity accountRenterDeposit = new AccountRenterWzDepositEntity();
        accountRenterDeposit.setId(accountRenterDepositEntity.getId());
        accountRenterDeposit.setVersion(accountRenterDepositEntity.getVersion());

        if(Objects.nonNull(accountRenterDeposit.getShouldReturnDeposit()) || accountRenterDeposit.getShouldReturnDeposit()>Math.abs(payedOrderRenterWZDepositDetail.getAmt())){
            accountRenterDeposit.setShouldReturnDeposit(accountRenterDeposit.getShouldReturnDeposit() + payedOrderRenterWZDepositDetail.getAmt());
        }
        int result =  accountRenterWzDepositMapper.updateByPrimaryKeySelective(accountRenterDeposit);
        if(result==0){
            throw new PayOrderRenterWZDepositException();
        }
    }
}
