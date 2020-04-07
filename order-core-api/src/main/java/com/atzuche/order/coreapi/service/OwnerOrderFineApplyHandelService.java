package com.atzuche.order.coreapi.service;

import com.alibaba.fastjson.JSON;
import com.atzuche.order.commons.entity.dto.CostBaseDTO;
import com.atzuche.order.commons.enums.DispatcherStatusEnum;
import com.atzuche.order.commons.enums.FineSubsidyCodeEnum;
import com.atzuche.order.commons.enums.FineSubsidySourceCodeEnum;
import com.atzuche.order.commons.enums.cashcode.FineTypeCashCodeEnum;
import com.atzuche.order.ownercost.entity.OwnerOrderFineApplyEntity;
import com.atzuche.order.ownercost.entity.OwnerOrderFineDeatailEntity;
import com.atzuche.order.ownercost.service.OwnerOrderFineApplyService;
import com.atzuche.order.ownercost.service.OwnerOrderFineDeatailService;
import com.atzuche.order.rentercost.entity.ConsoleRenterOrderFineDeatailEntity;
import com.atzuche.order.rentercost.service.ConsoleRenterOrderFineDeatailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 车主取消罚金后续处理
 *
 * @author pengcheng.fu
 * @date 2020/1/15 10:21
 */

@Service
public class OwnerOrderFineApplyHandelService {

    private static Logger logger = LoggerFactory.getLogger(OwnerOrderFineApplyHandelService.class);

    @Autowired
    private OwnerOrderFineApplyService ownerOrderFineApplyService;
    @Autowired
    private ConsoleRenterOrderFineDeatailService consoleRenterOrderFineDeatailService;
    @Autowired
    private OwnerOrderFineDeatailService ownerOrderFineDeatailService;


    @Transactional(rollbackFor = Exception.class)
    public boolean handleFineApplyRecord(OwnerOrderFineApplyEntity ownerOrderFineApplyEntity, DispatcherStatusEnum dispatcherStatus,
                                         Boolean isSubsidyFineAmt) {
        logger.info("Handle owner order fine apply record. param is,orderNo:[{}],dispatcherStatus:[{}]," +
                        "isSubsidyFineAmt:[{}]",
                JSON.toJSONString(ownerOrderFineApplyEntity), dispatcherStatus, isSubsidyFineAmt);


        if (null == ownerOrderFineApplyEntity) {
            logger.warn("Not fund ownerOrderFineApplyEntity.");
            return false;
        }

        if (null == dispatcherStatus) {
            logger.warn("Dispatcher status is empty. ownerOrderFineApplyEntity:[{}]", JSON.toJSONString(ownerOrderFineApplyEntity));
            return false;
        }

        OwnerOrderFineDeatailEntity entity = new OwnerOrderFineDeatailEntity();
        entity.setOrderNo(ownerOrderFineApplyEntity.getOrderNo());
        entity.setOwnerOrderNo(ownerOrderFineApplyEntity.getOwnerOrderNo());
        entity.setMemNo(ownerOrderFineApplyEntity.getMemNo().toString());
        entity.setFineAmount(-Math.abs(ownerOrderFineApplyEntity.getFineAmount()));
        entity.setFineType(ownerOrderFineApplyEntity.getFineType());
        entity.setFineTypeDesc(ownerOrderFineApplyEntity.getFineTypeDesc());
        entity.setFineSubsidySourceCode(ownerOrderFineApplyEntity.getFineSubsidySourceCode());
        entity.setFineSubsidySourceDesc(ownerOrderFineApplyEntity.getFineSubsidySourceDesc());
        //处理罚金补贴信息
        if (DispatcherStatusEnum.DISPATCH_SUCCESS.getCode() == dispatcherStatus.getCode()) {
            //调度成功,罚金补贴给平台
            entity.setFineSubsidyCode(FineSubsidyCodeEnum.PLATFORM.getFineSubsidyCode());
            entity.setFineSubsidyDesc(FineSubsidyCodeEnum.PLATFORM.getFineSubsidyDesc());
        } else if (DispatcherStatusEnum.DISPATCH_FAIL.getCode() == dispatcherStatus.getCode()) {
            //调度失败,罚金补贴给租客
            entity.setFineSubsidyCode(FineSubsidyCodeEnum.RENTER.getFineSubsidyCode());
            entity.setFineSubsidyDesc(FineSubsidyCodeEnum.RENTER.getFineSubsidyDesc());
            //租客收益信息处理
            CostBaseDTO costBaseDTO = new CostBaseDTO();
            costBaseDTO.setOrderNo(ownerOrderFineApplyEntity.getOrderNo());
            costBaseDTO.setMemNo(ownerOrderFineApplyEntity.getMemNo().toString());

            if(!isSubsidyFineAmt) {
                ConsoleRenterOrderFineDeatailEntity consoleRenterOrderFineDeatailEntity =
                        consoleRenterOrderFineDeatailService.fineDataConvert(costBaseDTO,
                                Math.abs(ownerOrderFineApplyEntity.getFineAmount()), FineSubsidyCodeEnum.RENTER,
                                FineSubsidySourceCodeEnum.OWNER, FineTypeCashCodeEnum.CANCEL_FINE);
                consoleRenterOrderFineDeatailService.saveConsoleRenterOrderFineDeatail(consoleRenterOrderFineDeatailEntity);
            } else {
                entity.setFineSubsidyCode(FineSubsidyCodeEnum.PLATFORM.getFineSubsidyCode());
                entity.setFineSubsidyDesc(FineSubsidyCodeEnum.PLATFORM.getFineSubsidyDesc());
            }
        } else {
            logger.warn("Dispatcher status is invalid. orderNo:[{}],dispatcherStatus:[{}]",
                    ownerOrderFineApplyEntity.getOrderNo(), dispatcherStatus);
            return false;
        }
        //处理车主罚金信息
        ownerOrderFineDeatailService.addOwnerOrderFineRecord(entity);
        //删除罚金请求信息
        return ownerOrderFineApplyService.setInvalid(ownerOrderFineApplyEntity.getId()) > 0;
    }


}
