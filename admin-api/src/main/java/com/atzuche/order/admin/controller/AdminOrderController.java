package com.atzuche.order.admin.controller;

import com.alibaba.fastjson.JSON;
import com.atzuche.order.admin.common.AdminUserUtil;
import com.atzuche.order.admin.constant.AdminOpTypeEnum;
import com.atzuche.order.admin.service.AdminOrderService;
import com.atzuche.order.admin.service.ModificationOrderService;
import com.atzuche.order.admin.service.OperatorLogService;
import com.atzuche.order.admin.service.RemoteFeignService;
import com.atzuche.order.admin.service.car.CarService;
import com.atzuche.order.admin.service.log.AdminLogService;
import com.atzuche.order.admin.vo.req.AdminTransferCarReqVO;
import com.atzuche.order.admin.vo.req.order.*;
import com.atzuche.order.admin.vo.resp.order.AdminModifyOrderFeeCompareVO;
import com.atzuche.order.commons.BindingResultUtil;
import com.atzuche.order.commons.ResponseCheckUtil;
import com.atzuche.order.commons.entity.orderDetailDto.OrderCouponDTO;
import com.atzuche.order.commons.entity.dto.ModifyOrderConsoleDTO;
import com.atzuche.order.commons.entity.orderDetailDto.OrderDetailReqDTO;
import com.atzuche.order.commons.entity.orderDetailDto.OrderDetailRespDTO;
import com.atzuche.order.commons.enums.BuyInsurKeyEnum;
import com.atzuche.order.commons.vo.DebtDetailVO;
import com.atzuche.order.commons.vo.req.ModifyInsurFlagVO;
import com.atzuche.order.commons.vo.req.ModifyOrderReqVO;
import com.atzuche.order.commons.vo.res.AdminOrderJudgeDutyResVO;
import com.atzuche.order.open.service.FeignOrderDetailService;
import com.atzuche.order.open.vo.request.TransferReq;
import com.autoyol.commons.web.ErrorCode;
import com.autoyol.commons.web.ResponseData;
import com.autoyol.doc.annotation.AutoDocGroup;
import com.autoyol.doc.annotation.AutoDocMethod;
import com.autoyol.doc.annotation.AutoDocVersion;
import com.caucho.hessian.io.RemoteDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 订单操作接口
 * @author <a href="mailto:lianglin.sjtu@gmail.com">AndySjtu</a>
 * @date 2020/1/7 11:08 上午
 **/
@Slf4j
@RestController
public class AdminOrderController {
    private final static Logger logger = LoggerFactory.getLogger(AdminOrderController.class);
    
