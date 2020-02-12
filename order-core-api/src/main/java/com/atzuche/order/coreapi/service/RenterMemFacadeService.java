package com.atzuche.order.coreapi.service;

import com.atzuche.order.rentermem.entity.RenterMemberEntity;
import com.atzuche.order.rentermem.entity.RenterMemberRightEntity;
import com.atzuche.order.rentermem.service.RenterMemberRightService;
import com.atzuche.order.rentermem.service.RenterMemberService;
import com.atzuche.order.renterorder.entity.RenterAdditionalDriverEntity;
import com.atzuche.order.renterorder.service.RenterAdditionalDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author <a href="mailto:lianglin.sjtu@gmail.com">AndySjtu</a>
 * @date 2020/2/12 11:20 上午
 **/
@Service
public class RenterMemFacadeService {
    @Autowired
    private RenterAdditionalDriverService renterAdditionalDriverService;
    @Autowired
    private RenterMemberService renterMemberService;
    @Autowired
    private RenterMemberRightService renterMemberRightService;

    public void getRenterMemInfo(String orderNo,String renterOrderNo,String renterNo){
        RenterMemberEntity renterMemberEntity = renterMemberService.queryRenterInfoByOrderNoAndRenterNo(orderNo,renterNo);
        List<RenterMemberRightEntity> rightEntities= renterMemberRightService.findByOrderNoAndMemNo(orderNo);
        List<RenterAdditionalDriverEntity> additionalDriverEntities = renterAdditionalDriverService.listDriversByRenterOrderNo(renterOrderNo);

    }


}
