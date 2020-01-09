package com.atzuche.order.cashieraccount.service.notservice;

import com.atzuche.order.accountrenterdeposit.exception.AccountRenterCostException;
import com.atzuche.order.accountrenterdeposit.exception.AccountRenterDepositDBException;
import com.atzuche.order.accountrenterdeposit.service.AccountRenterDepositService;
import com.atzuche.order.accountrenterdeposit.vo.req.CreateOrderRenterCostReqVO;
import com.atzuche.order.accountrenterdeposit.vo.req.CreateOrderRenterDepositReqVO;
import com.atzuche.order.accountrenterdeposit.vo.req.DetainRenterDepositReqVO;
import com.atzuche.order.accountrenterdeposit.vo.req.PayedOrderRenterDepositReqVO;
import com.atzuche.order.accountrenterrentcost.service.AccountRenterCostSettleService;
import com.atzuche.order.accountrenterrentcost.vo.req.AccountRenterCostDetailReqVO;
import com.atzuche.order.accountrenterrentcost.vo.req.AccountRenterCostReqVO;
import com.atzuche.order.accountrenterwzdepost.exception.AccountRenterWZDepositException;
import com.atzuche.order.accountrenterwzdepost.service.AccountRenterWzDepositService;
import com.atzuche.order.accountrenterwzdepost.vo.req.CreateOrderRenterWZDepositReqVO;
import com.atzuche.order.accountrenterwzdepost.vo.req.PayedOrderRenterDepositWZDetailReqVO;
import com.atzuche.order.accountrenterwzdepost.vo.req.PayedOrderRenterWZDepositReqVO;

import com.atzuche.order.cashieraccount.common.AESUtil;
import com.atzuche.order.cashieraccount.common.FasterJsonUtil;
import com.atzuche.order.cashieraccount.entity.CashierEntity;
import com.atzuche.order.cashieraccount.entity.CashierRefundApplyEntity;
import com.atzuche.order.cashieraccount.exception.OrderPayCallBackAsnyException;
import com.atzuche.order.cashieraccount.exception.OrderPaySignParamException;
import com.atzuche.order.cashieraccount.mapper.CashierMapper;
import com.atzuche.order.cashieraccount.vo.req.pay.OrderPaySignReqVO;
import com.atzuche.order.cashieraccount.vo.res.OrderPayableAmountResVO;
import com.atzuche.order.commons.IpUtil;
import com.atzuche.order.commons.LocalDateTimeUtils;
import com.atzuche.order.commons.enums.RenterCashCodeEnum;
import com.atzuche.order.commons.enums.account.PayStatusEnum;
import com.atzuche.order.commons.enums.cashier.*;
import com.atzuche.order.rentercost.entity.vo.PayableVO;
import com.atzuche.order.renterorder.entity.RenterOrderEntity;
import com.atzuche.order.renterorder.service.RenterOrderService;
import com.autoyol.autopay.gateway.constant.*;
import com.autoyol.autopay.gateway.util.AESSecurityUtils;
import com.autoyol.autopay.gateway.util.MD5;
import com.autoyol.autopay.gateway.util.RSASecurityUtils;
import com.autoyol.autopay.gateway.vo.req.BatchPayVo;
import com.autoyol.autopay.gateway.vo.req.NotifyDataVo;
import com.autoyol.autopay.gateway.vo.req.PayVo;
import com.autoyol.autopay.gateway.vo.req.RefundVo;
import com.autoyol.commons.utils.GsonUtils;
import com.autoyol.commons.web.ErrorCode;
import com.autoyol.doc.util.StringUtil;
import com.autoyol.vo.req.WalletDeductionReqVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * 收银表
 *
 * @author ZhangBin
 * @date 2019-12-11 11:17:59
 */
@Service
@Slf4j
public class CashierNoTService {
    @Autowired
    private CashierMapper cashierMapper;
    @Autowired
    RenterOrderService renterOrderService;
    @Autowired
    AccountRenterDepositService accountRenterDepositService;
    @Autowired
    AccountRenterWzDepositService accountRenterWzDepositService;
    @Autowired
    AccountRenterCostSettleService accountRenterCostSettleService;
    @Value("${env_t}")
    private String env;


    /**
     * 收银台根据主单号 向订单模块查询子单号
     * @param orderNo
     * @return
     */
    public RenterOrderEntity getRenterOrderNoByOrderNo(String orderNo){
        RenterOrderEntity renterOrderEntity =  renterOrderService.getRenterOrderByOrderNoAndWaitPay(orderNo);
        if(Objects.isNull(renterOrderEntity) || StringUtil.isBlank(renterOrderEntity.getRenterOrderNo())){
            throw new AccountRenterWZDepositException();
        }
        return renterOrderEntity;
    }

