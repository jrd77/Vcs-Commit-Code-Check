package com.atzuche.order.coreapi.service;

import com.atzuche.order.coreapi.entity.vo.req.AutoCoinDeductReqVO;
import com.atzuche.order.coreapi.entity.vo.req.OwnerCouponBindReqVO;
import com.atzuche.order.renterorder.service.AutoCoinService;
import com.atzuche.order.renterorder.service.OwnerDiscountCouponService;
import com.atzuche.order.renterorder.service.PlatformCouponService;
import com.autoyol.auto.coin.service.vo.req.AutoCoiChargeRequestVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 优惠券、凹凸币后续操作
 *
 * @author pengcheng.fu
 * @date 2020/1/1 15:49
 */

@Service
public class CouponAndCoinHandleService {

    @Autowired
    private OwnerDiscountCouponService ownerDiscountCouponService;

    @Autowired
    private PlatformCouponService platformCouponService;

    @Autowired
    private AutoCoinService autoCoinService;

    /**
     * 车主券绑定
     *
     * @param ownerCouponBindReqVO 请求参数
     * @return boolean
     */
    public boolean bindOwnerCoupon(OwnerCouponBindReqVO ownerCouponBindReqVO) {

        if (null == ownerCouponBindReqVO || StringUtils.isBlank(ownerCouponBindReqVO.getCouponNo())) {
            return true;
        }

        String orderNo = ownerCouponBindReqVO.getOrderNo();
        Integer rentAmt = ownerCouponBindReqVO.getRentAmt();
        String couponNo = ownerCouponBindReqVO.getCouponNo();
        Integer carNo = ownerCouponBindReqVO.getCarNo();
        String renterName = ownerCouponBindReqVO.getRenterName();
        String renterFirstName = ownerCouponBindReqVO.getRenterFirstName();
        String renterSex = ownerCouponBindReqVO.getRenterSex();
        return ownerDiscountCouponService.bindCoupon(orderNo, rentAmt, couponNo, carNo, renterName, renterFirstName, renterSex);
    }


    /**
     * 绑定平台券+送取服务券
     *
     * @param orderNo      主订单号
     * @param disCopuponId 优惠券ID
     * @param type         券类型:1,平台券 2,送取服务券
     * @param isUse        是否使用
     * @return boolean
     */
    public boolean bindCoupon(String orderNo, String disCopuponId, int type, Boolean isUse) {
        if (StringUtils.isBlank(disCopuponId)) {
            return true;
        }
        if(null == isUse || !isUse) {
            return true;
        }

        if (type == 1) {
            return platformCouponService.usePlatformCoupon(disCopuponId, Long.valueOf(orderNo), new Date()) > 0;
        }
        return platformCouponService.useGetCarFreeCoupon(disCopuponId, Long.valueOf(orderNo), new Date()) > 0;
    }


    /**
     * 扣除凹凸币
     *
     * @param autoCoinDeductReqVO 请求参数
     * @return boolean
     */
    public boolean deductionAotuCoin(AutoCoinDeductReqVO autoCoinDeductReqVO) {

        if (null == autoCoinDeductReqVO
                || null == autoCoinDeductReqVO.getUseAutoCoin() || autoCoinDeductReqVO.getUseAutoCoin() == 0
                || null == autoCoinDeductReqVO.getChargeAutoCoin() || autoCoinDeductReqVO.getChargeAutoCoin() == 0) {
            return true;
        }
        AutoCoiChargeRequestVO vo = new AutoCoiChargeRequestVO();
        BeanCopier beanCopier = BeanCopier.create(AutoCoinDeductReqVO.class, AutoCoiChargeRequestVO.class, false);
        beanCopier.copy(autoCoinDeductReqVO, vo, null);
        return autoCoinService.deduct(vo);
    }




    public boolean undoOwnerCoupon(String orderNo, String couponNo, String recover){
        if(StringUtils.isBlank(couponNo)) {
            return true;
        }
        return ownerDiscountCouponService.undoCoupon(orderNo, couponNo, recover);
    }

    public boolean undoPlatformCoupon(String orderNo) {
        return platformCouponService.cancelPlatformCoupon(orderNo) > 0;
    }

    public boolean undoGetCarFeeCoupon(String orderNo) {
        return platformCouponService.cancelGetCarFeeCoupon(orderNo) > 0;
    }

}
