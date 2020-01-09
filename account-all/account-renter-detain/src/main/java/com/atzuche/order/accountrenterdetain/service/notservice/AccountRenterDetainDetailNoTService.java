package com.atzuche.order.accountrenterdetain.service.notservice;

import com.atzuche.order.accountrenterdetain.entity.AccountRenterDetainDetailEntity;
import com.atzuche.order.accountrenterdetain.exception.AccountRenterDetainDetailException;
import com.atzuche.order.accountrenterdetain.vo.req.DetainRenterDepositReqVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atzuche.order.accountrenterdetain.mapper.AccountRenterDetainDetailMapper;


/**
 * 暂扣费用进出明细表
 *
 * @author ZhangBin
 * @date 2019-12-11 17:51:17
 */
@Service
public class AccountRenterDetainDetailNoTService {
    @Autowired
    private AccountRenterDetainDetailMapper accountRenterDetainDetailMapper;


    /**
     * 记录暂扣明细
     * @param detainRenterDeposit
     */
    public void insertCostDetail(DetainRenterDepositReqVO detainRenterDeposit) {
        AccountRenterDetainDetailEntity entity = new AccountRenterDetainDetailEntity();
        BeanUtils.copyProperties(detainRenterDeposit,entity);
        entity.setSourceCode(Integer.parseInt(detainRenterDeposit.getRenterCashCodeEnum().getCashNo()));
        entity.setSourceDetail(detainRenterDeposit.getRenterCashCodeEnum().getTxt());
        int result = accountRenterDetainDetailMapper.insertSelective(entity);
        if(result==0){
            throw new AccountRenterDetainDetailException();
        }
    }
}
