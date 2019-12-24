package com.atzuche.order.rentercost.entity.dto;

import com.atzuche.order.commons.entity.dto.*;
import lombok.Data;

@Data
public class RenterOrderCostReqDTO {
    /**
     * 基本信息
     */
    private CostBaseDTO costBaseDTO;
    /**
     * 租车费用参数
     */
    private RentAmtDTO rentAmtDTO;

    /**
     * 平台保障费参数
     */
    private InsurAmtDTO insurAmtDTO;

    /**
     * 全面保障费参数
     */
    private AbatementAmtDTO abatementAmtDTO;

    /**
     * 全面保障费参数
     */
    private ExtraDriverDTO extraDriverDTO;

    /**
     * 超里程费用参数
     */
    private MileageAmtDTO mileageAmtDTO;


}
