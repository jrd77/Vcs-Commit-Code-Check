package com.atzuche.order.admin.service;

import com.atzuche.order.accountrenterdeposit.exception.AccountRenterDepositDBException;
import com.atzuche.order.admin.common.AdminUserUtil;
import com.atzuche.order.admin.vo.req.renterWz.RenterWzCostDetailReqVO;
import com.atzuche.order.admin.vo.req.renterWz.TemporaryRefundReqVO;
import com.atzuche.order.admin.vo.resp.renterWz.*;
import com.atzuche.order.cashieraccount.service.CashierQueryService;
import com.atzuche.order.cashieraccount.vo.res.WzDepositMsgResVO;
import com.atzuche.order.commons.CompareHelper;
import com.atzuche.order.commons.DateUtils;
import com.atzuche.order.commons.enums.ErrorCode;
import com.atzuche.order.parentorder.entity.OrderStatusEntity;
import com.atzuche.order.parentorder.service.OrderStatusService;
import com.atzuche.order.rentercommodity.service.RenterGoodsService;
import com.atzuche.order.rentermem.service.RenterMemberService;
import com.atzuche.order.renterorder.entity.RenterOrderEntity;
import com.atzuche.order.renterorder.service.RenterOrderService;
import com.atzuche.order.renterwz.entity.RenterOrderWzCostDetailEntity;
import com.atzuche.order.renterwz.entity.WzCostLogEntity;
import com.atzuche.order.renterwz.entity.WzTemporaryRefundLogEntity;
import com.atzuche.order.renterwz.enums.WzCostEnums;
import com.atzuche.order.renterwz.service.RenterOrderWzCostDetailService;
import com.atzuche.order.renterwz.service.WzCostLogService;
import com.atzuche.order.renterwz.service.WzTemporaryRefundLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * RenterWzService
 *
 * @author shisong
 * @date 2020/1/6
 */
@Service
public class RenterWzService {

    @Resource
    private RenterOrderWzCostDetailService renterOrderWzCostDetailService;

    @Resource
    private WzCostLogService wzCostLogService;

    @Resource
    private RenterGoodsService renterGoodsService;

    @Resource
    private RenterMemberService renterMemberService;

    @Resource
    private WzTemporaryRefundLogService wzTemporaryRefundLogService;

    @Resource
    private CashierQueryService cashierQueryService;

    @Resource
    private OrderStatusService orderStatusService;

    @Resource
    private RenterOrderService renterOrderService;

    private static final String WZ_OTHER_FINE_REMARK = "其他扣款备注";
    private static final String WZ_OTHER_FINE = "其他扣款";
    private static final String WZ_OTHER_FINE_CODE = "100044";

    private static final String INSURANCE_CLAIM_REMARK = "保险理赔备注";
    private static final String INSURANCE_CLAIM = "保险理赔";
    private static final String INSURANCE_CLAIM_CODE = "100045";

    private static final String REMARK = "remark";
    private static final String AMOUNT = "amount";

    private static final String SOURCE_TYPE_CONSOLE = "2";

    private static final String RADIX_POINT = ".";

    private static final List<String> COST_CODE_LIST = Arrays.asList("100040","100042","100041","100043","100044","100045");


