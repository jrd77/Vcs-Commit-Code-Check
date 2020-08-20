package com.atzuche.order.settle.service.notservice;

import com.atzuche.order.accountplatorm.entity.AccountPlatformProfitDetailEntity;
import com.atzuche.order.accountplatorm.entity.AccountPlatformProfitEntity;
import com.atzuche.order.accountplatorm.entity.AccountPlatformSubsidyDetailEntity;
import com.atzuche.order.accountrenterdeposit.vo.req.OrderCancelRenterDepositReqVO;
import com.atzuche.order.accountrenterrentcost.entity.AccountRenterCostDetailEntity;
import com.atzuche.order.accountrenterrentcost.entity.AccountRenterCostSettleDetailEntity;
import com.atzuche.order.accountrenterrentcost.entity.AccountRenterCostSettleEntity;
import com.atzuche.order.accountrenterrentcost.vo.req.AccountRenterCostDetailReqVO;
import com.atzuche.order.accountrenterrentcost.vo.req.AccountRenterCostToFineReqVO;
import com.atzuche.order.accountrenterwzdepost.vo.req.RenterCancelWZDepositCostReqVO;
import com.atzuche.order.cashieraccount.entity.CashierEntity;
import com.atzuche.order.cashieraccount.service.CashierService;
import com.atzuche.order.cashieraccount.service.CashierSettleService;
import com.atzuche.order.cashieraccount.service.CashierWzSettleService;
import com.atzuche.order.cashieraccount.service.notservice.CashierNoTService;
import com.atzuche.order.cashieraccount.vo.req.CashierDeductDebtReqVO;
import com.atzuche.order.cashieraccount.vo.req.CashierRefundApplyReqVO;
import com.atzuche.order.cashieraccount.vo.res.CashierDeductDebtResVO;
import com.atzuche.order.coin.service.AccountRenterCostCoinService;
import com.atzuche.order.commons.PlatformProfitStatusEnum;
import com.atzuche.order.commons.constant.OrderConstant;
import com.atzuche.order.commons.entity.dto.CostBaseDTO;
import com.atzuche.order.commons.entity.dto.MileageAmtDTO;
import com.atzuche.order.commons.entity.dto.OilAmtDTO;
import com.atzuche.order.commons.entity.dto.RenterGoodsDetailDTO;
import com.atzuche.order.commons.enums.OrderStatusEnum;
import com.atzuche.order.commons.enums.SubsidySourceCodeEnum;
import com.atzuche.order.commons.enums.account.CostTypeEnum;
import com.atzuche.order.commons.enums.account.SettleStatusEnum;
import com.atzuche.order.commons.enums.account.debt.DebtTypeEnum;
import com.atzuche.order.commons.enums.account.income.AccountOwnerIncomeExamineStatus;
import com.atzuche.order.commons.enums.account.income.AccountOwnerIncomeExamineType;
import com.atzuche.order.commons.enums.cashcode.RenterCashCodeEnum;
import com.atzuche.order.commons.enums.cashier.OrderRefundStatusEnum;
import com.atzuche.order.commons.enums.cashier.PayLineEnum;
import com.atzuche.order.commons.enums.cashier.PaySourceEnum;
import com.atzuche.order.commons.enums.cashier.PayTypeEnum;
import com.atzuche.order.commons.service.OrderPayCallBack;
import com.atzuche.order.commons.vo.req.handover.rep.HandoverCarRespVO;
import com.atzuche.order.commons.vo.req.income.AccountOwnerIncomeExamineReqVO;
import com.atzuche.order.delivery.entity.RenterHandoverCarInfoEntity;
import com.atzuche.order.delivery.enums.RenterHandoverCarTypeEnum;
import com.atzuche.order.delivery.service.delivery.DeliveryCarInfoPriceService;
import com.atzuche.order.delivery.service.handover.HandoverCarService;
import com.atzuche.order.delivery.vo.delivery.DeliveryOilCostVO;
import com.atzuche.order.delivery.vo.delivery.rep.RenterGetAndReturnCarDTO;
import com.atzuche.order.delivery.vo.handover.HandoverCarRepVO;
import com.atzuche.order.flow.service.OrderFlowService;
import com.atzuche.order.ownercost.entity.ConsoleOwnerOrderFineDeatailEntity;
import com.atzuche.order.ownercost.entity.OwnerOrderEntity;
import com.atzuche.order.ownercost.entity.OwnerOrderFineDeatailEntity;
import com.atzuche.order.ownercost.service.ConsoleOwnerOrderFineDeatailService;
import com.atzuche.order.ownercost.service.OwnerOrderFineDeatailService;
import com.atzuche.order.ownercost.service.OwnerOrderService;
import com.atzuche.order.parentorder.dto.OrderStatusDTO;
import com.atzuche.order.parentorder.entity.OrderEntity;
import com.atzuche.order.parentorder.entity.OrderStatusEntity;
import com.atzuche.order.parentorder.service.OrderService;
import com.atzuche.order.parentorder.service.OrderStatusService;
import com.atzuche.order.rentercommodity.service.RenterGoodsService;
import com.atzuche.order.rentercost.entity.*;
import com.atzuche.order.rentercost.service.*;
import com.atzuche.order.rentermem.service.RenterMemberService;
import com.atzuche.order.renterorder.entity.RenterOrderEntity;
import com.atzuche.order.renterorder.service.OrderCouponService;
import com.atzuche.order.renterorder.service.RenterOrderService;
import com.atzuche.order.settle.dto.OrderSettleCommonParamDTO;
import com.atzuche.order.settle.dto.OrderSettleCommonResultDTO;
import com.atzuche.order.settle.exception.OrderSettleFlatAccountException;
import com.atzuche.order.settle.service.OrderSettleHandleService;
import com.atzuche.order.settle.service.OrderSettleNewService;
import com.atzuche.order.settle.service.OrderSettleRefundHandleService;
import com.atzuche.order.settle.service.OrderSettleWalletRefundHandleService;
import com.atzuche.order.settle.vo.req.*;
import com.atzuche.order.settle.vo.res.OrderSettleResVO;
import com.atzuche.order.wallet.WalletProxyService;
import com.autoyol.autopay.gateway.constant.DataPayKindConstant;
import com.autoyol.autopay.gateway.constant.DataPayTypeConstant;
import com.autoyol.commons.utils.GsonUtils;
import com.autoyol.doc.util.StringUtil;
import com.autoyol.platformcost.model.FeeResult;
import com.dianping.cat.Cat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
//
///**
// * 订单结算
// */
@Service
@Slf4j
public class OrderSettleNoTService {
    @Autowired
    private OrderSettleProxyService orderSettleProxyService;

    @Autowired CashierNoTService cashierNoTService;
    @Autowired private CashierService cashierService;
    @Autowired private CashierSettleService cashierSettleService;
    @Autowired private CashierWzSettleService cashierWzSettleService;
    @Autowired private RenterOrderCostDetailService renterOrderCostDetailService;
    @Autowired private RenterOrderSubsidyDetailService renterOrderSubsidyDetailService;
    @Autowired private RenterOrderFineDeatailService renterOrderFineDeatailService;
    @Autowired private OrderConsoleSubsidyDetailService orderConsoleSubsidyDetailService;
    @Autowired private ConsoleRenterOrderFineDeatailService consoleRenterOrderFineDeatailService;
    @Autowired private ConsoleOwnerOrderFineDeatailService consoleOwnerOrderFineDeatailService;
    @Autowired private HandoverCarService handoverCarService;
    @Autowired private RenterOrderService renterOrderService;
    @Autowired private OwnerOrderService ownerOrderService;
    @Autowired private OrderStatusService orderStatusService;
    @Autowired private RenterGoodsService renterGoodsService;
    @Autowired private AccountRenterCostCoinService accountRenterCostCoinService;
    @Autowired private WalletProxyService walletProxyService;
    @Autowired private OrderFlowService orderFlowService;
    @Autowired private OrderService orderService;
    @Autowired private OwnerOrderFineDeatailService ownerOrderFineDeatailService;
    @Autowired private OrderCouponService orderCouponService;
    @Autowired private OrderSettleNewService orderSettleNewService;
    @Autowired private DeliveryCarInfoPriceService deliveryCarInfoPriceService;
    @Autowired private OrderConsoleCostDetailService orderConsoleCostDetailService;
    @Autowired
    private RenterMemberService renterMemberService;
    @Autowired
    private OrderSettleHandleService orderSettleHandleService;
    @Autowired
    private OrderSettleRefundHandleService orderSettleRefundHandleService;


    // 租车费用
    private static final String RENT_COST_PAY_KIND = "11";
    // 车辆押金
    private static final String RENT_DEPOSIT_PAY_KIND = "01";
    // 虚拟支付
    private static final int VIRTUAL_PAYMENT = 2;

    /**
     * 初始化结算对象
     * @param orderNo
     */
    public SettleOrders initSettleOrders(String orderNo,SettleOrders settleOrders) {
        //1 校验参数
        if(StringUtil.isBlank(orderNo)){
            throw new OrderSettleFlatAccountException();
        }
        RenterOrderEntity renterOrder = renterOrderService.getRenterOrderByOrderNoAndIsEffective(orderNo);
        if(Objects.isNull(renterOrder) || Objects.isNull(renterOrder.getRenterOrderNo())){
            throw new OrderSettleFlatAccountException();
        }
        //是否企业级用户订单
        settleOrders.setIsEnterpriseUserOrder(renterMemberService.isEnterpriseUserOrder(renterOrder.getRenterOrderNo()));

        // 3 初始化数据
        // 3.1获取租客子订单 和 租客会员号
        String renterOrderNo = renterOrder.getRenterOrderNo();
        String renterMemNo = renterOrder.getRenterMemNo();
        // 3.2获取车主子订单 和 车主会员号
        settleOrders.setOrderNo(orderNo);
        settleOrders.setRenterOrderNo(renterOrderNo);
        settleOrders.setRenterMemNo(renterMemNo);
        settleOrders.setRenterOrder(renterOrder);
        // 获取租客支付记录
        List<CashierEntity> payList = cashierService.getCashierRentCostsByOrderNo(orderNo);
        if (payList != null && !payList.isEmpty()) {
            for (CashierEntity cashier:payList) {
                if (RENT_COST_PAY_KIND.equals(cashier.getPayKind()) && cashier.getPayLine() == VIRTUAL_PAYMENT) {
                    settleOrders.setRentCostVirtualPayFlag(true);
                } else if (RENT_DEPOSIT_PAY_KIND.equals(cashier.getPayKind()) && cashier.getPayLine() == VIRTUAL_PAYMENT) {
                    settleOrders.setRentDepositVirtualPayFlag(true);
                }
            }
        }
        // 2 校验订单状态 以及是否存在 理赔暂扣 存在不能进行结算 并CAT告警
        return settleOrders;
    }

