package com.atzuche.order.accountownercost.service.notservice;

import com.atzuche.order.accountownercost.exception.AccountOwnerCostSettleException;
import com.atzuche.order.accountownercost.entity.AccountOwnerCostSettleDetailEntity;
import com.atzuche.order.accountownercost.mapper.AccountOwnerCostSettleDetailMapper;
import com.atzuche.order.accountownercost.vo.req.AccountOwnerCostSettleDetailReqVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * 车主费用结算明细表
 *
 * @author ZhangBin
 * @date 2019-12-11 17:41:36
 */
@Service
public class AccountOwnerCostSettleDetailNoTService {
    @Autowired
    private AccountOwnerCostSettleDetailMapper accountOwnerCostSettleDetailMapper;


    public void insertAccountOwnerCostSettleDetail(List<AccountOwnerCostSettleDetailReqVO> accountOwnerCostSettleDetails) {
        if(CollectionUtils.isEmpty(accountOwnerCostSettleDetails)){
            throw new AccountOwnerCostSettleException();
        }
        for(int i=0;i<accountOwnerCostSettleDetails.size();i ++){
            AccountOwnerCostSettleDetailReqVO accountOwnerCostSettleDetail = accountOwnerCostSettleDetails.get(i);
            accountOwnerCostSettleDetail.check();
            AccountOwnerCostSettleDetailEntity accountOwnerCostSettleDetailEntity = new AccountOwnerCostSettleDetailEntity();
            BeanUtils.copyProperties(accountOwnerCostSettleDetail,accountOwnerCostSettleDetailEntity);
            int result = accountOwnerCostSettleDetailMapper.insert(accountOwnerCostSettleDetailEntity);
            if(result==0){
                throw new AccountOwnerCostSettleException();
            }
        }
    }
}
