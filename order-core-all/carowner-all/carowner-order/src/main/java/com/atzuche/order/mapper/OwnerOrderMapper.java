package com.atzuche.order.mapper;

import com.atzuche.order.entity.OwnerOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 车主订单子表
 * 
 * @author ZhangBin
 * @date 2019-12-11 18:07:01
 */
@Mapper
public interface OwnerOrderMapper{

    OwnerOrderEntity selectByPrimaryKey(Integer id);

    List<OwnerOrderEntity> selectALL();

    int insert(OwnerOrderEntity record);
    
    int insertSelective(OwnerOrderEntity record);

    int updateByPrimaryKey(OwnerOrderEntity record);
    
    int updateByPrimaryKeySelective(OwnerOrderEntity record);

}
