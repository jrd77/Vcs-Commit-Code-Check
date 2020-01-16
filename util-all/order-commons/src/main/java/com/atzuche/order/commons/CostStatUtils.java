package com.atzuche.order.commons;
/*
 * @Author ZhangBin
 * @Date 2020/1/15 15:54
 * @Description: 费用统计类
 *
 **/

import com.atzuche.order.commons.entity.orderDetailDto.*;
import com.atzuche.order.commons.entity.ownerOrderDetail.RenterOwnerPriceDTO;
import com.atzuche.order.commons.enums.ConsoleCostCashNoEnum;
import com.atzuche.order.commons.enums.OwnerCashCodeEnum;
import com.atzuche.order.commons.enums.OwnerFineTypeEnum;
import com.atzuche.order.commons.enums.SubsidySourceCodeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CostStatUtils {

    /*
     * @Author ZhangBin
     * @Date 2020/1/15 15:55
     * @Description: 获取车主租金
     *
     **/
    public static int getOwnerRentAmt(List<OwnerOrderPurchaseDetailDTO> list){
        Integer totalAmt = Optional.ofNullable(list)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(x -> OwnerCashCodeEnum.RENT_AMT.getCashNo().equals(x.getCostCode()))
                .collect(Collectors.summingInt(OwnerOrderPurchaseDetailDTO::getTotalAmount));
        return totalAmt==null?0:totalAmt;
    }

    /*
     * @Author ZhangBin
     * @Date 2020/1/15 16:09
     * @Description: 车主罚金统计
     *
     **/
    public static int getOwnerFienAmt(List<OwnerOrderFineDeatailDTO> ownerOrderFineDeatailDTOS,List<ConsoleOwnerOrderFineDeatailDTO> consoleOwnerOrderFineDeatailDTOList){
        Integer ownerOrderFineAmt = Optional.ofNullable(ownerOrderFineDeatailDTOS)
                .orElseGet(ArrayList::new)
                .stream()
                .collect(Collectors.summingInt(OwnerOrderFineDeatailDTO::getFineAmount));

        Integer consoleOwnerOrderFineAmt = Optional.ofNullable(consoleOwnerOrderFineDeatailDTOList)
                .orElseGet(ArrayList::new)
                .stream()
                .collect(Collectors.summingInt(ConsoleOwnerOrderFineDeatailDTO::getFineAmount));

        if(ownerOrderFineAmt == null){
            ownerOrderFineAmt = 0;
        }
        if(consoleOwnerOrderFineAmt == null){
            consoleOwnerOrderFineAmt = 0;
        }
        return ownerOrderFineAmt + consoleOwnerOrderFineAmt;
    }

    /*
     * @Author ZhangBin
     * @Date 2020/1/15 17:48
     * @Description: 车主对租客的相互调价
     * 
     **/
    public static RenterOwnerPriceDTO ownerRenterPrice(List<OwnerOrderSubsidyDetailDTO> ownerOrderSubsidyDetailDTOS){
        Integer amt = Optional.ofNullable(ownerOrderSubsidyDetailDTOS)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(x -> OwnerCashCodeEnum.OWNER_RENTER_PRICE.getCashNo().equals(x.getSubsidyCostCode()))
                .collect(Collectors.summingInt(OwnerOrderSubsidyDetailDTO::getSubsidyAmount));
        if(amt == null){
            amt = 0;
        }
        RenterOwnerPriceDTO renterOwnerPriceDTO = new RenterOwnerPriceDTO();
        renterOwnerPriceDTO.setOwnerToRenterPrice(amt);
        renterOwnerPriceDTO.setRenterToOwnerPrice(amt);
        return renterOwnerPriceDTO;
    }
    /*
     * @Author ZhangBin
     * @Date 2020/1/15 18:01
     * @Description: 通过费用编码获取补贴
     *
     **/
    public static OwnerOrderSubsidyDetailDTO ownerSubsidtyFilterByCashNo(String cashNo,List<OwnerOrderSubsidyDetailDTO> list){
        if(cashNo == null){
            return null;
        }
        List<OwnerOrderSubsidyDetailDTO> collect = Optional.ofNullable(list).orElseGet(ArrayList::new)
                .stream()
                .filter(x -> cashNo.equals(x.getSubsidyCostCode()))
                .collect(Collectors.toList());
        if(collect == null || collect.size()<=0){
            return null;
        }
        return collect.get(0);
    }

    /*
     * @Author ZhangBin
     * @Date 2020/1/15 20:07
     * @Description: 通过费用编码过滤车主补贴费用
     *
     **/
    public static int ownerSubsidtyAmtFilterByCashNo(OwnerCashCodeEnum ownerCashCodeEnum,List<OwnerOrderSubsidyDetailDTO> list){
        if(ownerCashCodeEnum == null){
            return 0;
        }
        List<OwnerOrderSubsidyDetailDTO> collect = Optional.ofNullable(list).orElseGet(ArrayList::new)
                .stream()
                .filter(x -> ownerCashCodeEnum.getCashNo().equals(x.getSubsidyCostCode()))
                .collect(Collectors.toList());
        if(collect == null || collect.size()<=0){
            return 0;
        }
        return collect.get(0).getSubsidyAmount();
    }

    /*
     * @Author ZhangBin
     * @Date 2020/1/15 20:07
     * @Description: 平台给车主的补贴
     * 
     **/
    public static List<OwnerOrderSubsidyDetailDTO> getPlatformToOwnerSubsidyList(List<OwnerOrderSubsidyDetailDTO> list){
        List<OwnerOrderSubsidyDetailDTO> collect = Optional.ofNullable(list)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(x -> SubsidySourceCodeEnum.PLATFORM.equals(x.getSubsidySourceCode()))
                .collect(Collectors.toList());
        return collect==null?new ArrayList<>():collect;
    }
    /*
     * @Author ZhangBin
     * @Date 2020/1/15 20:12
     * @Description: 统计金额
     * 
     **/
    public static int calAmt(List<OwnerOrderSubsidyDetailDTO> list){
        Integer collect = Optional.ofNullable(list)
                .orElseGet(ArrayList::new)
                .stream()
                .collect(Collectors.summingInt(OwnerOrderSubsidyDetailDTO::getSubsidyAmount));
        if(collect == null){
            return 0;
        }
        return collect;
    }
    
    /*
     * @Author ZhangBin
     * @Date 2020/1/15 20:27 
     * @Description: 通过cashno查询车主增值费用
     * 
     **/
    public static int getIncrementByCashNo(OwnerCashCodeEnum ownerCashCodeEnum,List<OwnerOrderIncrementDetailDTO> ownerOrderIncrementDetailDTOS){
        if(ownerCashCodeEnum == null){
            return 0;
        }
        Integer amt = Optional.ofNullable(ownerOrderIncrementDetailDTOS).orElseGet(ArrayList::new)
                .stream()
                .filter(x -> ownerCashCodeEnum.getCashNo().equals(x.getCostCode()))
                .collect(Collectors.summingInt(OwnerOrderIncrementDetailDTO::getTotalAmount));
        if(amt ==null){
            return 0;
        }
        return amt;
    }

    /*
     * @Author ZhangBin
     * @Date 2020/1/16 14:24
     * @Description: 通过费用编码计算罚金
     *
     **/
    public static int calOwnerFineByCashNo(OwnerFineTypeEnum ownerFineTypeEnum,List<OwnerOrderFineDeatailDTO> ownerOrderFineDeatailDTOS){
        if(ownerOrderFineDeatailDTOS == null || ownerOrderFineDeatailDTOS.size()<=0){
            return 0;
        }
        if(ownerFineTypeEnum == null){
            return 0;
        }
        Integer amt = ownerOrderFineDeatailDTOS
                .stream()
                .filter(x -> ownerFineTypeEnum.getFineType().equals(x.getFineType()))
                .collect(Collectors.summingInt(OwnerOrderFineDeatailDTO::getFineAmount));
        if(amt == null){
            return 0;
        }
        return amt;
    }

    public static int calConsoleAmtByCashNo(ConsoleCostCashNoEnum consoleCostCashNoEnum,List<OrderConsoleCostDetailDTO> list) {
        if(consoleCostCashNoEnum == null){
            return 0;
        }
        Integer amt = Optional.ofNullable(list).orElseGet(ArrayList::new)
                .stream()
                .filter(x -> consoleCostCashNoEnum.getCashNo().equals(x.getSubsidyTypeCode()))
                .collect(Collectors.summingInt(OrderConsoleCostDetailDTO::getSubsidyAmount));
        if(amt == null ){
            return amt;
        }
        return 0;
    }
}