    @Autowired
    private AdminOrderService adminOrderService;
    @Autowired
    private RemoteFeignService remoteFeignService;
    @Autowired
    private AdminLogService adminLogService;
    @Autowired
    private CarService carService;
    @Autowired
    private ModificationOrderService modificationOrderService;
    @Autowired
    private OperatorLogService operatorLogService;

    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "修改订单", value = "修改订单",response = ResponseData.class)
    @RequestMapping(value="console/order/modifyOrder",method = RequestMethod.POST)
    public ResponseData modifyOrder(@RequestBody ModifyOrderReqVO modifyOrderReqVO, BindingResult bindingResult)throws Exception{
        log.info("车辆押金信息-modifyOrderReqVO={}", JSON.toJSONString(modifyOrderReqVO));
        if (bindingResult.hasErrors()) {
            Optional<FieldError> error = bindingResult.getFieldErrors().stream().findFirst();
            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), error.isPresent() ?
                    error.get().getDefaultMessage() : ErrorCode.INPUT_ERROR.getText());
        }
        String orderNo = modifyOrderReqVO.getOrderNo();
        ResponseData<OrderDetailRespDTO> respDTOResponseData =remoteFeignService.getOrderdetailFromRemote(orderNo);
        OrderDetailRespDTO detailRespDTO = respDTOResponseData.getData();
        String  memNo = detailRespDTO.getRenterMember().getMemNo();
        modifyOrderReqVO.setMemNo(memNo);
        modifyOrderReqVO.setConsoleFlag(true);
        modifyOrderReqVO.setOperator(AdminUserUtil.getAdminUser().getAuthName());
        //adminOrderService.modifyOrder(modifyOrderReqVO);
        // 获取修改前数据
 		ModifyOrderConsoleDTO modifyOrderConsoleDTO = remoteFeignService.getInitModifyOrderDTO(modifyOrderReqVO);
        remoteFeignService.modifyOrder(modifyOrderReqVO);
        // 保存操作日志
        modificationOrderService.saveModifyOrderLog(modifyOrderReqVO, modifyOrderConsoleDTO);

        //记录日志
        adminlog(modifyOrderReqVO);

        return ResponseData.success();
    }
    
    
    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "修改是否购买保费", value = "修改是否购买保费",response = ResponseData.class)
    @RequestMapping(value="console/order/modifyinsurflag",method = RequestMethod.POST)
    public ResponseData modifyInsurFlag(@RequestBody ModifyInsurFlagVO modifyInsurFlagVO, BindingResult bindingResult)throws Exception{
        log.info("修改是否购买保费-modifyInsurFlagVO={}", modifyInsurFlagVO);
        if (bindingResult.hasErrors()) {
            Optional<FieldError> error = bindingResult.getFieldErrors().stream().findFirst();
            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), error.isPresent() ?
                    error.get().getDefaultMessage() : ErrorCode.INPUT_ERROR.getText());
        }
        String orderNo = modifyInsurFlagVO.getOrderNo();
        ResponseData<OrderDetailRespDTO> respDTOResponseData =remoteFeignService.getOrderdetailFromRemote(orderNo);

        OrderDetailRespDTO detailRespDTO = respDTOResponseData.getData();
        String  memNo = detailRespDTO.getRenterMember().getMemNo();
        LocalDateTime rentTime = detailRespDTO.getRenterOrder().getExpRentTime();
        LocalDateTime nowTime = LocalDateTime.now();
        if (rentTime != null && nowTime.isAfter(rentTime)) {
        	// 订单开始后不能修改
        	return ResponseData.createErrorCodeResponse("601233", "订单开始后不允许购买。");
        }
        ModifyOrderReqVO modifyOrderReqVO = new ModifyOrderReqVO();
        modifyOrderReqVO.setOrderNo(orderNo);
        modifyOrderReqVO.setMemNo(memNo);
        modifyOrderReqVO.setConsoleFlag(true);
        modifyOrderReqVO.setOperator(AdminUserUtil.getAdminUser().getAuthName());
        if (BuyInsurKeyEnum.ABATEMENTFLAG.getKey().equals(modifyInsurFlagVO.getBuyKey())) {
        	modifyOrderReqVO.setAbatementFlag(modifyInsurFlagVO.getBuyValue());
        } else if (BuyInsurKeyEnum.TYREINSURFLAG.getKey().equals(modifyInsurFlagVO.getBuyKey())) {
        	modifyOrderReqVO.setTyreInsurFlag(modifyInsurFlagVO.getBuyValue());
        	Integer abatementFlag = detailRespDTO.getRenterOrder().getIsAbatement();
        	if (abatementFlag == null || !abatementFlag.equals(1)) {
        		return ResponseData.createErrorCodeResponse("601234", "不能单独购买轮胎/轮毂保障服务，必须同时购买补充保障服务。");
        	}
        } else if (BuyInsurKeyEnum.DRIVERINSURFLAG.getKey().equals(modifyInsurFlagVO.getBuyKey())) {
        	modifyOrderReqVO.setDriverInsurFlag(modifyInsurFlagVO.getBuyValue());
        }
        remoteFeignService.modifyOrder(modifyOrderReqVO);
        // 记录购买日志
        operatorLogService.saveBuyAbatementLog(modifyInsurFlagVO);
        return ResponseData.success();
    }


    private void adminlog(ModifyOrderReqVO modifyOrderReqVO){
        try{
            String orderNo = modifyOrderReqVO.getOrderNo();
            if(StringUtils.isNotBlank(modifyOrderReqVO.getCarOwnerCouponId()) ||
                    StringUtils.isNotBlank(modifyOrderReqVO.getSrvGetReturnCouponId()) ||
                    StringUtils.isNotBlank(modifyOrderReqVO.getPlatformCouponId())){
                List<OrderCouponDTO> orderCouponDTOS = remoteFeignService.queryCouponByOrderNoFromRemote(orderNo);
                if(StringUtils.isNotBlank(modifyOrderReqVO.getCarOwnerCouponId())){
                    OrderCouponDTO orderCouponDTO = filterOrderCouponByCouponId(orderCouponDTOS, modifyOrderReqVO.getCarOwnerCouponId());
                    if(orderCouponDTO != null){
                        String desc = "添加 【"+orderCouponDTO.getCouponName()+"】 "+ orderCouponDTO.getCouponDesc();
                        adminLogService.insertLog(AdminOpTypeEnum.COUPON_EDIT,orderNo,orderCouponDTO.getRenterOrderNo(),null,desc);
                    }
                }

                if(StringUtils.isNotBlank(modifyOrderReqVO.getSrvGetReturnCouponId())){
                    OrderCouponDTO orderCouponDTO = filterOrderCouponByCouponId(orderCouponDTOS, modifyOrderReqVO.getSrvGetReturnCouponId());
                    if(orderCouponDTO != null){
                        String desc = "添加 【"+orderCouponDTO.getCouponName()+"】 "+ orderCouponDTO.getCouponDesc();
                        adminLogService.insertLog(AdminOpTypeEnum.COUPON_EDIT,orderNo,orderCouponDTO.getRenterOrderNo(),null,desc);
                    }
                }

                if(StringUtils.isNotBlank(modifyOrderReqVO.getPlatformCouponId())){
                    OrderCouponDTO orderCouponDTO = filterOrderCouponByCouponId(orderCouponDTOS, modifyOrderReqVO.getPlatformCouponId());
                    if(orderCouponDTO != null){
                        String desc = "添加 【"+orderCouponDTO.getCouponName()+"】 "+ orderCouponDTO.getCouponDesc();
                        adminLogService.insertLog(AdminOpTypeEnum.COUPON_EDIT,orderNo,orderCouponDTO.getRenterOrderNo(),null,desc);
                    }
                }
            }
        }catch (Exception e){
            log.error("优惠券编辑记录日志异常",e);
        }
    }
    private OrderCouponDTO filterOrderCouponByCouponId(List<OrderCouponDTO> orderCouponDTOS, String couponId){
        if(StringUtils.isBlank(couponId)){
            return null;
        }
        Optional<OrderCouponDTO> first = Optional.ofNullable(orderCouponDTOS)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(x -> couponId.equals(x.getCouponId()))
                .findFirst();
        if(first.isPresent()){
            return first.get();
        }
        return null;
    }



    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "平台取消", value = "平台取消",response = ResponseData.class)
    @RequestMapping(value="console/order/cancel/plat",method = RequestMethod.POST)
    public ResponseData cancelOrderByPlat(@RequestBody CancelOrderByPlatVO cancelOrderByPlatVO,BindingResult result, HttpServletRequest request, HttpServletResponse response)throws Exception{
         logger.info("admin={},cancelOrderByPlatVO is {}", AdminUserUtil.getAdminUser(),cancelOrderByPlatVO);
         if(result.hasErrors()){
             Optional<FieldError> error = result.getFieldErrors().stream().findFirst();
             return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), error.isPresent() ?
                     error.get().getDefaultMessage() : ErrorCode.INPUT_ERROR.getText());
         }
         cancelOrderByPlatVO.setOperator(AdminUserUtil.getAdminUser().getAuthName());
         adminOrderService.cancelOrderByAdmin(cancelOrderByPlatVO);
         try{
            adminLogService.insertLog(AdminOpTypeEnum.CANCEL_ORDER_PLAT,cancelOrderByPlatVO.getOrderNo(),AdminOpTypeEnum.CANCEL_ORDER_PLAT.getOpType());
         }catch (Exception e){
             log.error("修改订单-平台取消日志记录异常",e);
         }
         return ResponseData.success();
    }

    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "车主或者租客取消", value = "车主或者租客取消",response = ResponseData.class)
    @RequestMapping(value="console/order/cancel",method = RequestMethod.POST)
    public ResponseData cancelOrder(@Valid @RequestBody CancelOrderVO cancelOrderVO, BindingResult bindingResult)throws Exception{
        log.info("车主或者租客取消-reqVo={}", JSON.toJSONString(cancelOrderVO));
        BindingResultUtil.checkBindingResult(bindingResult);
        ResponseData responseData = adminOrderService.cancelOrder(cancelOrderVO);
        try{
            AdminOpTypeEnum adminOpTypeEnum = AdminOpTypeEnum.OTHER;
            if("1".equals(cancelOrderVO.getMemRole())){
                adminOpTypeEnum = AdminOpTypeEnum.CANCEL_ORDER_OWNER_PLAT;
            }else if("2".equals(cancelOrderVO.getMemRole())){
                adminOpTypeEnum = AdminOpTypeEnum.CANCEL_ORDER_RENTER_PLAT;
            }
            String desc = adminOpTypeEnum.getOpType()+" 取消原因："+cancelOrderVO.getCancelReason()==null?"":cancelOrderVO.getCancelReason();
            adminLogService.insertLog(adminOpTypeEnum,cancelOrderVO.getOrderNo(),desc);
        }catch (Exception e){
            log.error("修改订单-平台取消日志记录异常",e);
        }
        return responseData;
    }


    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "车主同意订单", value = "车主同意订单",response = ResponseData.class)
    @RequestMapping(value="console/order/owner/agree",method = RequestMethod.POST)
    public ResponseData agreeOrder(@Valid @RequestBody OwnerAgreeOrRefuseOrderReqVO reqVO,
                               BindingResult bindingResult){
        log.info("车主同意订单-reqVo={}", JSON.toJSONString(reqVO));
        BindingResultUtil.checkBindingResult(bindingResult);

        return adminOrderService.agreeOrder(reqVO);
    }


    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "车主拒绝订单", value = "车主拒绝订单",response = ResponseData.class)
    @RequestMapping(value="console/order/owner/refuse",method = RequestMethod.POST)
    public ResponseData refuseOrder(@Valid @RequestBody OwnerAgreeOrRefuseOrderReqVO reqVO,
                                    BindingResult bindingResult){
        log.info("车主拒绝订单-reqVo={}", JSON.toJSONString(reqVO));
        BindingResultUtil.checkBindingResult(bindingResult);

        return adminOrderService.refuseOrder(reqVO);
    }


    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "取消订单责任判定", value = "取消订单责任判定",response = ResponseData.class)
    @RequestMapping(value="console/order/cancel/judgeDuty",method = RequestMethod.POST)
    public ResponseData judgeDuty(@Valid @RequestBody CancelOrderJudgeDutyReqVO reqVO,
                                    BindingResult bindingResult){
        log.info("取消订单责任判定-reqVo={}", JSON.toJSONString(reqVO));
        BindingResultUtil.checkBindingResult(bindingResult);
        return adminOrderService.cancelOrderJudgeDuty(reqVO);
    }


    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "取消订单责任判定列表(取消/申诉--手动/自动判责)", value = "取消订单责任判定列表(取消/申诉--手动/自动判责)", response = AdminOrderJudgeDutyResVO.class)
    @RequestMapping(value = "console/order/cancel/judgeDuty/list", method = RequestMethod.POST)
    public ResponseData<AdminOrderJudgeDutyResVO> judgeDutyList(@Valid @RequestBody CancelOrderJudgeDutyListReqVO reqVO,
                                                                BindingResult bindingResult) {
        log.info("取消/申诉--手动/自动判责列表-reqVo={}", JSON.toJSONString(reqVO));
        BindingResultUtil.checkBindingResult(bindingResult);
        return adminOrderService.cancelOrderJudgeDutyList(reqVO.getOrderNo());
    }


    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "车主拒绝或者同意租客的订单修改申请接口", value = "车主拒绝或者同意租客的订单修改申请接口",response = ResponseData.class)
    @RequestMapping(value="console/order/modify/confirm",method = RequestMethod.POST)
    public ResponseData modifyApplicationConfirm(@Valid @RequestBody OrderModifyConfirmReqVO reqVO,BindingResult result){
        log.info("reqVo is {}",reqVO);
        if(result.hasErrors()){
            Optional<FieldError> error = result.getFieldErrors().stream().findFirst();
            return ResponseData.createErrorCodeResponse(ErrorCode.INPUT_ERROR.getCode(), error.isPresent() ?
                    error.get().getDefaultMessage() : ErrorCode.INPUT_ERROR.getText());
        }

        adminOrderService.modificationConfirm(reqVO);
        return ResponseData.success();

    }

    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "订单修改前的费用对比", value = "订单修改前的费用对比",response = AdminModifyOrderFeeCompareVO.class)
    @RequestMapping(value="console/order/modify/prefee",method = RequestMethod.POST)
    public ResponseData<AdminModifyOrderFeeCompareVO> preModifyOrder(@Valid @RequestBody AdminModifyOrderReqVO reqVO,BindingResult bindingResult){
        BindingResultUtil.checkBindingResult(bindingResult);

        String renterNo = adminOrderService.getRenterMemNo(reqVO.getOrderNo());

        AdminModifyOrderFeeCompareVO compareVO = adminOrderService.preModifyOrderFee(reqVO,renterNo);
        // 获取欠款
		DebtDetailVO debtDetailVO = adminOrderService.getDebtAmt(renterNo);
		compareVO.setDebtDetailVO(debtDetailVO);
		 
        return ResponseData.success(compareVO);

    }


    @AutoDocVersion(version = "订单修改")
    @AutoDocGroup(group = "订单修改")
    @AutoDocMethod(description = "管理后台换车", value = "管理后台换车")
    @RequestMapping(value="console/changeCar",method = RequestMethod.POST)
    public ResponseData<?> changeCar(@Valid @RequestBody AdminTransferCarReqVO reqVO, BindingResult bindingResult){
        BindingResultUtil.checkBindingResult(bindingResult);
        if (StringUtils.isBlank(reqVO.getCarNo()) && StringUtils.isBlank(reqVO.getPlateNum())) {
        	return ResponseData.createErrorCodeResponse("408508", "车辆号和车牌号二者必选其一");
        }
        // 根据车牌号获取车辆注册号
        String carNo = StringUtils.isBlank(reqVO.getCarNo()) ? carService.getCarNoByPlateNum(reqVO.getPlateNum()):reqVO.getCarNo();
        TransferReq req = new TransferReq();
        req.setOperator(AdminUserUtil.getAdminUser().getAuthName());
        BeanUtils.copyProperties(reqVO,req);
        req.setCarNo(carNo);
        String oldPlateNum = remoteFeignService.getCarPlateNum(reqVO.getOrderNo());
        adminOrderService.transferCar(req);
        String updPlateNum = remoteFeignService.getCarPlateNum(reqVO.getOrderNo());
        // 保存操作日志
        modificationOrderService.saveTransferLog(reqVO.getOrderNo(), oldPlateNum, updPlateNum);
        return ResponseData.success();


    }




}