    /**
     * 收银台记录应收金额
     */
    public int getPayDeposit(String orderNo,String memNo,String payKind){
        CashierEntity cashierEntity = cashierMapper.getPayAmtByPayKind(orderNo,memNo,payKind);
        if(Objects.isNull(cashierEntity) || Objects.isNull(cashierEntity.getId())){
            return 0;
        }
        if(PayStatusEnum.PAYED.getCode().equals(cashierEntity.getTransStatus())){
            return 0;
        }
        return cashierEntity.getPayAmt() ;
    }


    /**
     * 收银台支付记录
     */
    public CashierEntity getCashierEntity(String orderNo,String memNo,String payKind){
        CashierEntity cashierEntity = cashierMapper.getPayAmtByPayKind(orderNo,memNo,payKind);
        return cashierEntity;
    }

    /**
     * 收银台记录应收违章押金
     * @param createOrderRenterWZDepositReq
     */
    public void insertRenterWZDeposit(CreateOrderRenterWZDepositReqVO createOrderRenterWZDepositReq) {
        //1 校验
        Assert.notNull(createOrderRenterWZDepositReq, ErrorCode.PARAMETER_ERROR.getText());
        createOrderRenterWZDepositReq.check();

        CashierEntity cashierEntity = new CashierEntity();
        BeanUtils.copyProperties(createOrderRenterWZDepositReq,cashierEntity);
        cashierEntity.setPayAmt(createOrderRenterWZDepositReq.getYingfuDepositAmt());
        int result = cashierMapper.insert(cashierEntity);
        if(result==0){
            throw new AccountRenterWZDepositException();
        }
    }
    /**
     * 收银台记录应收车俩押金
     * @param createOrderRenterDeposit
     */
    public void insertRenterDeposit(CreateOrderRenterDepositReqVO createOrderRenterDeposit) {
        //1 校验
        Assert.notNull(createOrderRenterDeposit, ErrorCode.PARAMETER_ERROR.getText());
        createOrderRenterDeposit.check();
        CashierEntity cashierEntity = new CashierEntity();
        BeanUtils.copyProperties(createOrderRenterDeposit,cashierEntity);
        cashierEntity.setPayAmt(createOrderRenterDeposit.getYingfuDepositAmt());
        int result = cashierMapper.insert(cashierEntity);
        if(result==0){
            throw new AccountRenterDepositDBException();
        }
    }

    /**
     * 下单成功  收银台落 收银记录（此时没有应付 ，应付从租车费用中取）
     * @param createOrderRenterCost
     */
    public void insertRenterCost(CreateOrderRenterCostReqVO createOrderRenterCost) {
        //1 校验
        Assert.notNull(createOrderRenterCost, ErrorCode.PARAMETER_ERROR.getText());
        createOrderRenterCost.check();
        CashierEntity cashierEntity = new CashierEntity();
        BeanUtils.copyProperties(createOrderRenterCost,cashierEntity);
        cashierEntity.setPayAmt(NumberUtils.INTEGER_ZERO);
        int result = cashierMapper.insert(cashierEntity);
        if(result==0){
            throw new AccountRenterCostException();
        }
    }