    public boolean check(SettleOrders settleOrders) {
        return check(settleOrders, null);
    }
    /**
     * 校验是否可以结算 校验订单状态 以及是否存在 理赔暂扣 存在不能进行结算 并CAT告警
     * @param settleOrders
     */
    public boolean check(SettleOrders settleOrders,List<String> listOrderNos) {
        RenterOrderEntity renterOrder = settleOrders.getRenterOrder();

        //总开关
        // 1 订单校验是否可以结算
        OrderStatusEntity orderStatus = orderStatusService.getByOrderNo(renterOrder.getOrderNo());
        if(OrderStatusEnum.TO_SETTLE.getStatus() != orderStatus.getStatus()
                || SettleStatusEnum.SETTLEING.getCode() != orderStatus.getSettleStatus()
                || SettleStatusEnum.SETTLEING.getCode() != orderStatus.getCarDepositSettleStatus() ){
        	log.error("租客订单状态不是待结算，不能结算，orderNo=[{}]",renterOrder.getOrderNo());
            throw new RuntimeException("租客订单状态不是待结算，不能结算");
        }

        //判断取送车里程数是否填写，只有都填写了，才正常结算，否则不结算。
        boolean flagMileage = orderSettleProxyService.checkMileageData(settleOrders.getOrderNo(),listOrderNos);
        if(!flagMileage) {
            throw new RuntimeException("租客取车或还车里程数或油表刻度不完整不能结算");
        }
        //3 校验是否存在 理赔  存在不结算
        if(null != orderStatus.getIsClaims() && orderStatus.getIsClaims() == OrderConstant.YES){
            throw new RuntimeException("租客存在理赔信息不能结算");
        }
        //3 是否存在 暂扣存在不结算
        if (null != orderStatus.getIsDetain() && orderStatus.getIsDetain() == OrderConstant.YES) {
            throw new RuntimeException("租客存在暂扣信息不能结算");
        }
        //4 先查询  发现 有结算数据停止结算 手动处理
        orderSettleNewService.checkIsSettle(renterOrder.getOrderNo(),settleOrders);

        return true;
    }

    /**
     * 车辆结算  校验费用落库等无实物操作
     * @param settleOrders
     * @param settleOrdersDefinition
     */
    public void settleOrderFirst(SettleOrders settleOrders, SettleOrdersDefinition settleOrdersDefinition){
        //1 查询所有租客费用明细
        this.getRenterCostSettleDetail(settleOrders);
        log.info("OrderSettleService getRenterCostSettleDetail settleOrders [{}]", GsonUtils.toJson(settleOrders));
        Cat.logEvent("settleOrders",GsonUtils.toJson(settleOrders));

        //4 计算费用统计  资金统计
        this.settleOrdersDefinition(settleOrders,settleOrdersDefinition);
        log.info("OrderSettleService settleOrdersDefinition settleOrdersDefinition [{}]", GsonUtils.toJson(settleOrdersDefinition));
        Cat.logEvent("SettleOrdersDefinition",GsonUtils.toJson(settleOrdersDefinition));

        //5 费用明细先落库
        this.insertSettleOrders(settleOrdersDefinition);
    }

    /**
     * 查询租客费用明细
     * @param settleOrders
     */
    public void getRenterCostSettleDetail(SettleOrders settleOrders) {
        RentCosts rentCosts = new RentCosts();
        //1  初始化
        //更新：按主订单号查询。
        HandoverCarRespVO handoverCarRep = handoverCarService.getHandoverCarInfoByOrderNo(settleOrders.getOrderNo());

        // 1.2 油费、超里程费用 订单商品需要的参数
        RenterGoodsDetailDTO renterGoodsDetail = renterGoodsService.getRenterGoodsDetail(settleOrders.getRenterOrderNo(),Boolean.TRUE);

        //1 查询租车费用
        List<RenterOrderCostDetailEntity> renterOrderCostDetails = renterOrderCostDetailService.listRenterOrderCostDetail(settleOrders.getOrderNo(),settleOrders.getRenterOrderNo());
        //2 交接车-油费
        DeliveryOilCostVO deliveryOilCostVO = deliveryCarInfoPriceService.getOilCostByRenterOrderNo(settleOrders.getOrderNo(),renterGoodsDetail.getCarEngineType());
        RenterGetAndReturnCarDTO renterGetAndReturnCarDTO = Objects.isNull(deliveryOilCostVO)?null:deliveryOilCostVO.getRenterGetAndReturnCarDTO();

        //3 交接车-获取超里程费用
        MileageAmtDTO mileageAmtDTO = orderSettleProxyService.getMileageAmtDTO(settleOrders,settleOrders.getRenterOrder(),handoverCarRep,renterGoodsDetail);
        FeeResult feeResult = deliveryCarInfoPriceService.getMileageAmtEntity(mileageAmtDTO);

        //4 补贴
        List<RenterOrderSubsidyDetailEntity> renterOrderSubsidyDetails = renterOrderSubsidyDetailService.listRenterOrderSubsidyDetail(settleOrders.getOrderNo(),settleOrders.getRenterOrderNo());
        //5 租客罚金
        List<RenterOrderFineDeatailEntity> renterOrderFineDeatails = renterOrderFineDeatailService.listRenterOrderFineDeatail(settleOrders.getOrderNo(),settleOrders.getRenterOrderNo());
        //6 管理后台补贴 （租客车主共用表 ，会员号区分车主/租客）
        List<OrderConsoleSubsidyDetailEntity> orderConsoleSubsidyDetails = orderConsoleSubsidyDetailService.listOrderConsoleSubsidyDetailByOrderNoAndMemNo(settleOrders.getOrderNo(),settleOrders.getRenterMemNo());
        //7 获取全局的租客订单罚金明细（租客车主共用表 ，会员号区分车主/租客）
        List<ConsoleRenterOrderFineDeatailEntity> consoleRenterOrderFineDeatails = consoleRenterOrderFineDeatailService.listConsoleRenterOrderFineDeatail(settleOrders.getOrderNo(),settleOrders.getRenterMemNo());

        //8后台管理操作费用表（无条件补贴）
        List<OrderConsoleCostDetailEntity> orderConsoleCostDetailEntity = orderConsoleCostDetailService.selectByOrderNoAndMemNo(settleOrders.getOrderNo(),settleOrders.getRenterMemNo());

        rentCosts.setOilAmt(renterGetAndReturnCarDTO);
        rentCosts.setMileageAmt(feeResult);

        rentCosts.setRenterOrderCostDetails(renterOrderCostDetails);
        rentCosts.setOrderConsoleCostDetailEntity(orderConsoleCostDetailEntity);

        rentCosts.setRenterOrderFineDeatails(renterOrderFineDeatails);
        rentCosts.setConsoleRenterOrderFineDeatails(consoleRenterOrderFineDeatails);

        rentCosts.setRenterOrderSubsidyDetails(renterOrderSubsidyDetails);
        rentCosts.setOrderConsoleSubsidyDetails(orderConsoleSubsidyDetails);

//        settleOrders.setRenterOrderCost(renterOrderCost);
        settleOrders.setRentCosts(rentCosts);
    }

    /**
     * 计算费用统计
     * @param settleOrders
     * @param settleOrdersDefinition
     * @return
     */
    public void settleOrdersDefinition(SettleOrders settleOrders, SettleOrdersDefinition settleOrdersDefinition) {
        //1统计 租客结算费用明细， 补贴，费用总额
        handleRentAndPlatform(settleOrdersDefinition,settleOrders);
        //3统计 计算总费用
        countCost(settleOrdersDefinition,settleOrders);
    }

