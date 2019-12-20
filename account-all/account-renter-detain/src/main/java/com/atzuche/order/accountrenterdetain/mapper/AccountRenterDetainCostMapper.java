package com.atzuche.order.accountrenterdetain.mapper;

import com.atzuche.order.accountrenterdetain.entity.AccountRenterDetainCostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 暂扣费用总表
 * 
 * @author ZhangBin
 * @date 2019-12-11 17:51:17
 */
@Mapper
public interface AccountRenterDetainCostMapper{

    AccountRenterDetainCostEntity selectByPrimaryKey(Integer id);

    int insert(AccountRenterDetainCostEntity record);
    
    int updateByPrimaryKeySelective(AccountRenterDetainCostEntity record);

    AccountRenterDetainCostEntity getRenterDetainAmt(@Param("orderNo") String orderNo, @Param("memNo")String memNo);
}