    public void updateWzCost(String orderNo, List<RenterWzCostDetailReqVO> costDetails) {
        OrderStatusEntity orderStatus = orderStatusService.getByOrderNo(orderNo);
        if(orderStatus != null && orderStatus.getWzSettleStatus() != null && orderStatus.getWzSettleStatus().equals(1)){
            throw new AccountRenterDepositDBException(ErrorCode.RENTER_WZ_SETTLED.getCode(),ErrorCode.RENTER_WZ_SETTLED.getText());
        }
        //只会处理其他扣款 和 保险理赔
        for (RenterWzCostDetailReqVO costDetail : costDetails) {
            if(!WZ_OTHER_FINE_CODE.equals(costDetail.getCostCode()) && !INSURANCE_CLAIM_CODE.equals(costDetail.getCostCode())){
                continue;
            }
            RenterOrderWzCostDetailEntity fromDb = renterOrderWzCostDetailService.queryInfoByOrderAndCode(orderNo, costDetail.getCostCode());
            try {
                RenterOrderWzCostDetailEntity fromApp = new RenterOrderWzCostDetailEntity();
                BeanUtils.copyProperties(costDetail,fromApp);
                if(StringUtils.isNotBlank(costDetail.getAmount())){
                    fromApp.setAmount(Integer.parseInt(costDetail.getAmount()));
                }
                Map<String,String> paramNames = this.getParamNamesByCode(costDetail.getCostCode());
                CompareHelper<RenterOrderWzCostDetailEntity> compareHelper = new CompareHelper<>(fromDb,fromApp,paramNames);
                String content = compareHelper.compare();
                if(StringUtils.isNotBlank(content)){
                    //记录日志 并且做修改费用处理
                    updateCostStatus(orderNo, costDetail, fromDb);
                    saveWzCostLog(orderNo, costDetail, content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCostStatus(String orderNo, RenterWzCostDetailReqVO costDetail, RenterOrderWzCostDetailEntity fromDb) {
        String carNum;
        Integer memNo = null;
        if(fromDb != null){
            carNum = fromDb.getCarPlateNum();
            memNo = fromDb.getMemNo();
        }else{
            carNum = renterGoodsService.queryCarNumByOrderNo(orderNo);
            String renterNoByOrderNo = renterMemberService.getRenterNoByOrderNo(orderNo);
            if(StringUtils.isNotBlank(renterNoByOrderNo)){
                memNo = Integer.parseInt(renterNoByOrderNo);
            }
        }
        //先将之前的置为无效
        renterOrderWzCostDetailService.updateCostStatusByOrderNoAndCarNumAndMemNoAndCostCode(orderNo,carNum,memNo,1,costDetail.getCostCode());
        //再新添加
        RenterOrderWzCostDetailEntity entityByType = getEntityByType(costDetail.getCostCode(), orderNo, costDetail.getAmount(), carNum, memNo,costDetail.getRemark());
        renterOrderWzCostDetailService.saveRenterOrderWzCostDetail(entityByType);
    }

    private RenterOrderWzCostDetailEntity getEntityByType(String code, String orderNo, String amount, String carNum, Integer memNo, String remark){
        String authName = AdminUserUtil.getAdminUser().getAuthName();
        String authId = AdminUserUtil.getAdminUser().getAuthId();
        RenterOrderWzCostDetailEntity entity = new RenterOrderWzCostDetailEntity();
        entity.setOrderNo(orderNo);
        entity.setCarPlateNum(carNum);
        if(StringUtils.isNotBlank(amount)){
            entity.setAmount(Integer.parseInt(amount));
        }else{
            entity.setAmount(0);
        }
        entity.setMemNo(memNo);
        entity.setCostCode(code);
        entity.setCostDesc(WzCostEnums.getDesc(code));
        entity.setCreateTime(new Date());
        entity.setSourceType(SOURCE_TYPE_CONSOLE);
        entity.setOperatorName(authName);
        entity.setOperatorId(authId);
        entity.setCreateOp(authName);
        entity.setRemark(remark);
        return entity;
    }

    private void saveWzCostLog(String orderNo, RenterWzCostDetailReqVO costDetail, String content) {
        WzCostLogEntity wzCostLogEntity = new WzCostLogEntity();
        wzCostLogEntity.setContent(content);
        wzCostLogEntity.setCreateTime(new Date());
        wzCostLogEntity.setOperator(AdminUserUtil.getAdminUser().getAuthName());
        wzCostLogEntity.setOrderNo(orderNo);
        wzCostLogEntity.setCostCode(costDetail.getCostCode());
        wzCostLogService.save(wzCostLogEntity);
    }

    private Map<String, String> getParamNamesByCode(String costCode) {
        if(StringUtils.isBlank(costCode)){
            return null;
        }
        Map<String, String> map = new LinkedHashMap<>();
        if(INSURANCE_CLAIM_CODE.equals(costCode)){
            map.put(AMOUNT,INSURANCE_CLAIM);
            map.put(REMARK,INSURANCE_CLAIM_REMARK);
        }
        if(WZ_OTHER_FINE_CODE.equals(costCode)){
            map.put(AMOUNT,WZ_OTHER_FINE);
            map.put(REMARK,WZ_OTHER_FINE_REMARK);
        }
        return map;
    }

    public WzCostLogsResVO queryWzCostLogsByOrderNo(String orderNo) {
        List<WzCostLogEntity> wzCostLogEntities = wzCostLogService.queryWzCostLogsByOrderNo(orderNo);
        List<WzCostLogResVO> wzCostLogs = new ArrayList<>();
        WzCostLogResVO vo;
        for (WzCostLogEntity wzCostLog : wzCostLogEntities) {
            vo = new WzCostLogResVO();
            BeanUtils.copyProperties(wzCostLog,vo);
            vo.setCostItem(WzCostEnums.getDesc(wzCostLog.getCostCode()));
            vo.setCreateTimeStr(DateUtils.formate(wzCostLog.getCreateTime(),DateUtils.DATE_DEFAUTE1));
            vo.setOperateContent(wzCostLog.getContent());
            wzCostLogs.add(vo);
        }
        WzCostLogsResVO wzCostLogsResVO = new WzCostLogsResVO();
        wzCostLogsResVO.setWzCostLogs(wzCostLogs);
        return wzCostLogsResVO;
    }

    private List<TemporaryRefundLogResVO> queryTemporaryRefundLogsByOrderNo(String orderNo) {
        List<WzTemporaryRefundLogEntity> wzTemporaryRefundLogEntities = wzTemporaryRefundLogService.queryTemporaryRefundLogsByOrderNo(orderNo);
        List<TemporaryRefundLogResVO> wzCostLogs = new ArrayList<>();
        TemporaryRefundLogResVO vo;
        for (WzTemporaryRefundLogEntity wzCostLog : wzTemporaryRefundLogEntities) {
            vo = new TemporaryRefundLogResVO();
            BeanUtils.copyProperties(wzCostLog,vo);
            vo.setCreateTimeStr(DateUtils.formate(wzCostLog.getCreateTime(),DateUtils.DATE_DEFAUTE1));
            vo.setAmount(String.valueOf(wzCostLog.getAmount()));
            wzCostLogs.add(vo);
        }
        return wzCostLogs;
    }

    public void addTemporaryRefund(TemporaryRefundReqVO req) {
        //TODO 调用退款接口
        WzTemporaryRefundLogEntity dto = new WzTemporaryRefundLogEntity();
        BeanUtils.copyProperties(req,dto);
        dto.setCreateTime(new Date());
        dto.setOperator(AdminUserUtil.getAdminUser().getAuthName());
        dto.setAmount(convertIntString(req.getAmount()));
        dto.setStatus(1);
        wzTemporaryRefundLogService.save(dto);
    }

    private int convertIntString(String intStr){
        if(org.apache.commons.lang.StringUtils.isBlank(intStr)){
            return 0;
        }
        //判断是否有小数点
        if(intStr.contains(RADIX_POINT)){
            String subStr= intStr.substring(0,(intStr.indexOf(RADIX_POINT)));
            return Integer.parseInt(subStr);
        }
        return Integer.parseInt(intStr);
    }

    public RenterWzDetailResVO queryWzDetailByOrderNo(String orderNo) {
        RenterWzDetailResVO rs = new RenterWzDetailResVO();
        rs.setOrderNo(orderNo);
        //违章结算 状态
        OrderStatusEntity orderStatus = orderStatusService.getByOrderNo(orderNo);
        if(orderStatus == null || orderStatus.getWzSettleStatus() == null){
            rs.setSettleStatus("0");
        }else {
            rs.setSettleStatus(String.valueOf(orderStatus.getWzSettleStatus()));
        }

        //费用详情
        List<RenterWzCostDetailResVO> costDetails = getRenterWzCostDetailRes(orderNo);
        rs.setCostDetails(costDetails);

        //暂扣日志
        List<TemporaryRefundLogResVO> temporaryRefundLogResVos = this.queryTemporaryRefundLogsByOrderNo(orderNo);
        rs.setTemporaryRefundLogs(temporaryRefundLogResVos);

        WzDepositMsgResVO wzDepositMsg = cashierQueryService.queryWzDepositMsg(orderNo);

        //违章押金暂扣处理
        RenterWzWithholdResVO withhold = this.queryRenterWzWithhold(orderNo,rs.getSettleStatus(),orderStatus,wzDepositMsg);
        rs.setWithhold(withhold);

        //违章支付信息
        RenterWzInfoResVO renterWzInfo = this.queryRenterWzInfoByOrderNo(wzDepositMsg);
        rs.setInfo(renterWzInfo);

        return rs;
    }

    private static final String UN_SETTLE = "0";
    private RenterWzWithholdResVO queryRenterWzWithhold(String orderNo, String settleStatus, OrderStatusEntity orderStatus, WzDepositMsgResVO wzDepositMsg) {
        RenterWzWithholdResVO result = new RenterWzWithholdResVO();
        RenterOrderEntity renterOrder = renterOrderService.getRenterOrderByOrderNoAndIsEffective(orderNo);
        if(renterOrder != null && renterOrder.getActRevertTime() != null){
            result.setExpectSettleTimeStr(DateUtils.formate(renterOrder.getActRevertTime().plusDays(18L),DateUtils.DATE_DEFAUTE1));
        }
        if(UN_SETTLE.equals(settleStatus)){
            //未结算
            result.setShouldReturnDeposit(String.valueOf(wzDepositMsg.getWzDepositSurplusAmt()));
            result.setProvisionalDeduction(String.valueOf(wzDepositMsg.getDetainAmt()));
            result.setYuJiDiKouZuCheFee(String.valueOf(wzDepositMsg.getDetainCostAmt()));
            result.setActuallyProvisionalDeduction("0");
        }else{
            //已结算
            result.setShiJiZanKouJinE(String.valueOf(wzDepositMsg.getDetainAmt()));
            result.setShiJiYiTuiWeiZhangYaJin(String.valueOf(wzDepositMsg.getRefundAmt()));
            result.setShiJiDiKouZuCheFee(String.valueOf(wzDepositMsg.getDetainCostAmt()));
            result.setJieSuanShiDiKouLiShiQianKuan(String.valueOf(wzDepositMsg.getDebtAmt()));
            result.setActuallyProvisionalDeduction(String.valueOf(wzDepositMsg.getDetainAmt()));
            if(orderStatus != null && orderStatus.getWzSettleTime() != null){
                result.setRealSettleTimeStr(DateUtils.formate(orderStatus.getWzSettleTime(),DateUtils.DATE_DEFAUTE1));
            }
        }
        if(StringUtils.isNotBlank(wzDepositMsg.getDeductionTime())){
            result.setDeductionTimeStr(wzDepositMsg.getDeductionTime());
            result.setDeductionStatusStr(wzDepositMsg.getDebtStatus());
        }
        return result;
    }

    private RenterWzInfoResVO queryRenterWzInfoByOrderNo(WzDepositMsgResVO wzDepositMsg) {
        RenterWzInfoResVO result = new RenterWzInfoResVO();
        if(wzDepositMsg == null){
            return result;
        }
        result.setYingshouDeposit(String.valueOf(wzDepositMsg.getYingshouWzDepositAmt()));
        result.setWzDeposit(String.valueOf(wzDepositMsg.getWzDepositAmt()));
        result.setWaiverAmount(String.valueOf(wzDepositMsg.getReductionAmt()));
        result.setTransStatusStr(wzDepositMsg.getPayStatus());
        result.setPayTimeStr(wzDepositMsg.getPayTime());
        result.setPaymentStr(wzDepositMsg.getPaySource());
        result.setFreeDepositTypeStr(wzDepositMsg.getPayType());
        return result;
    }

    private List<RenterWzCostDetailResVO> getRenterWzCostDetailRes(String orderNo) {
        List<RenterOrderWzCostDetailEntity> results = new ArrayList<>();
        for (String costCode : COST_CODE_LIST) {
            RenterOrderWzCostDetailEntity dto = renterOrderWzCostDetailService.queryInfoWithSumAmountByOrderAndCode(orderNo,costCode);
            if(dto == null){
                dto = new RenterOrderWzCostDetailEntity();
                dto.setAmount(0);
                dto.setCostCode(costCode);
                dto.setCostDesc(WzCostEnums.getDesc(costCode));
                dto.setOrderNo(orderNo);
            }
            results.add(dto);
        }
        List<RenterWzCostDetailResVO> costDetails = new ArrayList<>();
        RenterWzCostDetailResVO dto;
        for (RenterOrderWzCostDetailEntity costDetail : results) {
            dto = new RenterWzCostDetailResVO();
            BeanUtils.copyProperties(costDetail,dto);
            dto.setAmount(String.valueOf(costDetail.getAmount()));
            dto.setCostType(WzCostEnums.getType(costDetail.getCostCode()));
            dto.setRemarkName(WzCostEnums.getRemark(costDetail.getCostCode()));
            costDetails.add(dto);
        }
        return costDetails;
    }
}