    /**
     *  1统计 租客结算费用明细， 补贴，费用总额
     * @param settleOrdersDefinition
     * @param settleOrders
     */
    public void handleRentAndPlatform(SettleOrdersDefinition settleOrdersDefinition, SettleOrders settleOrders) {
        //1 租客费用明细 整理
        RentCosts rentCosts = settleOrders.getRentCosts();
        List<AccountRenterCostSettleDetailEntity> accountRenterCostSettleDetails = settleOrdersDefinition.getAccountRenterCostSettleDetails();
        accountRenterCostSettleDetails = CollectionUtils.isEmpty(accountRenterCostSettleDetails)?new ArrayList<>():accountRenterCostSettleDetails;

        if(Objects.nonNull(rentCosts)){
            //1.1 查询租车费用
            List<RenterOrderCostDetailEntity> renterOrderCostDetails = rentCosts.getRenterOrderCostDetails();
            if(!CollectionUtils.isEmpty(renterOrderCostDetails)){
                for(int i=0; i<renterOrderCostDetails.size();i++){
                    RenterOrderCostDetailEntity renterOrderCostDetail = renterOrderCostDetails.get(i);
                    AccountRenterCostSettleDetailEntity accountRenterCostSettleDetail = new AccountRenterCostSettleDetailEntity();
                    BeanUtils.copyProperties(renterOrderCostDetail,accountRenterCostSettleDetail);
                    accountRenterCostSettleDetail.setCostCode(renterOrderCostDetail.getCostCode());
                    accountRenterCostSettleDetail.setCostDetail(renterOrderCostDetail.getCostDesc());
                    accountRenterCostSettleDetail.setUniqueNo(String.valueOf(renterOrderCostDetail.getId()));
                    accountRenterCostSettleDetail.setAmt(renterOrderCostDetail.getTotalAmount());
                    accountRenterCostSettleDetail.setId(null);
                    accountRenterCostSettleDetails.add(accountRenterCostSettleDetail);
                    // 租车费用
                    orderSettleNewService.addRentCostToPlatformAndOwner(renterOrderCostDetail,settleOrdersDefinition);
                }
            }
            //1.2 交接车-油费
            RenterGetAndReturnCarDTO oilAmt = rentCosts.getOilAmt();
            if(Objects.nonNull(oilAmt) && !StringUtil.isBlank(oilAmt.getOilDifferenceCrash())){
                AccountRenterCostSettleDetailEntity accountRenterCostSettleDetail = new AccountRenterCostSettleDetailEntity();
                BeanUtils.copyProperties(oilAmt,accountRenterCostSettleDetail);
                accountRenterCostSettleDetail.setCostCode(RenterCashCodeEnum.ACCOUNT_RENTER_DELIVERY_OIL_COST.getCashNo());
                accountRenterCostSettleDetail.setCostDetail(RenterCashCodeEnum.ACCOUNT_RENTER_DELIVERY_OIL_COST.getTxt());
                //油量差价
                String oilDifferenceCrash = oilAmt.getOilDifferenceCrash();
                oilDifferenceCrash = StringUtil.isBlank(oilDifferenceCrash)?"0":oilDifferenceCrash;
                // 兼容小数 小数部分舍弃 例如1.9 =》1
                accountRenterCostSettleDetail.setAmt(Double.valueOf(oilDifferenceCrash).intValue());
                accountRenterCostSettleDetail.setMemNo(settleOrders.getRenterMemNo());
                accountRenterCostSettleDetail.setOrderNo(settleOrders.getOrderNo());
                accountRenterCostSettleDetail.setRenterOrderNo(settleOrders.getRenterOrderNo());
                accountRenterCostSettleDetails.add(accountRenterCostSettleDetail);
            }
            //1.3 交接车-获取超里程费用
            FeeResult mileageAmt = rentCosts.getMileageAmt();
            if(Objects.nonNull(mileageAmt) && Objects.nonNull(mileageAmt.getTotalFee())){
                AccountRenterCostSettleDetailEntity accountRenterCostSettleDetail = new AccountRenterCostSettleDetailEntity();
                BeanUtils.copyProperties(mileageAmt,accountRenterCostSettleDetail);
                accountRenterCostSettleDetail.setCostCode(RenterCashCodeEnum.ACCOUNT_RENTER_DELIVERY_MILEAGE_COST.getCashNo());
                accountRenterCostSettleDetail.setCostDetail(RenterCashCodeEnum.ACCOUNT_RENTER_DELIVERY_MILEAGE_COST.getTxt());
                //bugfix:200415 租客超里程费用取负数
                accountRenterCostSettleDetail.setAmt(-Math.abs(mileageAmt.getTotalFee()));
                accountRenterCostSettleDetail.setOrderNo(settleOrders.getOrderNo());
                accountRenterCostSettleDetail.setRenterOrderNo(settleOrders.getRenterOrderNo());
                accountRenterCostSettleDetail.setMemNo(settleOrders.getRenterMemNo());
                accountRenterCostSettleDetails.add(accountRenterCostSettleDetail);
                //算平台收益
                //在车主端处理，是根据是否代管车来处理，是代管车归平台，否则归车主。200308
//                orderSettleNewService.addRenterGetAndReturnCarAmtToPlatform(accountRenterCostSettleDetail,settleOrdersDefinition);
            }

            // ----------------------------------------------- 5大表 -----------------------------------------------
            //1.4租客罚金
            List<RenterOrderFineDeatailEntity> renterOrderFineDeatails = rentCosts.getRenterOrderFineDeatails();
            if(!CollectionUtils.isEmpty(renterOrderFineDeatails)) {
                for (int i = 0; i < renterOrderFineDeatails.size(); i++) {
                    RenterOrderFineDeatailEntity renterOrderFineDetailEntity = renterOrderFineDeatails.get(i);
                    AccountRenterCostSettleDetailEntity accountRenterCostSettleDetail = new AccountRenterCostSettleDetailEntity();
                    BeanUtils.copyProperties(renterOrderFineDetailEntity,accountRenterCostSettleDetail);
                    accountRenterCostSettleDetail.setCostCode(String.valueOf(renterOrderFineDetailEntity.getFineType()));
                    accountRenterCostSettleDetail.setCostDetail(renterOrderFineDetailEntity.getFineTypeDesc());
                    accountRenterCostSettleDetail.setUniqueNo(String.valueOf(renterOrderFineDetailEntity.getId()));
                    accountRenterCostSettleDetail.setAmt(renterOrderFineDetailEntity.getFineAmount());
                    CostTypeEnum costTypeEnum = orderSettleProxyService.getCostTypeEnumByFine(renterOrderFineDetailEntity.getFineSubsidySourceCode());
                    accountRenterCostSettleDetail.setType(costTypeEnum.getCode());
                    accountRenterCostSettleDetails.add(accountRenterCostSettleDetail);
                    //罚金来源方 是平台
                    int fineAmount = renterOrderFineDetailEntity.getFineAmount();
                    //罚金补贴方 是平台
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(renterOrderFineDetailEntity.getFineSubsidyCode())){
                        AccountPlatformProfitDetailEntity entity = new AccountPlatformProfitDetailEntity();
                        BeanUtils.copyProperties(renterOrderFineDetailEntity,entity);
                        entity.setSourceCode(String.valueOf(renterOrderFineDetailEntity.getFineType()));
                        entity.setSourceDesc(renterOrderFineDetailEntity.getFineTypeDesc());
                        entity.setUniqueNo(String.valueOf(renterOrderFineDetailEntity.getId()));
                        entity.setAmt(-fineAmount);
                        settleOrdersDefinition.addPlatformProfit(entity);
                    }

                    //罚金补贴方 是平台
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(renterOrderFineDetailEntity.getFineSubsidySourceCode())){
                        AccountPlatformProfitDetailEntity entity = new AccountPlatformProfitDetailEntity();
                        BeanUtils.copyProperties(renterOrderFineDetailEntity,entity);
                        entity.setSourceCode(String.valueOf(renterOrderFineDetailEntity.getFineType()));
                        entity.setSourceDesc(renterOrderFineDetailEntity.getFineTypeDesc());
                        entity.setUniqueNo(String.valueOf(renterOrderFineDetailEntity.getId()));
                        entity.setAmt(-fineAmount);
                        settleOrdersDefinition.addPlatformProfit(entity);
                    }
                }
            }