    /**
     * 支付成功异步回调 车俩押金参数初始化
     * @param notifyDataVo
     * @return
     */
    public PayedOrderRenterDepositReqVO getPayedOrderRenterDepositReq(NotifyDataVo notifyDataVo) {
        PayedOrderRenterDepositReqVO vo = new PayedOrderRenterDepositReqVO();
        DetainRenterDepositReqVO detainRenterDeposit = new DetainRenterDepositReqVO();
        BeanUtils.copyProperties(notifyDataVo,vo);
        vo.setPayStatus(PayStatusEnum.PAYED.getCode());
        int transStatus = StringUtil.isBlank(notifyDataVo.getTransStatus())?0:Integer.parseInt(notifyDataVo.getTransStatus());
        vo.setPayStatus(transStatus);
        vo.setPayTime(LocalDateTimeUtils.parseStringToDateTime(notifyDataVo.getOrderTime(),LocalDateTimeUtils.DEFAULT_PATTERN));
        //"01"：消费
        if(DataPayTypeConstant.PAY_PUR.equals(notifyDataVo.getPayType())){
            Integer settleAmount = notifyDataVo.getSettleAmount()==null?0:Integer.parseInt(notifyDataVo.getSettleAmount());
            vo.setShifuDepositAmt(settleAmount);
            vo.setSurplusDepositAmt(settleAmount);
        }
        //"02"：预授权 TODO 预授权到期时间 （分为信用 和 芝麻）
        if(DataPayTypeConstant.PAY_PRE.equals(notifyDataVo.getPayType())){
            Integer settleAmount = notifyDataVo.getSettleAmount()==null?0:Integer.parseInt(notifyDataVo.getSettleAmount());
            vo.setAuthorizeDepositAmt(settleAmount);
            vo.setSurplusAuthorizeDepositAmt(settleAmount);
            detainRenterDeposit.setAuthorizeExpireTime(LocalDateTimeUtils.getDateAfter(vo.getPayTime(),30));
        }
        //TODO 预授权到期时间
        //车辆押金进出明细
        BeanUtils.copyProperties(notifyDataVo,detainRenterDeposit);
        Integer settleAmount = notifyDataVo.getSettleAmount()==null?0:Integer.parseInt(notifyDataVo.getSettleAmount());
        detainRenterDeposit.setAmt(settleAmount);
        detainRenterDeposit.setUniqueNo(notifyDataVo.getQn());
        detainRenterDeposit.setRenterCashCodeEnum(RenterCashCodeEnum.CASHIER_RENTER_DEPOSIT);
        vo.setDetainRenterDepositReqVO(detainRenterDeposit);
        return vo;
    }

    /**
     * 更新收银台租车押金已支付
     * @param notifyDataVo
     */
    public void updataCashier(NotifyDataVo notifyDataVo) {
        CashierEntity cashierEntity = cashierMapper.selectByPrimaryKey(Integer.parseInt(notifyDataVo.getPayId()));
        int result =0;
         if(Objects.nonNull(cashierEntity) && Objects.nonNull(cashierEntity.getId())){
             CashierEntity cashier = new CashierEntity();
             BeanUtils.copyProperties(notifyDataVo,cashier);
             cashier.setId(cashierEntity.getId());
             cashier.setVersion(cashierEntity.getVersion());
             cashier.setPaySn(cashierEntity.getPaySn()+1);
             result = cashierMapper.updateByPrimaryKeySelective(cashier);
         }else {
             CashierEntity cashier = new CashierEntity();
             BeanUtils.copyProperties(notifyDataVo,cashier);
             result = cashierMapper.insert(cashier);
         }
         if(result == 0){
            throw new OrderPayCallBackAsnyException();
         }
    }
    /**
     * 支付成功异步回调 违章押金参数初始化
     * @param notifyDataVo
     * @return
     */
    public PayedOrderRenterWZDepositReqVO getPayedOrderRenterWZDepositReq(NotifyDataVo notifyDataVo) {
        PayedOrderRenterWZDepositReqVO vo = new PayedOrderRenterWZDepositReqVO();
        BeanUtils.copyProperties(notifyDataVo,vo);
        Integer settleAmount = notifyDataVo.getSettleAmount()==null?0:Integer.parseInt(notifyDataVo.getSettleAmount());
        vo.setShishouDeposit(settleAmount);

        //违章押金进出明细
        PayedOrderRenterDepositWZDetailReqVO payedOrderRenterDepositDetail = new PayedOrderRenterDepositWZDetailReqVO();
        BeanUtils.copyProperties(notifyDataVo,payedOrderRenterDepositDetail);
        payedOrderRenterDepositDetail.setUniqueNo(notifyDataVo.getQn());
        payedOrderRenterDepositDetail.setRenterCashCodeEnum(RenterCashCodeEnum.CASHIER_RENTER_WZ_DEPOSIT);
        payedOrderRenterDepositDetail.setPayChannel(notifyDataVo.getPaySource());
        payedOrderRenterDepositDetail.setPayment(notifyDataVo.getPayType());
        payedOrderRenterDepositDetail.setAmt(settleAmount);
        vo.setPayedOrderRenterDepositDetailReqVO(payedOrderRenterDepositDetail);
        return vo;
    }

