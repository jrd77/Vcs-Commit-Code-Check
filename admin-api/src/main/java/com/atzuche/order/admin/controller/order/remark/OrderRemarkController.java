package com.atzuche.order.admin.controller.order.remark;

import com.atzuche.order.admin.controller.BaseController;
import com.atzuche.order.admin.service.remark.OrderRemarkLogService;
import com.atzuche.order.admin.service.remark.OrderRemarkService;
import com.atzuche.order.admin.vo.req.remark.*;
import com.atzuche.order.admin.vo.resp.remark.OrderRemarkLogPageListResponseVO;
import com.atzuche.order.admin.vo.resp.remark.OrderRemarkOverviewListResponseVO;
import com.atzuche.order.admin.vo.resp.remark.OrderRemarkPageListResponseVO;
import com.atzuche.order.admin.vo.resp.remark.OrderRemarkResponseVO;
import com.autoyol.commons.web.ResponseData;
import com.autoyol.doc.annotation.AutoDocMethod;
import com.autoyol.doc.annotation.AutoDocVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/console/order/remark")
@RestController
@AutoDocVersion(version = "订单备注接口文档")
public class OrderRemarkController extends BaseController {


    private static final Logger logger = LoggerFactory.getLogger(OrderRemarkController.class);

    @Autowired
    OrderRemarkService orderRemarkService;

    @Autowired
    OrderRemarkLogService orderLogRemarkService;

	@AutoDocMethod(description = "备注总览", value = "备注总览", response = OrderRemarkOverviewListResponseVO.class)
	@GetMapping("/overview")
	public ResponseData<OrderRemarkOverviewListResponseVO> getOverview(OrderRemarkRequestVO orderRemarkRequestVO, BindingResult bindingResult) {
        validate(bindingResult);
	    try{
            logger.info("获取备注总览{}",orderRemarkRequestVO.toString());
            return ResponseData.success(orderRemarkService.getOrderRemarkOverview(orderRemarkRequestVO));
        } catch (Exception e) {
            logger.info("获取备注总览{}",e);
        }
		return null;
	}

    @AutoDocMethod(description = "备注查询列表", value = "备注查询列表", response = OrderRemarkPageListResponseVO.class)
    @GetMapping("/list")
    public ResponseData<OrderRemarkPageListResponseVO> selectRemarklist(OrderRemarkListRequestVO orderRemarkListRequestVO, BindingResult bindingResult) {
        validate(bindingResult);
        return ResponseData.success(orderRemarkService.selectRemarklist(orderRemarkListRequestVO));
    }

    @AutoDocMethod(description = "备注日志查询列表", value = "备注日志查询列表", response = OrderRemarkLogPageListResponseVO.class)
    @GetMapping("/log/list")
    public ResponseData<OrderRemarkLogPageListResponseVO> logList(OrderRemarkLogListRequestVO orderRemarkLogListRequestVO, BindingResult bindingResult) {
        validate(bindingResult);
        return ResponseData.success(orderLogRemarkService.selectRemarkLoglist(orderRemarkLogListRequestVO));
    }


    @AutoDocMethod(description = "添加备注", value = "添加备注", response = ResponseData.class)
    @PostMapping("/add")
    public ResponseData<ResponseData> addOrderRemark(@RequestBody OrderRemarkAdditionRequestVO orderRemarkAdditionRequestVO, BindingResult bindingResult) {
        validate(bindingResult);
        orderRemarkService.addOrderRemark(orderRemarkAdditionRequestVO);
        return ResponseData.success(null);
    }

    @AutoDocMethod(description = "删除备注", value = "删除备注", response = ResponseData.class)
    @DeleteMapping("/delete")
    public ResponseData<ResponseData> delete(@RequestBody OrderRemarkDeleteRequestVO orderRemarkDeleteRequestVO, BindingResult bindingResult) {
        validate(bindingResult);
        orderRemarkService.deleteRemarkById(orderRemarkDeleteRequestVO);
        return ResponseData.success(null);
    }

    @AutoDocMethod(description = "编辑备注", value = "编辑备注", response = ResponseData.class)
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public ResponseData<ResponseData> update(@RequestBody OrderRemarkUpdateRequestVO orderRemarkUpdateRequestVO, BindingResult bindingResult) {
        validate(bindingResult);
        orderRemarkService.updateRemarkById(orderRemarkUpdateRequestVO);
        return ResponseData.success(null);
    }

    @AutoDocMethod(description = "获取备注信息", value = "获取备注信息", response = OrderRemarkResponseVO.class)
    @GetMapping("/information")
    public ResponseData<OrderRemarkResponseVO> getRemarkInformation(OrderRemarkInformationRequestVO orderRemarkInformationRequestVO, BindingResult bindingResult) {
        validate(bindingResult);
	    return ResponseData.success(orderRemarkService.getOrderRemarkInformation(orderRemarkInformationRequestVO));
    }

}
