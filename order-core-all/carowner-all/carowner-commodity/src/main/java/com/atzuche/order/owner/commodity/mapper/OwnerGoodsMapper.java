package com.atzuche.order.owner.commodity.mapper;
import com.atzuche.order.commons.entity.dto.LianHeMaiTongOrderDTO;
import com.atzuche.order.commons.vo.LianHeMaiTongOrderVO;
import com.atzuche.order.owner.commodity.entity.OwnerGoodsEntity;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 车主端商品概览表
 * 
 * @author ZhangBin
 * @date 2019-12-17 20:30:11
 */
@Mapper
public interface OwnerGoodsMapper{

    OwnerGoodsEntity selectByPrimaryKey(Integer id);

    int insert(OwnerGoodsEntity record);
    
    int insertSelective(OwnerGoodsEntity record);

    int updateByPrimaryKey(OwnerGoodsEntity record);
    
    int updateByPrimaryKeySelective(OwnerGoodsEntity record);

    OwnerGoodsEntity selectByOwnerOrderNo(@Param("ownerOrderNo")String ownerOrderNo);
    
    OwnerGoodsEntity getLastOwnerGoodsByOrderNo(@Param("orderNo")String orderNo);

    OwnerGoodsEntity getOwnerGoodsByCarNo(@Param("carNo")Integer carNo);

    OwnerGoodsEntity getOwnerGoodsByCarNoAndOrderNo(@Param("carNo")Integer carNo, @Param("orderNo")String orderNo);
    
    List<String> listOrderNoByCarNo(@Param("carNo")Integer carNo);

    OwnerGoodsEntity getOwnerGoodsByPlatNum(@Param("platNum")String platNum);

    LianHeMaiTongOrderDTO getByMemNoAndPlatNum(@Param("memNo")String memNo, @Param("platNum")String platNum);
}