    /**
     * 支付成功异步回调 补付租车费用回调
     * @param notifyDataVo
     * @return
     */
    public AccountRenterCostReqVO getAccountRenterCostReq(NotifyDataVo notifyDataVo,RenterCashCodeEnum renterCashCodeEnum) {
        AccountRenterCostReqVO vo = new AccountRenterCostReqVO();
        BeanUtils.copyProperties(notifyDataVo,vo);
        Integer settleAmount = notifyDataVo.getSettleAmount()==null?0:Integer.parseInt(notifyDataVo.getSettleAmount());
        vo.setShifuAmt(settleAmount);
        //费用明细
        AccountRenterCostDetailReqVO accountRenterCostDetail = new AccountRenterCostDetailReqVO();
        BeanUtils.copyProperties(notifyDataVo,accountRenterCostDetail);
        accountRenterCostDetail.setPaySource(notifyDataVo.getPaySource());
        accountRenterCostDetail.setPayType(notifyDataVo.getPayType());
        accountRenterCostDetail.setTime(LocalDateTimeUtils.parseStringToDateTime(notifyDataVo.getOrderTime(),LocalDateTimeUtils.DEFAULT_PATTERN));
        accountRenterCostDetail.setAmt(settleAmount);
        accountRenterCostDetail.setRenterCashCodeEnum(renterCashCodeEnum);
        vo.setAccountRenterCostDetailReqVO(accountRenterCostDetail);
        return vo;
    }
    /**
     * 计算租车费用应付
     * @param payableVOs
     * @return
     */
    public int sumRentOrderCost(List<PayableVO> payableVOs) {
        int amt = NumberUtils.INTEGER_ZERO;
        if(!CollectionUtils.isEmpty(payableVOs)){
            return payableVOs.stream().mapToInt(PayableVO::getAmt).sum();
        }
        return amt;
    }

    /**
     * 构造远程扣减钱包参数
     */
    public WalletDeductionReqVO getWalletDeductionReqVO(OrderPaySignReqVO orderPaySign, OrderPayableAmountResVO payVO,int paySn) {
        WalletDeductionReqVO vo = new WalletDeductionReqVO();
        vo.setIp(IpUtil.getLocalIp());
        vo.setAmt(payVO.getAmtWallet());
        vo.setOrderNo(Long.parseLong(orderPaySign.getOrderNo()));
        vo.setMemNo(Integer.valueOf(orderPaySign.getMenNo()));
        vo.setFlag(WalletFlagEnum.RENTER_CONSUME.getCode());
        vo.setFlagTxt(WalletFlagEnum.RENTER_CONSUME.getText());
        vo.setServiceName("order-center");
        vo.setPlatform(PlatformEnum.OPERATION_TERMINAL.name());
        vo.setOperator(orderPaySign.getOperator());
        vo.setOperatorName(orderPaySign.getOperatorName());
        vo.setNum(paySn);
        return vo;
    }

    /**
     *  获取租车费用支付次数
     * @param orderPaySign
     * @return
     */
    public int payOrderByWallet(OrderPaySignReqVO orderPaySign) {
        //取出所有户头 钱包支付款项
        List<String> payKinds = orderPaySign.getPayKind();
        int num = 1;
        if (!CollectionUtils.isEmpty(payKinds)) {
            //1 钱包 优先抵扣  支付租车费用
            if (payKinds.contains(DataPayKindConstant.RENT_AMOUNT)) {
                CashierEntity cashierEntity = cashierMapper.getPayDetail(orderPaySign.getOrderNo(),orderPaySign.getMenNo(),DataPayKindConstant.RENT_AMOUNT,DataPayTypeConstant.PAY_PUR,"00");
                if(Objects.nonNull(cashierEntity) && Objects.nonNull(cashierEntity.getPaySn())){
                    num = cashierEntity.getPaySn() + 1;
                }
            }
        }
        return num;
    }

    /**
     * 构造参数 PayVo (押金、违章押金)
     * @param cashierEntity
     * @param orderPaySign
     * @return
     */
    public PayVo getPayVO(CashierEntity cashierEntity,OrderPaySignReqVO orderPaySign,int amt ,String title,String payKind) {
        PayVo vo = new PayVo();
        int paySn = Objects.isNull(cashierEntity.getPaySn())?0:cashierEntity.getPaySn();
        vo.setInternalNo(String.valueOf(paySn));
        vo.setExtendParams(GsonUtils.toJson(cashierEntity));
        vo.setAtappId(DataAppIdConstant.APPID_SHORTRENT);
        vo.setMemNo(orderPaySign.getMenNo());
        vo.setOrderNo(orderPaySign.getOrderNo());
        vo.setOpenId(orderPaySign.getOpenId());
        vo.setReqOs(orderPaySign.getReqOs());
        vo.setPayAmt(String.valueOf(Math.abs(amt)));
        vo.setPayEnv(env);
        int id = Objects.isNull(cashierEntity.getId())?0:cashierEntity.getId();
        vo.setPayId(String.valueOf(id));
        vo.setPayKind(payKind);
        vo.setPaySn(String.valueOf(paySn));
        vo.setPaySource(orderPaySign.getPaySource());
        vo.setPayTitle(title);
        vo.setPayType(orderPaySign.getPayType());
        vo.setReqIp(IpUtil.getLocalIp());
        vo.setAtpaySign(StringUtils.EMPTY);
        return vo;
    }



