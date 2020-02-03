package com.atzuche.order.coreapi.controller;

import com.alibaba.fastjson.JSON;
import com.atzuche.order.commons.BindingResultUtil;
import com.atzuche.order.commons.LocalDateTimeUtils;
import com.atzuche.order.commons.OrderException;
import com.atzuche.order.commons.vo.req.AdminOrderReqVO;
import com.atzuche.order.commons.vo.req.NormalOrderReqVO;
import com.atzuche.order.commons.vo.req.OrderReqVO;
import com.atzuche.order.commons.vo.res.OrderResVO;
import com.atzuche.order.coreapi.service.StockService;
import com.atzuche.order.coreapi.service.SubmitOrderService;
import com.atzuche.order.parentorder.entity.OrderRecordEntity;
import com.atzuche.order.parentorder.service.OrderRecordService;
import com.autoyol.commons.web.ErrorCode;
import com.autoyol.commons.web.ResponseData;
import com.autoyol.doc.annotation.AutoDocMethod;
import com.autoyol.doc.annotation.AutoDocVersion;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 下单
 *
 * @author pengcheng.fu
 * @date 2019/12/23 12:00
 */
@RequestMapping("/order")
@RestController
@AutoDocVersion(version = "订单接口文档")
public class SubmitOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmitOrderController.class);

    @Resource
    private SubmitOrderService submitOrderService;
    @Autowired
    private OrderRecordService orderRecordService;
    @Autowired
    private StockService stockService;

    @AutoDocMethod(description = "提交订单", value = "提交订单", response = OrderResVO.class)
    @PostMapping("/normal/req")
    public ResponseData<OrderResVO> submitOrder(@Valid @RequestBody NormalOrderReqVO normalOrderReqVO, BindingResult bindingResult) throws Exception {
        LOGGER.info("Submit order.param is,normalOrderReqVO:[{}]", JSON.toJSONString(normalOrderReqVO));
        BindingResultUtil.checkBindingResult(bindingResult);

        String memNo = normalOrderReqVO.getMemNo();
        if (StringUtils.isBlank(memNo)) {
            return new ResponseData<>(ErrorCode.NEED_LOGIN.getCode(), ErrorCode.NEED_LOGIN.getText());
        }
        OrderResVO orderResVO = null;
        try{
            BeanCopier beanCopier = BeanCopier.create(NormalOrderReqVO.class, OrderReqVO.class, false);
            OrderReqVO orderReqVO = new OrderReqVO();
            beanCopier.copy(normalOrderReqVO, orderReqVO, null);
            orderReqVO.setAbatement(Integer.valueOf(normalOrderReqVO.getAbatement()));
            orderReqVO.setIMEI(normalOrderReqVO.getIMEI());
            orderReqVO.setRentTime(LocalDateTimeUtils.parseStringToDateTime(normalOrderReqVO.getRentTime(),
                    LocalDateTimeUtils.DEFAULT_PATTERN));
            orderReqVO.setRevertTime(LocalDateTimeUtils.parseStringToDateTime(normalOrderReqVO.getRevertTime(),
                    LocalDateTimeUtils.DEFAULT_PATTERN));

            orderResVO = submitOrderService.submitOrder(orderReqVO);

            OrderRecordEntity orderRecordEntity = new OrderRecordEntity();
            orderRecordEntity.setErrorCode(ErrorCode.SUCCESS.getCode());
            orderRecordEntity.setErrorTxt(ErrorCode.SUCCESS.getText());
            orderRecordEntity.setMemNo(normalOrderReqVO.getMemNo());
            orderRecordEntity.setOrderNo(orderResVO.getOrderNo());
            orderRecordEntity.setParam(JSON.toJSONString(normalOrderReqVO));
            orderRecordEntity.setResult(JSON.toJSONString(orderResVO));
            orderRecordService.save(orderRecordEntity);
            //TODO:发送订单成功的MQ事件
        }catch(OrderException orderException){
            String orderNo = orderResVO==null?"":orderResVO.getOrderNo();
            OrderRecordEntity orderRecordEntity = new OrderRecordEntity();
            orderRecordEntity.setErrorCode(orderException.getErrorCode());
            orderRecordEntity.setErrorTxt(orderException.getErrorMsg());
            orderRecordEntity.setMemNo(normalOrderReqVO.getMemNo());
            orderRecordEntity.setOrderNo(orderNo);
            orderRecordEntity.setParam(JSON.toJSONString(normalOrderReqVO));
            orderRecordEntity.setResult(JSON.toJSONString(orderResVO));
            orderRecordService.save(orderRecordEntity);

            //释放库存
            if(orderNo != null && orderNo.trim().length()>0){
                Integer carNo = Integer.valueOf(normalOrderReqVO.getCarNo());
                stockService.releaseCarStock(orderNo,carNo);
            }
            throw orderException;
        }catch (Exception e){
            String orderNo = orderResVO==null?"":orderResVO.getOrderNo();
            OrderRecordEntity orderRecordEntity = new OrderRecordEntity();
            orderRecordEntity.setErrorCode(ErrorCode.SYS_ERROR.getCode());
            orderRecordEntity.setErrorTxt(ErrorCode.SYS_ERROR.getText());
            orderRecordEntity.setMemNo(normalOrderReqVO.getMemNo());
            orderRecordEntity.setOrderNo(orderResVO==null?"":orderResVO.getOrderNo());
            orderRecordEntity.setParam(JSON.toJSONString(normalOrderReqVO));
            orderRecordEntity.setResult(JSON.toJSONString(orderResVO));
            orderRecordService.save(orderRecordEntity);

            //释放库存
            if(orderNo != null && orderNo.trim().length()>0){
                Integer carNo = Integer.valueOf(normalOrderReqVO.getCarNo());
                stockService.releaseCarStock(orderNo,carNo);
            }
            throw e;
        }
        return ResponseData.success(orderResVO);
    }



    @AutoDocMethod(description = "提交订单(管理后台)", value = "提交订单(管理后台)", response = OrderResVO.class)
    @PostMapping("/admin/req")
    public ResponseData<OrderResVO> submitOrder(@Valid @RequestBody AdminOrderReqVO adminOrderReqVO,
                                                BindingResult bindingResult) {
        LOGGER.info("Submit order.param is,adminOrderReqVO:[{}]", JSON.toJSONString(adminOrderReqVO));
        BindingResultUtil.checkBindingResult(bindingResult);
        String memNo = adminOrderReqVO.getMemNo();
        if (StringUtils.isBlank(memNo)) {
            return new ResponseData<>(ErrorCode.NEED_LOGIN.getCode(), ErrorCode.NEED_LOGIN.getText());
        }
        OrderResVO orderResVO = null;
        try{
            BeanCopier beanCopier = BeanCopier.create(AdminOrderReqVO.class, OrderReqVO.class, false);
            OrderReqVO orderReqVO = new OrderReqVO();
            beanCopier.copy(adminOrderReqVO, orderReqVO, null);
            orderReqVO.setRentTime(LocalDateTimeUtils.parseStringToDateTime(adminOrderReqVO.getRentTime(),
                    LocalDateTimeUtils.DEFAULT_PATTERN));
            orderReqVO.setRevertTime(LocalDateTimeUtils.parseStringToDateTime(adminOrderReqVO.getRevertTime(),
                    LocalDateTimeUtils.DEFAULT_PATTERN));

            orderResVO = submitOrderService.submitOrder(orderReqVO);
            OrderRecordEntity orderRecordEntity = new OrderRecordEntity();
            orderRecordEntity.setErrorCode(ErrorCode.SUCCESS.getCode());
            orderRecordEntity.setErrorTxt(ErrorCode.SUCCESS.getText());
            orderRecordEntity.setMemNo(adminOrderReqVO.getMemNo());
            orderRecordEntity.setOrderNo(orderResVO.getOrderNo());
            orderRecordEntity.setParam(JSON.toJSONString(adminOrderReqVO));
            orderRecordEntity.setResult(JSON.toJSONString(orderResVO));
            orderRecordService.save(orderRecordEntity);
        }catch(OrderException orderException){
            OrderRecordEntity orderRecordEntity = new OrderRecordEntity();
            orderRecordEntity.setErrorCode(orderException.getErrorCode());
            orderRecordEntity.setErrorTxt(orderException.getErrorMsg());
            orderRecordEntity.setMemNo(adminOrderReqVO.getMemNo());
            orderRecordEntity.setOrderNo(orderResVO==null?"":orderResVO.getOrderNo());
            orderRecordEntity.setParam(JSON.toJSONString(adminOrderReqVO));
            orderRecordEntity.setResult(JSON.toJSONString(orderResVO));
            orderRecordService.save(orderRecordEntity);
            //释放库存
            String orderNo = orderResVO==null?"":orderResVO.getOrderNo();
            if(orderNo != null && orderNo.trim().length()>0){
                Integer carNo = Integer.valueOf(adminOrderReqVO.getCarNo());
                stockService.releaseCarStock(orderNo,carNo);
            }
            throw orderException;
        }catch (Exception e){
            OrderRecordEntity orderRecordEntity = new OrderRecordEntity();
            orderRecordEntity.setErrorCode(ErrorCode.SYS_ERROR.getCode());
            orderRecordEntity.setErrorTxt(ErrorCode.SYS_ERROR.getText());
            orderRecordEntity.setMemNo(adminOrderReqVO.getMemNo());
            orderRecordEntity.setOrderNo(orderResVO==null?"":orderResVO.getOrderNo());
            orderRecordEntity.setParam(JSON.toJSONString(adminOrderReqVO));
            orderRecordEntity.setResult(JSON.toJSONString(orderResVO));
            orderRecordService.save(orderRecordEntity);
            //释放库存
            String orderNo = orderResVO==null?"":orderResVO.getOrderNo();
            if(orderNo != null && orderNo.trim().length()>0){
                Integer carNo = Integer.valueOf(adminOrderReqVO.getCarNo());
                stockService.releaseCarStock(orderNo,carNo);
            }
            throw e;
        }

        return ResponseData.success(orderResVO);
    }
}