            //1.5 获取全局的租客订单罚金明细
            List<ConsoleRenterOrderFineDeatailEntity> consoleRenterOrderFineDeatails = rentCosts.getConsoleRenterOrderFineDeatails();
            if(!CollectionUtils.isEmpty(consoleRenterOrderFineDeatails)) {
                for (int i = 0; i < consoleRenterOrderFineDeatails.size(); i++) {
                    ConsoleRenterOrderFineDeatailEntity consoleRenterOrderFineDetailEntity = consoleRenterOrderFineDeatails.get(i);
                    AccountRenterCostSettleDetailEntity accountRenterCostSettleDetail = new AccountRenterCostSettleDetailEntity();
                    BeanUtils.copyProperties(consoleRenterOrderFineDetailEntity,accountRenterCostSettleDetail);
                    accountRenterCostSettleDetail.setCostCode(String.valueOf(consoleRenterOrderFineDetailEntity.getFineType()));
                    accountRenterCostSettleDetail.setCostDetail(consoleRenterOrderFineDetailEntity.getFineTypeDesc());
                    accountRenterCostSettleDetail.setUniqueNo(String.valueOf(consoleRenterOrderFineDetailEntity.getId()));
                    accountRenterCostSettleDetail.setAmt(consoleRenterOrderFineDetailEntity.getFineAmount());
                    CostTypeEnum costTypeEnum = orderSettleProxyService.getCostTypeEnumByFine(consoleRenterOrderFineDetailEntity.getFineSubsidySourceCode());
                    accountRenterCostSettleDetail.setType(costTypeEnum.getCode());
                    accountRenterCostSettleDetails.add(accountRenterCostSettleDetail);

                    //罚金来源方 是平台
                    int fineAmount = consoleRenterOrderFineDetailEntity.getFineAmount();
                    //罚金补贴方 是平台
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(consoleRenterOrderFineDetailEntity.getFineSubsidyCode())){
                        AccountPlatformProfitDetailEntity entity = new AccountPlatformProfitDetailEntity();
                        BeanUtils.copyProperties(consoleRenterOrderFineDetailEntity,entity);
                        entity.setSourceCode(String.valueOf(consoleRenterOrderFineDetailEntity.getFineType()));
                        entity.setSourceDesc(consoleRenterOrderFineDetailEntity.getFineTypeDesc());
                        entity.setUniqueNo(String.valueOf(consoleRenterOrderFineDetailEntity.getId()));
                        entity.setAmt(-fineAmount);
                        settleOrdersDefinition.addPlatformProfit(entity);
                    }
                    //罚金补贴方 是平台
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(consoleRenterOrderFineDetailEntity.getFineSubsidySourceCode())){
                        AccountPlatformProfitDetailEntity entity = new AccountPlatformProfitDetailEntity();
                        BeanUtils.copyProperties(consoleRenterOrderFineDetailEntity,entity);
                        entity.setSourceCode(String.valueOf(consoleRenterOrderFineDetailEntity.getFineType()));
                        entity.setSourceDesc(consoleRenterOrderFineDetailEntity.getFineTypeDesc());
                        entity.setUniqueNo(String.valueOf(consoleRenterOrderFineDetailEntity.getId()));
                        entity.setAmt(-fineAmount);
                        settleOrdersDefinition.addPlatformProfit(entity);
                    }
                }
            }
            // 1.6 后台管理操作费用表（无条件补贴）
            List<OrderConsoleCostDetailEntity> orderConsoleCostDetails = rentCosts.getOrderConsoleCostDetailEntity();
            if(!CollectionUtils.isEmpty(orderConsoleCostDetails)){
                for (int i = 0; i < orderConsoleCostDetails.size(); i++) {
                    OrderConsoleCostDetailEntity orderConsoleCostDetailEntity = orderConsoleCostDetails.get(i);
                    AccountRenterCostSettleDetailEntity accountRenterCostSettleDetail = new AccountRenterCostSettleDetailEntity();
                    BeanUtils.copyProperties(orderConsoleCostDetailEntity,accountRenterCostSettleDetail);
                    accountRenterCostSettleDetail.setCostCode(orderConsoleCostDetailEntity.getSubsidyTypeCode());
                    accountRenterCostSettleDetail.setCostDetail(orderConsoleCostDetailEntity.getSubsidTypeName());
                    accountRenterCostSettleDetail.setUniqueNo(String.valueOf(orderConsoleCostDetailEntity.getId()));
                    accountRenterCostSettleDetail.setAmt(orderConsoleCostDetailEntity.getSubsidyAmount());
                    CostTypeEnum costTypeEnum = orderSettleProxyService.getCostTypeEnumByConsoleCost(orderConsoleCostDetailEntity.getSubsidySourceCode());
                    accountRenterCostSettleDetail.setType(costTypeEnum.getCode());
                    accountRenterCostSettleDetails.add(accountRenterCostSettleDetail);

                    //罚金来源方 是平台
                    int costAmount = orderConsoleCostDetailEntity.getSubsidyAmount();
                    //罚金补贴方 是平台
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(orderConsoleCostDetailEntity.getSubsidyTargetCode())){
                        AccountPlatformProfitDetailEntity entity = new AccountPlatformProfitDetailEntity();
                        BeanUtils.copyProperties(orderConsoleCostDetailEntity,entity);
                        entity.setSourceCode(orderConsoleCostDetailEntity.getSubsidyTypeCode());
                        entity.setSourceDesc(orderConsoleCostDetailEntity.getSubsidTypeName());
                        entity.setUniqueNo(String.valueOf(orderConsoleCostDetailEntity.getId()));
                        entity.setAmt(-costAmount);
                        settleOrdersDefinition.addPlatformProfit(entity);
                    }
                    //罚金补贴方 是平台
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(orderConsoleCostDetailEntity.getSubsidySourceCode())){
                        AccountPlatformProfitDetailEntity entity = new AccountPlatformProfitDetailEntity();
                        BeanUtils.copyProperties(orderConsoleCostDetailEntity,entity);
                        entity.setSourceCode(orderConsoleCostDetailEntity.getSubsidyTypeCode());
                        entity.setSourceDesc(orderConsoleCostDetailEntity.getSubsidyTypeCode());
                        entity.setUniqueNo(String.valueOf(orderConsoleCostDetailEntity.getId()));
                        entity.setAmt(-costAmount);
                        settleOrdersDefinition.addPlatformProfit(entity);
                    }
                }
            }

            // -----------------------------------------PlatformSubsidy  以上是PlatformProfit

            //1.7 补贴
            List<RenterOrderSubsidyDetailEntity> renterOrderSubsidyDetails = rentCosts.getRenterOrderSubsidyDetails();
            if(!CollectionUtils.isEmpty(renterOrderSubsidyDetails)) {
                for (int i = 0; i < renterOrderSubsidyDetails.size(); i++) {
                    RenterOrderSubsidyDetailEntity renterOrderSubsidyDetailEntity = renterOrderSubsidyDetails.get(i);
                    AccountRenterCostSettleDetailEntity accountRenterCostSettleDetail = new AccountRenterCostSettleDetailEntity();
                    BeanUtils.copyProperties(renterOrderSubsidyDetailEntity,accountRenterCostSettleDetail);
                    accountRenterCostSettleDetail.setCostCode(renterOrderSubsidyDetailEntity.getSubsidyTypeCode());
                    accountRenterCostSettleDetail.setCostDetail(renterOrderSubsidyDetailEntity.getSubsidyCostName());
                    accountRenterCostSettleDetail.setUniqueNo(String.valueOf(renterOrderSubsidyDetailEntity.getId()));
                    accountRenterCostSettleDetail.setAmt(renterOrderSubsidyDetailEntity.getSubsidyAmount());
                    CostTypeEnum costTypeEnum = orderSettleProxyService.getCostTypeEnumBySubsidy(renterOrderSubsidyDetailEntity.getSubsidySourceCode());
                    accountRenterCostSettleDetail.setType(costTypeEnum.getCode());
                    accountRenterCostSettleDetails.add(accountRenterCostSettleDetail);

                    int subsidyAmount = renterOrderSubsidyDetailEntity.getSubsidyAmount();
                    // 平台补贴 记录补贴
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(renterOrderSubsidyDetailEntity.getSubsidyTargetCode())){
                        AccountPlatformSubsidyDetailEntity entity = new AccountPlatformSubsidyDetailEntity();
                        BeanUtils.copyProperties(renterOrderSubsidyDetailEntity,entity);
                        entity.setSourceCode(renterOrderSubsidyDetailEntity.getSubsidyCostCode());
                        entity.setSourceDesc(renterOrderSubsidyDetailEntity.getSubsidyCostName());
                        entity.setUniqueNo(String.valueOf(renterOrderSubsidyDetailEntity.getId()));
                        entity.setAmt(-subsidyAmount);
                        entity.setSubsidyName(SubsidySourceCodeEnum.RENTER.getDesc());
                        settleOrdersDefinition.addPlatformSubsidy(entity);
                    }
                    // 平台补贴 记录补贴
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(renterOrderSubsidyDetailEntity.getSubsidySourceCode())){
                        AccountPlatformSubsidyDetailEntity entity = new AccountPlatformSubsidyDetailEntity();
                        BeanUtils.copyProperties(renterOrderSubsidyDetailEntity,entity);
                        entity.setSourceCode(renterOrderSubsidyDetailEntity.getSubsidyCostCode());
                        entity.setSourceDesc(renterOrderSubsidyDetailEntity.getSubsidyCostName());
                        entity.setUniqueNo(String.valueOf(renterOrderSubsidyDetailEntity.getId()));
                        entity.setAmt(-subsidyAmount);
                        entity.setSubsidyName(SubsidySourceCodeEnum.RENTER.getDesc());
                        settleOrdersDefinition.addPlatformSubsidy(entity);
                    }
                }
            }
            //1.8 管理后台补贴
            List<OrderConsoleSubsidyDetailEntity> orderConsoleSubsidyDetails = rentCosts.getOrderConsoleSubsidyDetails();
            if(!CollectionUtils.isEmpty(orderConsoleSubsidyDetails)) {
                for (int i = 0; i < orderConsoleSubsidyDetails.size(); i++) {
                    OrderConsoleSubsidyDetailEntity orderConsoleSubsidyDetailEntity = orderConsoleSubsidyDetails.get(i);
                    AccountRenterCostSettleDetailEntity accountRenterCostSettleDetail = new AccountRenterCostSettleDetailEntity();
                    BeanUtils.copyProperties(orderConsoleSubsidyDetailEntity,accountRenterCostSettleDetail);
                    accountRenterCostSettleDetail.setCostCode(orderConsoleSubsidyDetailEntity.getSubsidyCostCode());
                    accountRenterCostSettleDetail.setCostDetail(orderConsoleSubsidyDetailEntity.getSubsidyCostName());
                    accountRenterCostSettleDetail.setUniqueNo(String.valueOf(orderConsoleSubsidyDetailEntity.getId()));
                    accountRenterCostSettleDetail.setAmt(orderConsoleSubsidyDetailEntity.getSubsidyAmount());
                    CostTypeEnum costTypeEnum = orderSettleProxyService.getCostTypeEnumBySubsidy(orderConsoleSubsidyDetailEntity.getSubsidySourceCode());
                    accountRenterCostSettleDetail.setType(costTypeEnum.getCode());
                    accountRenterCostSettleDetails.add(accountRenterCostSettleDetail);

                    // 平台补贴 记录补贴
                    int subsidyAmount = orderConsoleSubsidyDetailEntity.getSubsidyAmount();

                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(orderConsoleSubsidyDetailEntity.getSubsidySourceCode())){
                        AccountPlatformSubsidyDetailEntity entity = new AccountPlatformSubsidyDetailEntity();
                        BeanUtils.copyProperties(orderConsoleSubsidyDetailEntity,entity);
                        entity.setSourceCode(orderConsoleSubsidyDetailEntity.getSubsidyCostCode());
                        entity.setSourceDesc(orderConsoleSubsidyDetailEntity.getSubsidyCostName());
                        entity.setUniqueNo(String.valueOf(orderConsoleSubsidyDetailEntity.getId()));
                        entity.setAmt(-subsidyAmount);
                        entity.setSubsidyName(SubsidySourceCodeEnum.RENTER.getDesc());
                        settleOrdersDefinition.addPlatformSubsidy(entity);
                    }

                    //补充 200308 huangjing 之前这个遗漏？？
                    if(SubsidySourceCodeEnum.PLATFORM.getCode().equals(orderConsoleSubsidyDetailEntity.getSubsidyTargetCode())){
                        AccountPlatformSubsidyDetailEntity entity = new AccountPlatformSubsidyDetailEntity();
                        BeanUtils.copyProperties(orderConsoleSubsidyDetailEntity,entity);
                        entity.setSourceCode(orderConsoleSubsidyDetailEntity.getSubsidyCostCode());
                        entity.setSourceDesc(orderConsoleSubsidyDetailEntity.getSubsidyCostName());
                        entity.setUniqueNo(String.valueOf(orderConsoleSubsidyDetailEntity.getId()));
                        entity.setAmt(-subsidyAmount);
                        entity.setSubsidyName(SubsidySourceCodeEnum.RENTER.getDesc());
                        settleOrdersDefinition.addPlatformSubsidy(entity);
                    }
                }
            }


        }
        settleOrdersDefinition.setAccountRenterCostSettleDetails(accountRenterCostSettleDetails);
    }

    /**
     * 根据流水记录总账
     * @param settleOrdersDefinition
     */
    private void countCost(SettleOrdersDefinition settleOrdersDefinition,SettleOrders settleOrders) {
        List<AccountRenterCostSettleDetailEntity> accountRenterCostSettleDetails = settleOrdersDefinition.getAccountRenterCostSettleDetails();
        //1租客总账
        if(!CollectionUtils.isEmpty(accountRenterCostSettleDetails)){
            //租客结算的总费用
            int renterCostAmtFinal = accountRenterCostSettleDetails.stream().mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            settleOrdersDefinition.setRenterCostAmtFinal(renterCostAmtFinal);

            int rentCostAmt = accountRenterCostSettleDetails.stream().filter(obj ->{return obj.getAmt()<0;}).mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            settleOrdersDefinition.setRentCostAmt(rentCostAmt);
            int rentSubsidyAmt = accountRenterCostSettleDetails.stream().filter(obj ->{return obj.getAmt()>0;}).mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            settleOrdersDefinition.setRentSubsidyAmt(rentSubsidyAmt);
        }
        //2车主总账
//        List<AccountOwnerCostSettleDetailEntity> accountOwnerCostSettleDetails = settleOrdersDefinition.getAccountOwnerCostSettleDetails();
//        if(!CollectionUtils.isEmpty(accountOwnerCostSettleDetails)){
//            int ownerCostAmtFinal = accountOwnerCostSettleDetails.stream().mapToInt(AccountOwnerCostSettleDetailEntity::getAmt).sum();
//            settleOrdersDefinition.setOwnerCostAmtFinal(ownerCostAmtFinal);
//            int ownerCostAmt = accountOwnerCostSettleDetails.stream().filter(obj ->{return obj.getAmt()>0;}).mapToInt(AccountOwnerCostSettleDetailEntity::getAmt).sum();
//            settleOrdersDefinition.setOwnerCostAmt(ownerCostAmt);
//            int ownerSubsidyAmt = accountOwnerCostSettleDetails.stream().filter(obj ->{return obj.getAmt()<0;}).mapToInt(AccountOwnerCostSettleDetailEntity::getAmt).sum();
//            settleOrdersDefinition.setOwnerSubsidyAmt(ownerSubsidyAmt);
//        }
        // 3 计算车主、租客交接车油费差 , 放在车主的方法中，先租客，后车主，后平台的顺序中。目前车主和平台捆绑在一起来结算。
//        orderSettleNewService.addPlatFormAmt(settleOrdersDefinition,settleOrders);
        //4 平台收益总账
//        List<AccountPlatformProfitDetailEntity> accountPlatformProfitDetails = settleOrdersDefinition.getAccountPlatformProfitDetails();
//        if(!CollectionUtils.isEmpty(accountPlatformProfitDetails)){
//            int platformProfitAmt = accountPlatformProfitDetails.stream().mapToInt(AccountPlatformProfitDetailEntity::getAmt).sum();
//            settleOrdersDefinition.setPlatformProfitAmt(platformProfitAmt);
//        }
//        //5 平台补贴总额
//        List<AccountPlatformSubsidyDetailEntity> accountPlatformSubsidyDetails = settleOrdersDefinition.getAccountPlatformSubsidyDetails();
//        if(!CollectionUtils.isEmpty(accountPlatformSubsidyDetails)){
//            int platformSubsidyAmt = accountPlatformSubsidyDetails.stream().mapToInt(AccountPlatformSubsidyDetailEntity::getAmt).sum();
//            settleOrdersDefinition.setPlatformSubsidyAmt(platformSubsidyAmt);
//        }

    }

    /**
     * 结算明细记录落库
     * @param settleOrdersDefinition
     */
    public void insertSettleOrders(SettleOrdersDefinition settleOrdersDefinition) {
        //2 明细落库
        //2.1 租客端 明细落库
        List<AccountRenterCostSettleDetailEntity> accountRenterCostSettleDetails = settleOrdersDefinition.getAccountRenterCostSettleDetails();
        cashierSettleService.insertAccountRenterCostSettleDetails(accountRenterCostSettleDetails);
    }


    /**
     * 结算逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleOrderAfter(SettleOrders settleOrders, SettleOrdersDefinition settleOrdersDefinition, OrderPayCallBack callBack) {
        // 租车费用  总费用 信息落库 并返回最新租车费用 实付 account_Renter_Cost_Settle 租客费用及其结算总表 shifu
        AccountRenterCostSettleEntity accountRenterCostSettle = cashierSettleService.updateRentSettleCost(settleOrders.getOrderNo(), settleOrders.getRenterMemNo(), settleOrdersDefinition.getAccountRenterCostSettleDetails());
        log.info("OrderSettleService updateRentSettleCost [{}]", GsonUtils.toJson(accountRenterCostSettle));
        Cat.logEvent("updateRentSettleCost", GsonUtils.toJson(accountRenterCostSettle));
        // 车主 费用 落库表
        // 获取租客 实付 车辆押金
        //从AccountRenterDepositDetail租车押金资金明细表中求和。  account_renter_deposit_detail
        int depositAmt = cashierSettleService.getRentDeposit(settleOrders.getOrderNo(), settleOrders.getRenterMemNo());
        //从租车押金accountRenterDeposit总表中查询 shifu  shifuDepositAmt
        int depositAmtRealPay = cashierSettleService.getRentDepositRealPay(settleOrders.getOrderNo(), settleOrders.getRenterMemNo());

        SettleOrdersAccount settleOrdersAccount = new SettleOrdersAccount();
        BeanUtils.copyProperties(settleOrders, settleOrdersAccount);
        //查询订单剩余应付
        //租车费用（总）
        settleOrdersAccount.setRentCostAmtFinal(accountRenterCostSettle.getRentAmt());
        settleOrdersAccount.setRentCostPayAmt(accountRenterCostSettle.getShifuAmt());
        settleOrdersAccount.setDepositAmt(depositAmt);
        //动态求和，考虑到退款的情况。真实剩余。
        settleOrdersAccount.setDepositSurplusAmt(depositAmt);

        //>0 表示 实付 大于应退 差值为应退（实付>0  应付小于0 getRentAmt 为所有租车费用明细含补贴 相加之和）
        int rentCostSurplusAmt =
                (accountRenterCostSettle.getRentAmt() + accountRenterCostSettle.getShifuAmt()) > OrderConstant.ZERO ?
                        (accountRenterCostSettle.getRentAmt() + accountRenterCostSettle.getShifuAmt()) : OrderConstant.ZERO;
        settleOrdersAccount.setRentCostSurplusAmt(rentCostSurplusAmt);
        log.info("OrderSettleService settleOrdersDefinition settleOrdersAccount one [{}],depositAmtRealPay=[{}]", GsonUtils.toJson(settleOrdersAccount), depositAmtRealPay);
        Cat.logEvent("settleOrdersAccount", GsonUtils.toJson(settleOrdersAccount));

        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setSettleTime(LocalDateTime.now());
        orderStatusDTO.setOrderNo(settleOrders.getOrderNo());
        orderStatusDTO.setStatus(OrderStatusEnum.TO_WZ_SETTLE.getStatus());
        orderStatusDTO.setSettleStatus(SettleStatusEnum.SETTLED.getCode());
        //车辆押金的结算状态
        orderStatusDTO.setCarDepositSettleStatus(SettleStatusEnum.SETTLED.getCode());
        orderStatusDTO.setCarDepositSettleTime(LocalDateTime.now());

        //9 租客费用 结余处理
        orderSettleNewService.rentCostSettle(settleOrders, settleOrdersAccount, callBack);

        //10 需要根据实际的实收来计算。
        int yingkouAmt1 = accountRenterCostSettle.getShifuAmt() - settleOrdersAccount.getRentCostSurplusAmt();
        int totalOldRealDebtAmt;
        int yingkouAmt;
        int yingkouAmt2;
        if (Objects.nonNull(settleOrders.getIsEnterpriseUserOrder()) && settleOrders.getIsEnterpriseUserOrder()) {
            OrderSettleResVO resVO = orderSettleHandleService.commonDeductionDebtHandle(settleOrdersAccount.getRenterMemNo(), settleOrdersAccount.getOrderNo(), OrderSettleHandleService.DEPOSIT_SETTLE_TYPE);
            totalOldRealDebtAmt = resVO.getOldTotalRealDebtAmt();
            orderStatusDTO.setSettleStatus(resVO.getSettleStatus().getCode());
            orderStatusDTO.setCarDepositSettleStatus(resVO.getSettleStatus().getCode());

            SettleOrderRenterDepositReqVO reqVO = new SettleOrderRenterDepositReqVO();
            reqVO.setOrderNo(settleOrders.getOrderNo());
            reqVO.setMemNo(settleOrders.getRenterMemNo());
            reqVO.setCostEnum(RenterCashCodeEnum.SETTLE_WALLET_TO_RENT_COST);
            reqVO.setSourceEnum(RenterCashCodeEnum.SETTLE_WALLET_TO_RENT_COST);
            // 总计消费，应收=应扣
            reqVO.setShouldTakeAmt(settleOrdersDefinition.getRenterCostAmtFinal());
            reqVO.setRealDeductAmt(resVO.getNewTotalRealDebtAmt() + resVO.getOldTotalRealDebtAmt());
            yingkouAmt2 = orderSettleHandleService.accountRentetDepositHandle(reqVO);
        } else {
            // 10.1 租客车辆押金/租客剩余租车费用 结余历史欠款
            orderSettleNewService.repayHistoryDebtRent(settleOrdersAccount);
            // 10.2 抵扣老系统欠款
            totalOldRealDebtAmt = orderSettleNewService.oldRepayHistoryDebtRent(settleOrdersAccount);
            // 10.3 钱包余额抵扣欠款(押金抵扣->欠款(押金不足)->钱包抵扣->欠款(钱包余额不足))
            if (settleOrdersAccount.getDepositSurplusAmt() <= OrderConstant.ZERO
                    && Objects.nonNull(settleOrders.getUseWallet()) && settleOrders.getUseWallet()) {
                //使用钱包抵扣欠款
                OrderSettleResVO resVO = orderSettleHandleService.commonDeductionDebtHandle(settleOrdersAccount.getRenterMemNo(), settleOrdersAccount.getOrderNo(), OrderSettleHandleService.DEPOSIT_SETTLE_TYPE);
                totalOldRealDebtAmt = totalOldRealDebtAmt + resVO.getOldTotalRealDebtAmt();
                yingkouAmt2 =
                        settleOrdersAccount.getDepositAmt() + resVO.getOldTotalRealDebtAmt() + resVO.getNewTotalRealDebtAmt();
            } else {
                yingkouAmt2 = settleOrdersAccount.getDepositAmt() - settleOrdersAccount.getDepositSurplusAmt();
            }
        }
        settleOrders.setRenterTotalOldRealDebtAmt(totalOldRealDebtAmt);

        //11 租客费用 退还
        orderSettleNewService.refundRentCost(settleOrdersAccount, settleOrdersDefinition.getAccountRenterCostSettleDetails(), orderStatusDTO, settleOrders);
        //12 租客押金 退还
        orderSettleNewService.refundDepositAmt(settleOrdersAccount, orderStatusDTO);

        // 更新订单状态(跟租客结算走)
        settleOrdersAccount.setOrderStatusDTO(orderStatusDTO);
        this.saveOrderStatusInfo(settleOrdersAccount);
        log.info("OrderSettleService settleOrdersDefinition settleOrdersAccount two [{}]", GsonUtils.toJson(settleOrdersAccount));
        yingkouAmt = yingkouAmt1 + yingkouAmt2;

        // 单独修改
        AccountRenterCostSettleEntity entity = new AccountRenterCostSettleEntity();
        // 根据ID来修改
        entity.setOrderNo(settleOrders.getOrderNo());
        entity.setMemNo(settleOrders.getRenterMemNo());
        entity.setYingkouAmt(-yingkouAmt);
        cashierSettleService.updateRentSettleCost(entity);
        log.info("cashierSettleService.updateRentSettleCost. param is,entity:[{}]", GsonUtils.toJson(entity));
        Cat.logEvent("settleUndoCoupon", GsonUtils.toJson(settleOrdersAccount));
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * 车辆结算
     * @param orderNo
     * @return
     */
    public List<AccountRenterCostDetailEntity> getAccountRenterCostDetailsByOrderNo(String orderNo){
        return cashierSettleService.getAccountRenterCostDetailsByOrderNo(orderNo);
    }


    public SettleOrders preInitSettleOrders(String orderNo,String renterOrderNo,String ownerOrderNo) {
        log.info("orderNo={},renterOrderNo={},ownerOrderNo={}",orderNo,renterOrderNo,ownerOrderNo);
        SettleOrders settleOrders = new SettleOrders();
        settleOrders.setOrderNo(orderNo);


        //1 校验参数
        if(StringUtil.isBlank(orderNo)){
            throw new OrderSettleFlatAccountException();
        }
        //查询租客子单
        if(org.apache.commons.lang.StringUtils.isNotBlank(renterOrderNo)) {
            RenterOrderEntity renterOrder = renterOrderService.getRenterOrderByRenterOrderNo(renterOrderNo);
            if(Objects.isNull(renterOrder) || Objects.isNull(renterOrder.getRenterOrderNo())){
                throw new OrderSettleFlatAccountException();
            }
            String renterMemNo = renterOrder.getRenterMemNo();
            settleOrders.setRenterMemNo(renterMemNo);
            settleOrders.setRenterOrderNo(renterOrderNo);
            settleOrders.setRenterOrder(renterOrder);
        } else {  //查询有效的租客子订单
            RenterOrderEntity renterOrder = renterOrderService.getRenterOrderByOrderNoAndIsEffective(orderNo);
            if(Objects.isNull(renterOrder) || Objects.isNull(renterOrder.getRenterOrderNo())){
                throw new OrderSettleFlatAccountException();
            }
            // 3.1获取租客子订单 和 租客会员号
            // 数据封装
            renterOrderNo = renterOrder.getRenterOrderNo();
            String renterMemNo = renterOrder.getRenterMemNo();
            settleOrders.setRenterMemNo(renterMemNo);
            settleOrders.setRenterOrderNo(renterOrderNo);
            settleOrders.setRenterOrder(renterOrder);
        }


        if(org.apache.commons.lang.StringUtils.isNotBlank(ownerOrderNo)) {
            OwnerOrderEntity ownerOrder = ownerOrderService.getOwnerOrderByOwnerOrderNo(ownerOrderNo);
            if(Objects.isNull(ownerOrder) || Objects.isNull(ownerOrder.getOwnerOrderNo())){
                throw new OrderSettleFlatAccountException();
            }

            String ownerMemNo = ownerOrder.getMemNo();
            settleOrders.setOwnerOrderNo(ownerOrderNo);
            settleOrders.setOwnerMemNo(ownerMemNo);
            settleOrders.setOwnerOrder(ownerOrder);

        } else { //查询有效的车主子订单
            OwnerOrderEntity ownerOrder = ownerOrderService.getOwnerOrderByOrderNoAndIsEffective(orderNo);
            if(Objects.isNull(ownerOrder) || Objects.isNull(ownerOrder.getOwnerOrderNo())){
                throw new OrderSettleFlatAccountException();
            }
            String ownerMemNo = ownerOrder.getMemNo();
            settleOrders.setOwnerOrderNo(ownerOrderNo);
            settleOrders.setOwnerMemNo(ownerMemNo);
            settleOrders.setOwnerOrder(ownerOrder);
        }

        // 2 校验订单状态 以及是否存在 理赔暂扣 存在不能进行结算 并CAT告警
//        this.check(renterOrder);
        // 3 初始化数据

        // 3.1获取租客子订单 和 租客会员号
//        String renterOrderNo = renterOrder.getRenterOrderNo();
//        String renterMemNo = renterOrder.getRenterMemNo();
        //3.2获取车主子订单 和 车主会员号
//        String ownerOrderNo = ownerOrder.getOwnerOrderNo();

        return settleOrders;
    }


    /**
     * 租客交接车-油费 参数构建
     * @param settleOrders
     * @param renterOrder
     * @return
     */
    private OilAmtDTO getOilAmtDTO(SettleOrders settleOrders,RenterOrderEntity renterOrder,HandoverCarRepVO handoverCarRep,RenterGoodsDetailDTO renterGoodsDetail) {
        OilAmtDTO oilAmtDTO = new OilAmtDTO();
        CostBaseDTO costBaseDTO = orderSettleProxyService.getCostBaseRent(settleOrders,renterOrder);
        oilAmtDTO.setCostBaseDTO(costBaseDTO);
        oilAmtDTO.setCarOwnerType(renterGoodsDetail.getCarOwnerType());
        OrderEntity orderEntity = orderService.getOrderEntity(settleOrders.getOrderNo());
        String cityCodeStr = "";
        if(Objects.nonNull(orderEntity) && Objects.nonNull(orderEntity.getCityCode())){
            cityCodeStr = orderEntity.getCityCode();
        }
        if(StringUtil.isBlank(cityCodeStr)){
            throw new RuntimeException("结算下单城市不存在");
        }
        oilAmtDTO.setCityCode(Integer.parseInt(cityCodeStr));
        oilAmtDTO.setEngineType(renterGoodsDetail.getCarEngineType());
        oilAmtDTO.setOilVolume(renterGoodsDetail.getCarOilVolume());
        //
        oilAmtDTO.setOilScaleDenominator(renterGoodsDetail.getCarOilVolume());

        //默认值0  取/还 车油表刻度
        oilAmtDTO.setGetOilScale(0);
        oilAmtDTO.setReturnOilScale(0);
        List<RenterHandoverCarInfoEntity> renterHandoverCarInfos = handoverCarRep.getRenterHandoverCarInfoEntities();
        if(!CollectionUtils.isEmpty(renterHandoverCarInfos)){
            for(int i=0;i<renterHandoverCarInfos.size();i++){
                RenterHandoverCarInfoEntity renterHandoverCarInfo = renterHandoverCarInfos.get(i);
                if(RenterHandoverCarTypeEnum.OWNER_TO_RENTER.getValue().equals(renterHandoverCarInfo.getType())
                        ||  RenterHandoverCarTypeEnum.RENYUN_TO_RENTER.getValue().equals(renterHandoverCarInfo.getType())
                        ){
                    oilAmtDTO.setReturnOilScale(Objects.isNull(renterHandoverCarInfo.getOilNum())?0:renterHandoverCarInfo.getOilNum());
                }

                if(RenterHandoverCarTypeEnum.RENTER_TO_OWNER.getValue().equals(renterHandoverCarInfo.getType())
                        ||  RenterHandoverCarTypeEnum.RENTER_TO_RENYUN.getValue().equals(renterHandoverCarInfo.getType())
                        ){
                    oilAmtDTO.setGetOilScale(Objects.isNull(renterHandoverCarInfo.getOilNum())?0:renterHandoverCarInfo.getOilNum());
                }
            }
        }

        return oilAmtDTO;
    }


    /**
     * 车辆结算成功 更新订单状态
     * @param settleOrdersAccount
     */
    public void saveOrderStatusInfo(SettleOrdersAccount settleOrdersAccount) {
        //1更新 订单流转状态

        orderStatusService.saveOrderStatusInfo(settleOrdersAccount.getOrderStatusDTO());
        //2记录订单流传信息
        orderFlowService.inserOrderStatusChangeProcessInfo(settleOrdersAccount.getOrderNo(), OrderStatusEnum.TO_WZ_SETTLE);
        // 更新租客订单状态
        renterOrderService.updateRenterStatusByRenterOrderNo(settleOrdersAccount.getRenterOrderNo(), OrderStatusEnum.TO_WZ_SETTLE.getStatus());
        // 更新车主订单状态
        ownerOrderService.updateOwnerStatusByOwnerOrderNo(settleOrdersAccount.getOwnerOrderNo(), OrderStatusEnum.TO_WZ_SETTLE.getStatus());
    }


    /**
     * 取消订单结算 数据初始化
     * @param orderNo
     * @return
     */
    public SettleOrders initCancelSettleOrders(String orderNo) {
        //1 校验参数
        if(StringUtil.isBlank(orderNo)){
            throw new OrderSettleFlatAccountException();
        }
        RenterOrderEntity renterOrder = renterOrderService.getRenterOrderByOrderNoAndIsEffective(orderNo);
        if(Objects.isNull(renterOrder) || Objects.isNull(renterOrder.getRenterOrderNo())){
            log.error("获取结算订单失败renterOrder={},orderNo={}",renterOrder,orderNo);
            throw new OrderSettleFlatAccountException();
        }
        OwnerOrderEntity ownerOrder = ownerOrderService.queryCancelOwnerOrderByOrderNoIsEffective(orderNo);
        // 3 初始化数据
        // 3.1获取租客子订单 和 租客会员号
        String renterOrderNo = renterOrder.getRenterOrderNo();
        String renterMemNo = renterOrder.getRenterMemNo();
        //3.2获取车主子订单 和 车主会员号
        String ownerOrderNo = Objects.nonNull(ownerOrder)?ownerOrder.getOwnerOrderNo():"";
        String ownerMemNo = Objects.nonNull(ownerOrder)?ownerOrder.getMemNo():"";

        SettleOrders settleOrders = new SettleOrders();
        settleOrders.setOrderNo(orderNo);
        settleOrders.setRenterOrderNo(renterOrderNo);
        settleOrders.setOwnerOrderNo(ownerOrderNo);
        settleOrders.setRenterMemNo(renterMemNo);
        settleOrders.setOwnerMemNo(ownerMemNo);
        settleOrders.setRenterOrder(renterOrder);
        settleOrders.setOwnerOrder(ownerOrder);
        return settleOrders;
    }

    /**
     * 取消订单查询 获取租客罚金信息
     * @param settleOrders
     */
    public void getCancelRenterCostSettleDetail(SettleOrders settleOrders) {
        //1 租客罚金
        List<RenterOrderFineDeatailEntity> renterOrderFineDeatails = renterOrderFineDeatailService.listRenterOrderFineDeatail(settleOrders.getOrderNo(),settleOrders.getRenterOrderNo());
        //2 获取全局的租客订单罚金明细（租客车主共用表 ，会员号区分车主/租客）
        List<ConsoleRenterOrderFineDeatailEntity> consoleRenterOrderFineDeatails = consoleRenterOrderFineDeatailService.listConsoleRenterOrderFineDeatail(settleOrders.getOrderNo(),settleOrders.getRenterMemNo());
        //3 补贴包含凹凸币信息(这里其实只需要凹凸币的补贴)
        List<RenterOrderSubsidyDetailEntity> renterOrderSubsidyDetails = renterOrderSubsidyDetailService.listRenterOrderSubsidyDetail(settleOrders.getOrderNo(),settleOrders.getRenterOrderNo());
        //4.`order_console_subsidy_detail` 管理后台对租客的补贴不用管

        RentCosts rentCosts = new RentCosts();
        rentCosts.setRenterOrderSubsidyDetails(renterOrderSubsidyDetails);
        rentCosts.setRenterOrderFineDeatails(renterOrderFineDeatails);
        rentCosts.setConsoleRenterOrderFineDeatails(consoleRenterOrderFineDeatails);
        settleOrders.setRentCosts(rentCosts);
    }




    /**
     * 查询所有车主罚金明细
     * @param settleOrders
     */
    public void getCancelOwnerCostSettleDetail(SettleOrders settleOrders) {
        OwnerCosts ownerCosts = new OwnerCosts();
        //1 获取全局的车主订单罚金明细
        List<ConsoleOwnerOrderFineDeatailEntity> consoleOwnerOrderFineDeatailEntitys = consoleOwnerOrderFineDeatailService.selectByOrderNo(settleOrders.getOrderNo(),settleOrders.getOwnerMemNo());

        //2 车主罚金
        List<OwnerOrderFineDeatailEntity> ownerOrderFineDeatails = ownerOrderFineDeatailService.getOwnerOrderFineDeatailByOrderNo(settleOrders.getOrderNo(),settleOrders.getOwnerOrderNo());
        //不考虑补贴? 车主和管理后台补贴? 非正常的结算，异常终止，可以理解为管理后台的都不计算。只考虑违约金的情况。

        ownerCosts.setOwnerOrderFineDeatails(ownerOrderFineDeatails);
        ownerCosts.setConsoleOwnerOrderFineDeatailEntitys(consoleOwnerOrderFineDeatailEntitys);
        settleOrders.setOwnerCosts(ownerCosts);
    }


    /**
     * 车主存在 罚金 走历史欠款
     * @param settleCancelOrdersAccount
     */
    public void handleOwnerFine(SettleOrders settleOrders,SettleCancelOrdersAccount settleCancelOrdersAccount) {
        //1 车主存在 罚金走历史欠款
        if(settleCancelOrdersAccount.getOwnerFineAmt()<0){
            //2 记录历史欠款
            AccountInsertDebtReqVO accountInsertDebt = new AccountInsertDebtReqVO();
            BeanUtils.copyProperties(settleOrders,accountInsertDebt);
            accountInsertDebt.setType(DebtTypeEnum.CANCEL.getCode());
            accountInsertDebt.setMemNo(settleOrders.getOwnerMemNo());
            accountInsertDebt.setSourceCode(RenterCashCodeEnum.HISTORY_AMT.getCashNo());
            accountInsertDebt.setSourceDetail(RenterCashCodeEnum.HISTORY_AMT.getTxt());
            accountInsertDebt.setAmt(settleCancelOrdersAccount.getOwnerFineAmt());
            /*
               有历史欠款就更新欠款总金额，没有历史欠款就新增历史欠款  insert/update  account_debt
               添加历史欠款明细记录 insert  account_debt_detail
             */
            cashierService.createDebt(accountInsertDebt);
        }
    }

    /**
     租客存在罚金 （抵扣优先级 钱包》租车费用》车辆押金》违章押金）
     * @param settleOrders
     * @param settleCancelOrdersAccount
     */
    public void handleRentFine(SettleOrders settleOrders, SettleCancelOrdersAccount settleCancelOrdersAccount) {
        int rentCostAmt = settleCancelOrdersAccount.getRentCostAmt();
        int rentDepositAmt =  settleCancelOrdersAccount.getRentDepositAmt();
        int rentWzDepositAmt = settleCancelOrdersAccount.getRentWzDepositAmt();
        int renWalletAmt =  settleCancelOrdersAccount.getRenWalletAmt();

        //2 罚金处理
        int rentFineAmt = settleCancelOrdersAccount.getRentFineAmt();
        //2.1 钱包抵罚金
        if(renWalletAmt>0 && rentFineAmt<0){
            AccountRenterCostToFineReqVO vo = new AccountRenterCostToFineReqVO();
            BeanUtils.copyProperties(settleOrders,vo);
            vo.setMemNo(settleOrders.getRenterMemNo());
            int debtAmt = renWalletAmt + rentFineAmt;
            //计算抵扣金额
            int amt = debtAmt>=0?rentFineAmt:-renWalletAmt;
            vo.setAmt(amt);
            //钱包支付金额抵扣 罚金 insert account_renter_cost_detail
            cashierSettleService.deductWalletCostToRentFine(vo);
            rentFineAmt = renWalletAmt + rentFineAmt;
            settleCancelOrdersAccount.setRentSurplusWalletAmt(settleCancelOrdersAccount.getRentSurplusWalletAmt()+amt);
        }
        //2.2 租车费用抵罚金
        if(rentCostAmt>0 && rentFineAmt<0){
            AccountRenterCostToFineReqVO vo = new AccountRenterCostToFineReqVO();
            BeanUtils.copyProperties(settleOrders,vo);
            vo.setMemNo(settleOrders.getRenterMemNo());
            int debtAmt = rentCostAmt + rentFineAmt;
            //计算抵扣金额
            int amt = debtAmt>=0?rentFineAmt:-rentCostAmt;
            vo.setAmt(amt);
            //租车费用抵扣 罚金 insert account_renter_cost_detail
            cashierSettleService.deductRentCostToRentFine(vo);
            rentFineAmt = rentCostAmt + rentFineAmt;
            settleCancelOrdersAccount.setRentSurplusCostAmt(settleCancelOrdersAccount.getRentSurplusCostAmt()+amt);
        }
        //2.2 车辆押金抵扣
        if(rentDepositAmt>0 && rentFineAmt<0){
            OrderCancelRenterDepositReqVO vo = new OrderCancelRenterDepositReqVO();
            BeanUtils.copyProperties(settleOrders,vo);
            vo.setMemNo(settleOrders.getRenterMemNo());
            int debtAmt = rentDepositAmt + rentFineAmt;
            //计算抵扣金额
            int amt = debtAmt>=0?rentFineAmt:-rentDepositAmt;
            vo.setAmt(amt);
            //押金抵扣抵扣 罚金
            // insert account_renter_deposit_detail
            // update account_renter_deposit
            cashierSettleService.deductRentDepositToRentFine(vo);
            rentFineAmt = rentDepositAmt + rentFineAmt;
            settleCancelOrdersAccount.setRentSurplusDepositAmt(settleCancelOrdersAccount.getRentSurplusDepositAmt()+amt);
        }
        //2.2 违章押金抵扣
        if(rentWzDepositAmt>0 && rentFineAmt<0){
            RenterCancelWZDepositCostReqVO vo = new RenterCancelWZDepositCostReqVO();
            BeanUtils.copyProperties(settleOrders,vo);
            vo.setMemNo(settleOrders.getRenterMemNo());
            int debtAmt = rentWzDepositAmt + rentFineAmt;
            //计算抵扣金额
            int amt = debtAmt>=0?rentFineAmt:-rentWzDepositAmt;
            vo.setAmt(amt);
            //押金抵扣抵扣 罚金
            //insert account_renter_wz_deposit_detail 违章押金进出明细表
            //TODO zhangbin account_renter_wz_deposit  不管么？？ 结算状态、结算金额、应付金额、实付金额是否需要变化
            cashierSettleService.deductRentWzDepositToRentFine(vo);
            rentFineAmt = rentWzDepositAmt + rentFineAmt;
            settleCancelOrdersAccount.setRentSurplusWzDepositAmt(settleCancelOrdersAccount.getRentSurplusWzDepositAmt()+amt);
        }
        //3 钱包，租车费用、车辆押金、违章押金抵扣之后，罚金还有剩余，则罚金走个人历史欠款
        if(rentFineAmt<0){
            //2 记录历史欠款
            AccountInsertDebtReqVO accountInsertDebt = new AccountInsertDebtReqVO();
            BeanUtils.copyProperties(settleOrders,accountInsertDebt);
            accountInsertDebt.setType(DebtTypeEnum.CANCEL.getCode());
            accountInsertDebt.setMemNo(settleOrders.getRenterMemNo());
            accountInsertDebt.setSourceCode(RenterCashCodeEnum.HISTORY_AMT.getCashNo());
            accountInsertDebt.setSourceDetail(RenterCashCodeEnum.HISTORY_AMT.getTxt());
            accountInsertDebt.setAmt(rentFineAmt);
            //历史欠款 有就跟心欠款总额，没有就插入历史欠款。都需要添加欠款流水记录
            cashierService.createDebt(accountInsertDebt);
        }
    }

    /**
     * 租客金额 退还 包含 凹凸币，钱包 租车费用 押金 违章押金 退还
     *
     * @param settleOrders              初始化参数
     * @param settleCancelOrdersAccount 公共参数
     * @param orderStatusDTO            订单状态
     */
    public void refundCancelCost(SettleOrders settleOrders, SettleCancelOrdersAccount settleCancelOrdersAccount, OrderStatusDTO orderStatusDTO) {
        //1退还凹凸币
        if (settleCancelOrdersAccount.getRenCoinAmt() > 0) {
            accountRenterCostCoinService.settleAutoCoin(settleOrders.getRenterMemNo(), settleOrders.getOrderNo(), settleCancelOrdersAccount.getRenCoinAmt());
        }
        //2补贴给租客的费用走钱包
        if (settleCancelOrdersAccount.getRentFineTotal() > OrderConstant.ZERO) {
            walletProxyService.returnOrChargeWallet(settleOrders.getRenterMemNo(), settleOrders.getOrderNo(), settleCancelOrdersAccount.getRentFineTotal());
            //记录退还
            AccountRenterCostDetailReqVO accountRenterCostDetail = new AccountRenterCostDetailReqVO();
            accountRenterCostDetail.setMemNo(settleOrders.getRenterMemNo());
            accountRenterCostDetail.setOrderNo(settleOrders.getOrderNo());
            accountRenterCostDetail.setPaySource(com.atzuche.order.commons.enums.cashier.PaySourceEnum.WALLET_PAY.getText());
            accountRenterCostDetail.setPaySourceCode(com.atzuche.order.commons.enums.cashier.PaySourceEnum.WALLET_PAY.getCode());
            accountRenterCostDetail.setRenterCashCodeEnum(RenterCashCodeEnum.CANCEL_RENT_COST_TO_RETURN_AMT);
            accountRenterCostDetail.setAmt(-settleCancelOrdersAccount.getRentFineTotal());
            accountRenterCostDetail.setPayTypeCode(PayTypeEnum.PUR_RETURN.getCode());
            accountRenterCostDetail.setPayType(PayTypeEnum.PUR_RETURN.getText());
            cashierService.refundRentCostWallet(accountRenterCostDetail);
        }
        OrderSettleCommonParamDTO common = new OrderSettleCommonParamDTO();
        common.setOrderNo(settleOrders.getOrderNo());
        common.setMemNo(settleOrders.getRenterMemNo());
        common.setRenterOrderNo(settleOrders.getRenterOrderNo());

        //3租车费用退还
        if (settleCancelOrdersAccount.getRentSurplusCostAmt() > 0) {
            common.setCashCodeEnum(RenterCashCodeEnum.SETTLE_RENT_COST_TO_RETURN_AMT);
            common.setRentCarCostCashCodeEnum(RenterCashCodeEnum.CANCEL_RENT_COST_TO_RETURN_AMT);
            OrderSettleCommonResultDTO result = orderSettleRefundHandleService.rentCarCostRefundHandle(common,
                    settleCancelOrdersAccount.getRentSurplusCostAmt());
            orderStatusDTO.setRentCarRefundStatus(result.getStatus());
        }

        //4车辆押金退还
        if(settleCancelOrdersAccount.getRentSurplusDepositAmt()>0){
            common.setCashCodeEnum(RenterCashCodeEnum.CANCEL_RENT_DEPOSIT_TO_RETURN_AMT);
            OrderSettleCommonResultDTO result = orderSettleRefundHandleService.rentCarDepositRefundHandle(common,
                    settleCancelOrdersAccount.getRentSurplusDepositAmt());
            orderStatusDTO.setDepositRefundStatus(result.getStatus());
        }

        //5违章押金退还
        if (settleCancelOrdersAccount.getRentSurplusWzDepositAmt() > 0) {
            common.setCashCodeEnum(RenterCashCodeEnum.CANCEL_RENT_WZ_DEPOSIT_TO_RETURN_AMT);
            OrderSettleCommonResultDTO result = orderSettleRefundHandleService.wzDepositRefundHandle(common,
                    settleCancelOrdersAccount.getRentSurplusWzDepositAmt());
            orderStatusDTO.setWzRefundStatus(result.getStatus());
        }
    }


    /**
     * 租客还历史欠款
     */
    public void repayHistoryDebtRentCancel(SettleOrders settleOrders, SettleCancelOrdersAccount settleCancelOrdersAccount) {
        //钱包抵扣历史欠款(补贴租客金额,租客收益走钱包)
        if(settleCancelOrdersAccount.getRentFineTotal() > OrderConstant.ZERO){
            CashierDeductDebtReqVO cashierDeductDebtReq = new CashierDeductDebtReqVO();
            BeanUtils.copyProperties(settleOrders,cashierDeductDebtReq);
            cashierDeductDebtReq.setAmt(settleCancelOrdersAccount.getRentFineTotal());
            cashierDeductDebtReq.setRenterCashCodeEnum(RenterCashCodeEnum.SETTLE_RENT_WALLET_TO_HISTORY_AMT);
            cashierDeductDebtReq.setMemNo(settleOrders.getRenterMemNo());
            cashierDeductDebtReq.setPaySource(PaySourceEnum.WALLET_PAY.getText());
            cashierDeductDebtReq.setPaySourceCode(PaySourceEnum.WALLET_PAY.getCode());
            CashierDeductDebtResVO result = cashierService.deductDebtByRentCost(cashierDeductDebtReq);
            if(Objects.nonNull(result)){
                //已抵扣抵扣金额
                int deductAmt = result.getDeductAmt();
                //计算 还完历史欠款 剩余 应退 剩余租客收益
                settleCancelOrdersAccount.setRentFineTotal(settleCancelOrdersAccount.getRentFineTotal() - deductAmt);
            }
        }
        //租车费用
        if(settleCancelOrdersAccount.getRentSurplusCostAmt()>0){
            CashierDeductDebtReqVO cashierDeductDebtReq = new CashierDeductDebtReqVO();
            BeanUtils.copyProperties(settleOrders,cashierDeductDebtReq);
            cashierDeductDebtReq.setAmt(settleCancelOrdersAccount.getRentSurplusCostAmt());
            cashierDeductDebtReq.setRenterCashCodeEnum(RenterCashCodeEnum.SETTLE_RENT_COST_TO_HISTORY_AMT);
            cashierDeductDebtReq.setMemNo(settleOrders.getRenterMemNo());
            CashierDeductDebtResVO result = cashierService.deductDebtByRentCost(cashierDeductDebtReq);
            if(Objects.nonNull(result)){
                //已抵扣抵扣金额
                int deductAmt = result.getDeductAmt();
                //计算 还完历史欠款 剩余 应退 剩余租车费用
                settleCancelOrdersAccount.setRentSurplusCostAmt(settleCancelOrdersAccount.getRentSurplusCostAmt() - deductAmt);
            }
        }
        //车辆押金
        if(settleCancelOrdersAccount.getRentSurplusDepositAmt()>0){
            CashierDeductDebtReqVO cashierDeductDebtReq = new CashierDeductDebtReqVO();
            BeanUtils.copyProperties(settleOrders,cashierDeductDebtReq);
            cashierDeductDebtReq.setAmt(settleCancelOrdersAccount.getRentSurplusDepositAmt());
            cashierDeductDebtReq.setRenterCashCodeEnum(RenterCashCodeEnum.SETTLE_DEPOSIT_TO_HISTORY_AMT);
            cashierDeductDebtReq.setMemNo(settleOrders.getRenterMemNo());
            CashierDeductDebtResVO result = cashierService.deductDebt(cashierDeductDebtReq);
            if(Objects.nonNull(result)){
                //已抵扣抵扣金额
                int deductAmt = result.getDeductAmt();
                //计算 还完历史欠款 剩余 应退 剩余租车费用
                settleCancelOrdersAccount.setRentSurplusDepositAmt(settleCancelOrdersAccount.getRentSurplusDepositAmt() - deductAmt);
            }
        }

        //违章押金
        if(settleCancelOrdersAccount.getRentSurplusWzDepositAmt()>0){
            CashierDeductDebtReqVO cashierDeductDebtReq = new CashierDeductDebtReqVO();
            BeanUtils.copyProperties(settleOrders,cashierDeductDebtReq);
            cashierDeductDebtReq.setAmt(settleCancelOrdersAccount.getRentSurplusWzDepositAmt());
            cashierDeductDebtReq.setRenterCashCodeEnum(RenterCashCodeEnum.SETTLE_WZ_TO_HISTORY_AMT);
            cashierDeductDebtReq.setMemNo(settleOrders.getRenterMemNo());
            CashierDeductDebtResVO result = cashierWzSettleService.deductWZDebt(cashierDeductDebtReq);
            if(Objects.nonNull(result)){
                //已抵扣抵扣金额
                int deductAmt = result.getDeductAmt();
                //计算 还完历史欠款 剩余 应退 剩余租车费用
                settleCancelOrdersAccount.setRentSurplusWzDepositAmt(settleCancelOrdersAccount.getRentSurplusWzDepositAmt() - deductAmt);
            }
        }
    }

    /**
     * 车俩结算 优惠卷 退还
     * @param renterOrderSubsidyDetails 租客补贴 列表
     */
    public void settleUndoCoupon(String orderNo,List<RenterOrderSubsidyDetailEntity> renterOrderSubsidyDetails) {
        boolean isUndoCoupon=false;
        boolean isUndoGetCarFeeCoupon=false;
        boolean isUndoOwnerCoupon=false;
        List<RenterOrderSubsidyDetailEntity> isUndoCouponDetails = renterOrderSubsidyDetails.stream().filter(obj ->{
            return RenterCashCodeEnum.REAL_COUPON_OFFSET.equals(obj.getSubsidyCostCode());
        }).collect(Collectors.toList());
        List<RenterOrderSubsidyDetailEntity> isUndoGetCarFeeCouponDetails = renterOrderSubsidyDetails.stream().filter(obj ->{
            return RenterCashCodeEnum.GETCARFEE_COUPON_OFFSET.equals(obj.getSubsidyCostCode());
        }).collect(Collectors.toList());
        List<RenterOrderSubsidyDetailEntity> isUndoOwnerCouponDetails = renterOrderSubsidyDetails.stream().filter(obj ->{
            return RenterCashCodeEnum.OWNER_COUPON_OFFSET_COST.equals(obj.getSubsidyCostCode());
        }).collect(Collectors.toList());
        //判断是否要退
        isUndoCoupon = CollectionUtils.isEmpty(isUndoCouponDetails);
        isUndoGetCarFeeCoupon = CollectionUtils.isEmpty(isUndoGetCarFeeCouponDetails);
        isUndoOwnerCoupon = CollectionUtils.isEmpty(isUndoOwnerCouponDetails);
        // 结算退回优惠卷
        try {
            orderCouponService.settleUndoCoupon(orderNo,isUndoCoupon,isUndoGetCarFeeCoupon,isUndoOwnerCoupon);
        } catch (Exception e) {
            log.error("OrderSettleNoTService settleUndoCoupon error [{}]",e);
        }

    }

    /**
     * 处理
     * @param settleOrders
     * @param settleCancelOrdersAccount
     */
    public void handleIncomeFine(SettleOrders settleOrders, SettleCancelOrdersAccount settleCancelOrdersAccount) {
        AccountPlatformProfitEntity accountPlatformProfitEntity = new AccountPlatformProfitEntity();
        accountPlatformProfitEntity.setOrderNo(settleOrders.getOrderNo());
        accountPlatformProfitEntity.setStatus(PlatformProfitStatusEnum.CANCEL_SETTLE.getCode());

        // 租客罚金收益，租客罚金收益退到钱包
        if(settleCancelOrdersAccount.getRentFineIncomeAmt()!=0){
            AccountRenterCostSettleDetailEntity entity = new AccountRenterCostSettleDetailEntity();
            entity.setOrderNo(settleOrders.getOrderNo());
            entity.setMemNo(settleOrders.getRenterMemNo());
            entity.setRenterOrderNo(settleOrders.getRenterOrderNo());
            entity.setAmt(settleCancelOrdersAccount.getRentFineIncomeAmt());
            entity.setType(1);
            //insert 流水记录 account_renter_cost_settle_detail(租车费用结算明细表)
            cashierSettleService.insertAccountRenterCostSettleDetail(entity);
            //租客罚金收益退到钱包（调用远程钱包服务）
            walletProxyService.returnOrChargeWallet(settleOrders.getRenterMemNo(),settleOrders.getOrderNo(),settleCancelOrdersAccount.getRentFineIncomeAmt());
        }
        // 车主罚金收入，走收益审核列表。
        if(settleCancelOrdersAccount.getOwnerFineIncomeAmt()!=0){
            AccountOwnerIncomeExamineReqVO accountOwnerIncomeExamine = new AccountOwnerIncomeExamineReqVO();
            accountOwnerIncomeExamine.setAmt(settleCancelOrdersAccount.getOwnerFineIncomeAmt());
            accountOwnerIncomeExamine.setMemNo(settleOrders.getOwnerMemNo());
            accountOwnerIncomeExamine.setOrderNo(settleOrders.getOrderNo());
            accountOwnerIncomeExamine.setOwnerOrderNo(settleOrders.getOwnerOrderNo());
            accountOwnerIncomeExamine.setRemark("罚金收入");
            accountOwnerIncomeExamine.setDetail("罚金收入");
            accountOwnerIncomeExamine.setOwnerOrderNo(settleOrders.getOwnerOrderNo());
            accountOwnerIncomeExamine.setStatus(AccountOwnerIncomeExamineStatus.WAIT_EXAMINE);
            accountOwnerIncomeExamine.setType(AccountOwnerIncomeExamineType.OWNER_INCOME);
            //insert  account_owner_income_examine(车主收益待审核表)
            cashierService.insertOwnerIncomeExamine(accountOwnerIncomeExamine);
            //insert 流水记录 account_owner_cost_settle_detail 租车费用结算明细
            cashierSettleService.insertAccountOwnerCostSettleDetails(settleCancelOrdersAccount.getAccountOwnerCostSettleDetails());
            accountPlatformProfitEntity.setOwnerIncomeAmt(-settleCancelOrdersAccount.getOwnerFineIncomeAmt());
        }
        // 平台罚金收入
        if(settleCancelOrdersAccount.getPlatformFineImconeAmt()!=0){
            List<AccountPlatformProfitDetailEntity> accountPlatformProfitDetails = new ArrayList<>();
            AccountPlatformProfitDetailEntity entity = new AccountPlatformProfitDetailEntity();
            entity.setOrderNo(settleOrders.getOrderNo());
            entity.setAmt(settleCancelOrdersAccount.getPlatformFineImconeAmt());
            entity.setSourceDesc(RenterCashCodeEnum.ACCOUNT_RENTER_FINE_COST.getTxt());
            entity.setSourceCode(RenterCashCodeEnum.ACCOUNT_RENTER_FINE_COST.getCashNo());
            accountPlatformProfitDetails.add(entity);
            //insert 流水记录 account_platform_profit_detail(平台结算收益明细表)
            cashierSettleService.insertAccountPlatformProfitDetails(accountPlatformProfitDetails);
            accountPlatformProfitEntity.setPlatformReceivableAmt(settleCancelOrdersAccount.getPlatformFineImconeAmt());
            accountPlatformProfitEntity.setPlatformReceivedAmt(settleCancelOrdersAccount.getPlatformFineImconeAmt());
        }
        //insert 结算总表 account_platform_profit(平台订单收益结算表)
        cashierSettleService.insertAccountPlatformProfit(accountPlatformProfitEntity);
    }


    /**
     * 结算退还租车费用金额 费用明细
     * @param settleOrders 公共参数
     * @param rentCostSurplusAmt 剩余租车费用
     * @return AccountRenterCostSettleDetailEntity
     */
    private AccountRenterCostSettleDetailEntity getAccountRenterCostSettleDetailEntityForRentCost(SettleOrders settleOrders, int rentCostSurplusAmt, int id) {
        AccountRenterCostSettleDetailEntity entity = new AccountRenterCostSettleDetailEntity();
        BeanUtils.copyProperties(settleOrders,entity);
        entity.setMemNo(settleOrders.getRenterMemNo());
        entity.setAmt(rentCostSurplusAmt);
        entity.setCostCode(RenterCashCodeEnum.SETTLE_RENT_COST_TO_RETURN_AMT.getCashNo());
        entity.setCostDetail(RenterCashCodeEnum.SETTLE_RENT_COST_TO_RETURN_AMT.getTxt());
        entity.setUniqueNo(String.valueOf(id));
        return entity;
    }
}