    /**
     * 租车押金 收银台回调
     * @param notifyDataVo
     * @param payedOrderRenterDeposit
     */
    public void updataCashierAndRenterDeposit(NotifyDataVo notifyDataVo, PayedOrderRenterDepositReqVO payedOrderRenterDeposit) {
        //1更新收银台
        updataCashier(notifyDataVo);
        //2 租车押金 更新数据
        accountRenterDepositService.updateRenterDeposit(payedOrderRenterDeposit);

    }
    /**
     * 违章押金 收银台回调
     * @param notifyDataVo
     * @param payedOrderRenterWZDeposit
     */
    public void updataCashierAndRenterWzDeposit(NotifyDataVo notifyDataVo, PayedOrderRenterWZDepositReqVO payedOrderRenterWZDeposit) {
        //1更新收银台
        updataCashier(notifyDataVo);
        //2 违章押金 更新数据
        accountRenterWzDepositService.updateRenterWZDeposit(payedOrderRenterWZDeposit);
    }

    /**
     * 租车费用/补付租车费用
     * @param notifyDataVo
     * @param accountRenterCostReq
     */
    public void updataCashierAndRenterCost(NotifyDataVo notifyDataVo,AccountRenterCostReqVO accountRenterCostReq) {
        //1更新收银台
        updataCashier(notifyDataVo);
        //2  实收租车费用落库 更新数据
        accountRenterCostSettleService.insertRenterCostDetail(accountRenterCostReq);
    }

    /**
     * 返回租车费用补付记录数
     * @param orderNo
     * @param menNo
     * @return
     */
    public String getCashierRentCostPaySn(String orderNo, String menNo) {
        List<CashierEntity> sashierEntitys = cashierMapper.getCashierRentCosts(orderNo,menNo,DataPayKindConstant.RENT_AMOUNT,DataPayTypeConstant.PAY_PUR);
        if(CollectionUtils.isEmpty(sashierEntitys)){
            return "1";
        }
        return String.valueOf(sashierEntitys.size()+1);
    }

    /**
     * 支付签名
     * @param payVos
     * @return
     * TODO 公私钥
     */
    public String getPaySignByPayVos(List<PayVo> payVos) {
        String signs = StringUtils.EMPTY;
        try {
            if(!CollectionUtils.isEmpty(payVos)){
                for(int i=0;i<payVos.size();i++){
                    PayVo payVo = payVos.get(i);
                    String reqContent = FasterJsonUtil.toJson(payVo);
                    //私钥加密
                    String sign = RSASecurityUtils.privateKeySignature(AESUtil.keySign,reqContent);
                    payVo.setAtpaySign(sign);
                }
            }
            BatchPayVo bpv = new BatchPayVo();
            bpv.setLstPayVo(payVos);
            String reqContents = FasterJsonUtil.toJson(bpv);
            //私钥加密
            signs = AESSecurityUtils.encrypt(AESUtil.keyAec, reqContents);
        } catch (Exception e) {
            log.error("getPaySignByPayVos 支付验签数据失败 ，param ;[{}],e:[{}]",FasterJsonUtil.toJson(payVos),e);
            throw new OrderPaySignParamException();
        }
        return signs;
    }

    /**
     * 钱包支付成功 收银台落库
     * @param orderPaySign
     * @param amtWallet
     */
    public void insertRenterCostByWallet(OrderPaySignReqVO orderPaySign, int amtWallet) {
       //1查询 收银台保存 钱包支付信息
        CashierEntity cashierEntity = cashierMapper.getPayDeposit(orderPaySign.getOrderNo(),orderPaySign.getMenNo(),DataPayKindConstant.RENT_AMOUNT,DataPayTypeConstant.PAY_PUR);
        int result = 0;
        int cashierId=0;
        RenterCashCodeEnum renterCashCodeEnum = RenterCashCodeEnum.ACCOUNT_RENTER_RENT_COST;
        if(Objects.nonNull(cashierEntity) && Objects.nonNull(cashierEntity.getPaySn())){
            CashierEntity cashier = new CashierEntity ();
            cashier.setId(cashierEntity.getId());
            cashier.setPaySn(cashierEntity.getPaySn() + 1);
            cashier.setPayAmt(cashier.getPayAmt() + amtWallet);
            cashier.setVersion(cashierEntity.getVersion());
            result = cashierMapper.updateByPrimaryKeySelective(cashier);
            cashierId = cashier.getId();
            renterCashCodeEnum = RenterCashCodeEnum.ACCOUNT_RENTER_RENT_COST_AGAIN;
        }else {
            CashierEntity cashier = new CashierEntity ();
            BeanUtils.copyProperties(orderPaySign,cashier);
            cashier.setPayAmt(amtWallet);
            cashier.setPaySource(PaySourceEnum.WALLET_PAY.getCode());
            cashier.setPayTitle("订单号：" + orderPaySign.getOrderNo() + "钱包支付金额：" + amtWallet + "元");
            cashier.setPayKind(DataPayKindConstant.RENT_AMOUNT);
            cashier.setPayType(DataPayTypeConstant.PAY_PUR);
            cashier.setAtappId(DataAppIdConstant.APPID_SHORTRENT);
            cashier.setTransStatus("00");
            cashier.setPaySn(NumberUtils.INTEGER_ONE);
            result = cashierMapper.insert(cashier);
            cashierId = cashier.getId();
        }
        if(result ==0){
            throw new AccountRenterDepositDBException();
        }

        //2 构造参数  记录个人 租车费用户头 记录 钱包支付信息
        AccountRenterCostReqVO accountRenterCostReq = new AccountRenterCostReqVO();
        BeanUtils.copyProperties(orderPaySign,accountRenterCostReq);
        accountRenterCostReq.setShifuAmt(Math.abs(amtWallet));
        AccountRenterCostDetailReqVO accountRenterCostDetailReq = new AccountRenterCostDetailReqVO();
        BeanUtils.copyProperties(orderPaySign,accountRenterCostDetailReq);
        accountRenterCostDetailReq.setUniqueNo(String.valueOf(cashierId));
        accountRenterCostDetailReq.setAmt(Math.abs(amtWallet));
        accountRenterCostDetailReq.setTransTime(LocalDateTime.now());
        accountRenterCostDetailReq.setRenterCashCodeEnum(renterCashCodeEnum);
        accountRenterCostDetailReq.setPaySource(PaySourceEnum.WALLET_PAY.getText());
        accountRenterCostDetailReq.setPaySourceCode(PaySourceEnum.WALLET_PAY.getCode());
        accountRenterCostDetailReq.setPayTypeCode(orderPaySign.getPayType());
        accountRenterCostDetailReq.setPayType(PayTypeEnum.getFlagText(orderPaySign.getPayType()));
        accountRenterCostReq.setAccountRenterCostDetailReqVO(accountRenterCostDetailReq);
        accountRenterCostSettleService.insertRenterCostDetail(accountRenterCostReq);
    }

    /**
     * 退款请求参数构造
     * @param cashierRefundApply
     * @return
     */
    public RefundVo getRefundVo(CashierRefundApplyEntity cashierRefundApply) {
        RefundVo refundVo = new RefundVo();
        BeanUtils.copyProperties(cashierRefundApply,refundVo);
        refundVo.setPayType(DataPayTypeConstant.PUR_RETURN);
        refundVo.setReqIp(IpUtil.getLocalIp());
        refundVo.setPaySn(String.valueOf(cashierRefundApply.getNum()+1));
        refundVo.setExtendParams(GsonUtils.toJson(cashierRefundApply));
        refundVo.setAtpaySign(StringUtils.EMPTY);
        String payMd5 =  MD5.MD5Encode(FasterJsonUtil.toJson(refundVo));
        refundVo.setPayMd5(payMd5);
        String reqContent = FasterJsonUtil.toJson(refundVo);
        //TODO 签名串
        String sign = StringUtils.EMPTY;
        try {
            sign = RSASecurityUtils.privateKeySignature(PublicKeySignConstants.AUTO_TRANS_PUBLIC_KEY,reqContent);
        } catch (Exception e) {
            log.error("refundOrderPay 支付验签数据失败 ，param ;[{}],e:[{}]",FasterJsonUtil.toJson(refundVo),e);
            throw new OrderPaySignParamException();
        }
        refundVo.setAtpaySign(sign);
        return refundVo;
    }


}
