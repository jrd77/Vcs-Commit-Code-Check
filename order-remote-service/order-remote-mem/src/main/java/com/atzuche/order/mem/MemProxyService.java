package com.atzuche.order.mem;

import com.alibaba.fastjson.JSON;
import com.atzuche.order.commons.CatConstants;
import com.atzuche.order.commons.LocalDateTimeUtils;
import com.atzuche.order.commons.entity.dto.*;
import com.atzuche.order.commons.enums.MemberFlagEnum;
import com.atzuche.order.commons.enums.OwnerMemRightEnum;
import com.atzuche.order.commons.enums.RenterMemRightEnum;
import com.atzuche.order.commons.enums.RightTypeEnum;
import com.atzuche.order.mem.dto.OrderRenterInfoDTO;
import com.autoyol.commons.web.ErrorCode;
import com.autoyol.commons.web.ResponseData;
import com.autoyol.member.detail.api.MemberDetailFeignService;
import com.autoyol.member.detail.enums.MemberSelectKeyEnum;
import com.autoyol.member.detail.vo.res.*;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 封装对远程会员详情服务的相关调用
 * @author <a href="mailto:lianglin.sjtu@gmail.com">AndySjtu</a>
 * @date 2019/12/19 2:18 下午
 **/
@Service
public class MemProxyService {
    private final static Logger log = LoggerFactory.getLogger(MemProxyService.class);

    @Autowired
    private MemberDetailFeignService memberDetailFeignService;

    public OrderRenterInfoDTO getRenterInfoByMemNo(String memNo){
        List<String> selectKey = Arrays.asList(
                MemberSelectKeyEnum.MEMBER_CORE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_AUTH_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_BASE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_ROLE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_ADDITION_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_STATISTICS_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_RELIEF_INFO.getKey());
        ResponseData<MemberTotalInfo> responseData = null;
        log.info("Feign 开始获取租客会员信息,memNo={}",memNo);
        Transaction t = Cat.newTransaction(CatConstants.FEIGN_CALL, "会员详情服务");
        try{
            Cat.logEvent(CatConstants.FEIGN_METHOD,"MemberDetailFeignService.getMemberSelectInfo");
            String parameter = "memNo="+memNo+"&selectKey"+JSON.toJSONString(selectKey);
            Cat.logEvent(CatConstants.FEIGN_PARAM,parameter);
            responseData = memberDetailFeignService.getMemberSelectInfo(Integer.parseInt(memNo), selectKey);
            if(responseData == null || !ErrorCode.SUCCESS.getCode().equals(responseData.getResCode()) || responseData.getData() == null){
                log.error("Feign 获取租客会员信息失败,memNo={},responseData={}",memNo,JSON.toJSONString(responseData));
                RenterMemberFailException renterMemberByFeignException = new RenterMemberFailException();
                throw renterMemberByFeignException;
            }
            t.setStatus(Transaction.SUCCESS);
        }catch(RenterMemberFailException ex){
            t.setStatus(ex);
            Cat.logError("Feign 获取租客会员信息失败",ex);
            throw ex;
        }catch (Exception e){
            log.error("Feign 获取租客会员信息失败,submitReqDto={},memNo={}",memNo,null,e);
            RenterMemberErrException err = new RenterMemberErrException();
            Cat.logError("Feign 获取租客会员信息失败",err);
            t.setStatus(e);
            throw err;
        }finally {
            t.complete();
        }

        MemberTotalInfo memberTotalInfo = responseData.getData();
        MemberAuthInfo memberAuthInfo = memberTotalInfo.getMemberAuthInfo();
        MemberCoreInfo memberCoreInfo = memberTotalInfo.getMemberCoreInfo();
        MemberBaseInfo memberBaseInfo = memberTotalInfo.getMemberBaseInfo();
        MemberAdditionInfo memberAdditionInfo = memberTotalInfo.getMemberAdditionInfo();
        MemberStatisticsInfo memberStatisticsInfo = memberTotalInfo.getMemberStatisticsInfo();
        MemberRoleInfo memberRoleInfo = memberTotalInfo.getMemberRoleInfo();

        OrderRenterInfoDTO dto = new OrderRenterInfoDTO();
        dto.setMemNo(memNo);
        dto.setRealName(memberCoreInfo.getRealName());
        dto.setRenterPhone(String.valueOf(memberCoreInfo.getMobile()));
        dto.setEmail(memberAuthInfo.getEmail());
        dto.setGender(convertGender(memberBaseInfo.getGender()));
        dto.setDriLicRecordNo(memberAuthInfo.getDriLicRecordNo());
        dto.setIdNo(memberAuthInfo.getIdCard());
        dto.setCensusRegiste(memberBaseInfo.getCensusRegiste());
        dto.setCity(memberBaseInfo.getCity());
        dto.setProvince(memberBaseInfo.getProvince());
        dto.setInternalStaff(convertYesOrNo(memberRoleInfo.getInternalStaff()));
        dto.setCpicFlag(convertYesOrNo(memberRoleInfo.getCpicMemberFlag()));
//        dto.setLabelTagList(convertLabelList(memberRoleInfo.));
        dto.setAdditionalDrivers(memberAdditionInfo.getCommUseDriverList());
        dto.setRegTimeTxt(convertTime(memberCoreInfo.getRegTime()));
        dto.setDriveAge(memberAuthInfo.getDriLicFirstTime());//TODO
        dto.setBuyTimes(String.valueOf(memberStatisticsInfo.getSuccessOrderNum()));
        dto.setUpgrades("0");


        return dto;
    }

    private String convertTime(Date regTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(regTime);
    }

    private String convertYesOrNo(Integer internalStaff) {
        if(internalStaff!=null&&internalStaff==1){
            return "1";
        }
        return "0";
    }

    private String convertGender(Integer gender) {
        if(gender!=null&&gender==1){
            return "1";
        }
        return "2";
    }


    /**
     * 返回用户的会员号
     * @param mobile
     * @return
     * @throws RenterMemberFailException
     */
    public Integer getMemNoByMoile(String mobile)throws RenterMemberFailException{
        ResponseData<Integer> responseData = null;
        log.info("Feign 开始获取车主会员信息,mobile={}",mobile);
        Transaction t = Cat.newTransaction(CatConstants.FEIGN_CALL, "会员详情服务");
        try{
            Cat.logEvent(CatConstants.FEIGN_METHOD,"MemberDetailFeignService.getMemNoByMobile");
            String parameter = "mobile="+mobile;
            Cat.logEvent(CatConstants.FEIGN_PARAM,parameter);
            responseData = memberDetailFeignService.getMemNoByMobile(Long.parseLong(mobile));
            Cat.logEvent(CatConstants.FEIGN_RESULT,JSON.toJSONString(responseData));
            if(responseData == null || !ErrorCode.SUCCESS.getCode().equals(responseData.getResCode())){
                log.error("Feign 获取车主会员信息失败,mobile={},orderContextDto={}",mobile, JSON.toJSONString(responseData));
                OwnerMemberFailException failException = new OwnerMemberFailException();
                throw failException;
            }
            t.setStatus(Transaction.SUCCESS);
            return responseData.getData();
        }catch (OwnerMemberFailException oe){
            Cat.logError("Feign 获取车主会员信息失败",oe);
            t.setStatus(oe);
            throw oe;
        }
        catch (Exception e){
            t.setStatus(e);
            Cat.logError("Feign 获取车主会员信息失败",e);
            log.error("Feign 获取车主会员信息失败,orderContextDto={},mobile={}",mobile,e);
            throw new OwnerMemberErrException();
        }finally {
            t.complete();
        }
    }



    //获取车主会员信息
    public OwnerMemberDTO getOwnerMemberInfo(String memNo) throws RenterMemberFailException {
        List<String> selectKey = Arrays.asList(
                MemberSelectKeyEnum.MEMBER_CORE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_BASE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_ROLE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_ADDITION_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_STATISTICS_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_RELIEF_INFO.getKey());
        ResponseData<MemberTotalInfo> responseData = null;
        log.info("Feign 开始获取车主会员信息,memNo={}",memNo);
        Transaction t = Cat.newTransaction(CatConstants.FEIGN_CALL, "会员详情服务");
        try{
            Cat.logEvent(CatConstants.FEIGN_METHOD,"MemberDetailFeignService.getMemberSelectInfo");
            String parameter = "memNo="+memNo+"&selectKey"+ JSON.toJSONString(selectKey);
            Cat.logEvent(CatConstants.FEIGN_PARAM,parameter);
            responseData = memberDetailFeignService.getMemberSelectInfo(Integer.valueOf(memNo), selectKey);
            Cat.logEvent(CatConstants.FEIGN_RESULT,JSON.toJSONString(responseData));
            if(responseData == null || !ErrorCode.SUCCESS.getCode().equals(responseData.getResCode())){
                log.error("Feign 获取车主会员信息失败,memNo={},orderContextDto={}",memNo, JSON.toJSONString(responseData));
                OwnerMemberFailException failException = new OwnerMemberFailException();
                throw failException;
            }
            t.setStatus(Transaction.SUCCESS);
        }catch (OwnerMemberFailException oe){
            Cat.logError("Feign 获取车主会员信息失败",oe);
            t.setStatus(oe);
            throw oe;
        }
        catch (Exception e){
            t.setStatus(e);
            Cat.logError("Feign 获取车主会员信息失败",e);
            log.error("Feign 获取车主会员信息失败,orderContextDto={},memNo={}",memNo,e);
            throw new OwnerMemberErrException();
        }finally {
            t.complete();
        }

        MemberTotalInfo memberTotalInfo = responseData.getData();
        MemberCoreInfo memberCoreInfo = memberTotalInfo.getMemberCoreInfo();
        MemberStatisticsInfo memberStatisticsInfo = memberTotalInfo.getMemberStatisticsInfo();
        OwnerMemberDTO ownerMemberDto = new OwnerMemberDTO();
        ownerMemberDto.setMemNo(memNo);
        ownerMemberDto.setPhone(memberCoreInfo.getMobile()==null ? "" : String.valueOf(memberCoreInfo.getMobile()));
        ownerMemberDto.setHeaderUrl(memberCoreInfo.getPortraitPath());
        ownerMemberDto.setRealName(memberCoreInfo.getRealName());
        ownerMemberDto.setNickName(memberCoreInfo.getNickName());
        ownerMemberDto.setOrderSuccessCount(memberStatisticsInfo.getSuccessOrderNum());
        //ownerMemberDto.setMemType();
        List<OwnerMemberRightDTO> rights = new ArrayList<>();
        MemberRoleInfo memberRoleInfo = memberTotalInfo.getMemberRoleInfo();

        if(memberRoleInfo != null){
            if(memberRoleInfo.getInternalStaff()!=null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.STAFF.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.STAFF.getRightName());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getInternalStaff()));
                internalStaff.setRightType(RightTypeEnum.STAFF.getCode());
                internalStaff.setRightDesc("是否是内部员工");
                rights.add(internalStaff);
            }
            if(memberRoleInfo.getMemberFlag() != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                MemberFlagEnum memberFlagEnum = MemberFlagEnum.getRightByIndex(memberRoleInfo.getMemberFlag());
                internalStaff.setRightCode(memberFlagEnum.getRightCode());
                internalStaff.setRightName(memberFlagEnum.getRightName());
                internalStaff.setRightValue("1");
                internalStaff.setRightType(RightTypeEnum.MEMBER_FLAG.getCode());
                internalStaff.setRightDesc("会员标识");
                rights.add(internalStaff);
            }
            if(memberRoleInfo.getCpicMemberFlag() != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.CPIC_MEM.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.CPIC_MEM.getRightName());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getCpicMemberFlag()));
                internalStaff.setRightType(RightTypeEnum.CPIC.getCode());
                internalStaff.setRightDesc("是否太保会员");
                rights.add(internalStaff);
            }
        }
        MemberReliefInfo memberReliefInfo = memberTotalInfo.getMemberReliefInfo();
        if(memberReliefInfo != null){
            WxBindingTaskInfo wxBindingTaskInfo = memberReliefInfo.getWxBindingTaskInfo();
            if(wxBindingTaskInfo !=null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.BIND_WECHAT.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.BIND_WECHAT.getRightName());
                internalStaff.setRightValue(wxBindingTaskInfo.getReliefPercentage()==null?"0":String.valueOf(wxBindingTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(wxBindingTaskInfo.getTitle());
                internalStaff.setRightType(RightTypeEnum.TASK.getCode());
                rights.add(internalStaff);
            }
            MemberLevelTaskInfo memberLevelTaskInfo = memberReliefInfo.getMemberLevelTaskInfo();
            if(memberLevelTaskInfo != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.MEMBER_LEVEL.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.MEMBER_LEVEL.getRightName());
                internalStaff.setRightValue(memberLevelTaskInfo.getReliefPercentage()==null?"0":String.valueOf(memberLevelTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(memberLevelTaskInfo.getTitle());
                internalStaff.setRightType(RightTypeEnum.TASK.getCode());
                rights.add(internalStaff);
            }
            InvitationTaskInfo invitationTaskInfo = memberReliefInfo.getInvitationTaskInfo();
            if(invitationTaskInfo != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.INVITE_FRIENDS.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.INVITE_FRIENDS.getRightName());
                internalStaff.setRightValue(invitationTaskInfo.getReliefPercentage()==null?"0":String.valueOf(invitationTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(invitationTaskInfo.getTitle());
                internalStaff.setRightType(RightTypeEnum.TASK.getCode());
                rights.add(internalStaff);
            }
            RentCarTaskInfo rentCarTaskInfo = memberReliefInfo.getRentCarTaskInfo();
            if(rentCarTaskInfo != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.SUCCESS_RENTCAR.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.SUCCESS_RENTCAR.getRightName());
                internalStaff.setRightValue(rentCarTaskInfo.getReliefPercentage()==null?"0":String.valueOf(rentCarTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(rentCarTaskInfo.getTitle());
                internalStaff.setRightType(RightTypeEnum.TASK.getCode());
                rights.add(internalStaff);
            }
        }
        ownerMemberDto.setOwnerMemberRightDTOList(rights);
        return ownerMemberDto;
    }

    //获取租客会员信息
    public RenterMemberDTO getRenterMemberInfo(String memNo) throws RenterMemberFailException {
        List<String> selectKey = Arrays.asList(
                MemberSelectKeyEnum.MEMBER_CORE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_AUTH_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_BASE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_ROLE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_ADDITION_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_STATISTICS_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_RELIEF_INFO.getKey());
        ResponseData<MemberTotalInfo> responseData = null;
        log.info("Feign 开始获取租客会员信息,memNo={}",memNo);
        Transaction t = Cat.newTransaction(CatConstants.FEIGN_CALL, "会员详情服务");
        try{
            Cat.logEvent(CatConstants.FEIGN_METHOD,"MemberDetailFeignService.getMemberSelectInfo");
            String parameter = "memNo="+memNo+"&selectKey"+JSON.toJSONString(selectKey);
            Cat.logEvent(CatConstants.FEIGN_PARAM,parameter);
            responseData = memberDetailFeignService.getMemberSelectInfo(Integer.parseInt(memNo), selectKey);
            if(responseData == null || !ErrorCode.SUCCESS.getCode().equals(responseData.getResCode()) || responseData.getData() == null){
                log.error("Feign 获取租客会员信息失败,memNo={},responseData={}",memNo,JSON.toJSONString(responseData));
                RenterMemberFailException renterMemberByFeignException = new RenterMemberFailException();
                throw renterMemberByFeignException;
            }
            t.setStatus(Transaction.SUCCESS);
        }catch(RenterMemberFailException ex){
            t.setStatus(ex);
            Cat.logError("Feign 获取租客会员信息失败",ex);
            throw ex;
        }catch (Exception e){
            log.error("Feign 获取租客会员信息失败,submitReqDto={},memNo={}",memNo,null,e);
            RenterMemberErrException err = new RenterMemberErrException();
            Cat.logError("Feign 获取租客会员信息失败",err);
            t.setStatus(e);
            throw err;
        }finally {
            t.complete();
        }

        MemberTotalInfo memberTotalInfo = responseData.getData();
        MemberAuthInfo memberAuthInfo = memberTotalInfo.getMemberAuthInfo();
        MemberCoreInfo memberCoreInfo = memberTotalInfo.getMemberCoreInfo();
        MemberBaseInfo memberBaseInfo = memberTotalInfo.getMemberBaseInfo();
        MemberAdditionInfo memberAdditionInfo = memberTotalInfo.getMemberAdditionInfo();
        MemberStatisticsInfo memberStatisticsInfo = memberTotalInfo.getMemberStatisticsInfo();
        MemberRoleInfo memberRoleInfo = memberTotalInfo.getMemberRoleInfo();
        RenterMemberDTO renterMemberDto = new RenterMemberDTO();
        renterMemberDto.setMemNo(memNo);
        renterMemberDto.setPhone(memberCoreInfo.getMobile()==null ? "" : String.valueOf(memberCoreInfo.getMobile()));
        renterMemberDto.setHeaderUrl(memberCoreInfo.getPortraitPath());
        renterMemberDto.setRealName(memberCoreInfo.getRealName());
        renterMemberDto.setNickName(memberCoreInfo.getNickName());
        renterMemberDto.setCertificationTime(LocalDateTimeUtils.parseStringToLocalDate(memberAuthInfo.getDriLicFirstTime()));
        renterMemberDto.setRentFlag(memberCoreInfo.getRentFlag());
        renterMemberDto.setFirstName(memberBaseInfo.getFirstName());
        renterMemberDto.setGender(memberBaseInfo.getGender());
        renterMemberDto.setIdCardAuth(memberAuthInfo.getIdCardAuth());
        renterMemberDto.setDriLicAuth(memberAuthInfo.getDriLicAuth());
        renterMemberDto.setDriViceLicAuth(memberAuthInfo.getDriViceLicAuth());
        renterMemberDto.setOrderSuccessCount(memberStatisticsInfo.getSuccessOrderNum());
        List<CommUseDriverInfo> commUseDriverList = memberAdditionInfo.getCommUseDriverList();
        List<CommUseDriverInfoDTO> CommUseDriverList = new ArrayList<>();
        commUseDriverList.forEach(x->{
            CommUseDriverInfoDTO commUseDriverInfoDTO = new CommUseDriverInfoDTO();
            BeanUtils.copyProperties(x,commUseDriverInfoDTO);
            CommUseDriverList.add(commUseDriverInfoDTO);
        });
        renterMemberDto.setCommUseDriverList(CommUseDriverList);
        renterMemberDto.setIsNew(memberRoleInfo.getIsNew());
        renterMemberDto.setRenterCheck(memberAuthInfo.getRenterCheck());
        renterMemberDto.setRegTime(memberCoreInfo.getRegTime()==null ? null: LocalDateTimeUtils.dateToLocalDateTime(memberCoreInfo.getRegTime()));
        renterMemberDto.setOuterSource(memberBaseInfo.getOuterSource());

        List<RenterMemberRightDTO> rights = new ArrayList<>();

        if(memberRoleInfo != null){
            if(memberRoleInfo.getInternalStaff()!=null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.STAFF.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.STAFF.getRightName());
                internalStaff.setRightType(RightTypeEnum.STAFF.getCode());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getInternalStaff()));
                internalStaff.setRightDesc("是否是内部员工");
                rights.add(internalStaff);
            }
            if(memberRoleInfo.getMemberFlag() != null){
                MemberFlagEnum memberFlagEnum = MemberFlagEnum.getRightByIndex(memberRoleInfo.getMemberFlag());
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(memberFlagEnum.getRightCode());
                internalStaff.setRightName(memberFlagEnum.getRightName());
                internalStaff.setRightType(RightTypeEnum.MEMBER_FLAG.getCode());
                internalStaff.setRightValue("1");
                internalStaff.setRightDesc("会员标识");
                rights.add(internalStaff);
            }
            if(memberRoleInfo.getCpicMemberFlag() != null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.CPIC_MEM.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.CPIC_MEM.getRightName());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getCpicMemberFlag()));
                internalStaff.setRightType(RightTypeEnum.CPIC.getCode());
                internalStaff.setRightDesc("是否太保会员");
                rights.add(internalStaff);
            }
        }
        MemberReliefInfo memberReliefInfo = memberTotalInfo.getMemberReliefInfo();
        if(memberReliefInfo != null){
            WxBindingTaskInfo wxBindingTaskInfo = memberReliefInfo.getWxBindingTaskInfo();
            if(wxBindingTaskInfo !=null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.BIND_WECHAT.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.BIND_WECHAT.getRightName());
                internalStaff.setRightValue(wxBindingTaskInfo.getReliefPercentage()==null?"0":String.valueOf(wxBindingTaskInfo.getReliefPercentage()));
                internalStaff.setRightType(RightTypeEnum.TASK.getCode());
                internalStaff.setRightDesc(wxBindingTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            MemberLevelTaskInfo memberLevelTaskInfo = memberReliefInfo.getMemberLevelTaskInfo();
            if(memberLevelTaskInfo != null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.MEMBER_LEVEL.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.MEMBER_LEVEL.getRightName());
                internalStaff.setRightValue(memberLevelTaskInfo.getReliefPercentage()==null?"0":String.valueOf(memberLevelTaskInfo.getReliefPercentage()));
                internalStaff.setRightType(RightTypeEnum.TASK.getCode());
                internalStaff.setRightDesc(memberLevelTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            InvitationTaskInfo invitationTaskInfo = memberReliefInfo.getInvitationTaskInfo();
            if(invitationTaskInfo != null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.INVITE_FRIENDS.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.INVITE_FRIENDS.getRightName());
                internalStaff.setRightValue(invitationTaskInfo.getReliefPercentage()==null?"0":String.valueOf(invitationTaskInfo.getReliefPercentage()));
                internalStaff.setRightType(RightTypeEnum.TASK.getCode());
                internalStaff.setRightDesc(invitationTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            RentCarTaskInfo rentCarTaskInfo = memberReliefInfo.getRentCarTaskInfo();
            if(rentCarTaskInfo != null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.SUCCESS_RENTCAR.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.SUCCESS_RENTCAR.getRightName());
                internalStaff.setRightValue(rentCarTaskInfo.getReliefPercentage()==null?"0":String.valueOf(rentCarTaskInfo.getReliefPercentage()));
                internalStaff.setRightType(RightTypeEnum.TASK.getCode());
                internalStaff.setRightDesc(rentCarTaskInfo.getTitle());
                rights.add(internalStaff);
            }
        }
        renterMemberDto.setRenterMemberRightDTOList(rights);
        return renterMemberDto;
    }

    /*
     * @Author ZhangBin
     * @Date 2019/12/31 14:28
     * @Description: 根据会员号，获取常用驾驶人列表
     *
     **/
    public List<CommUseDriverInfoDTO> getCommUseDriverList(String memNo){
        List<String> selectKey = Arrays.asList(MemberSelectKeyEnum.MEMBER_ADDITION_INFO.getKey());
        ResponseData<MemberAdditionInfo> responseData = null;
        log.info("Feign 开始获取附加驾驶人信息,memNo={}",memNo);
        Transaction t = Cat.newTransaction(CatConstants.FEIGN_CALL, "附加驾驶人信息");
        try{
            Cat.logEvent(CatConstants.FEIGN_METHOD,"MemberDetailFeignService.getMemberSelectInfo");
            String parameter = "memNo="+memNo+"&selectKey"+JSON.toJSONString(selectKey);
            Cat.logEvent(CatConstants.FEIGN_PARAM,parameter);
            responseData = memberDetailFeignService.getMemberAdditionInfo(Integer.parseInt(memNo));
            if(responseData == null || !ErrorCode.SUCCESS.getCode().equals(responseData.getResCode()) || responseData.getData() == null){
                log.error("Feign 获取附加驾驶人信息失败,memNo={},responseData={}",memNo,JSON.toJSONString(responseData));
                RenterDriverFailException failException = new RenterDriverFailException();
                throw failException;
            }
            t.setStatus(Transaction.SUCCESS);
        }catch(RenterDriverFailException ex){
            t.setStatus(ex);
            Cat.logError("Feign 获取附加驾驶人信息失败",ex);
            throw ex;
        }catch (Exception e){
            log.error("Feign 获取附加驾驶人信息失败,submitReqDto={},memNo={}",memNo,null,e);
            RenterDriverErrException err = new RenterDriverErrException();
            Cat.logError("Feign 获取附加驾驶人信息失败",err);
            t.setStatus(e);
            throw err;
        }finally {
            t.complete();
        }
        MemberAdditionInfo memberAdditionInfo = responseData.getData();
        List<CommUseDriverInfo> commUseDriverList = memberAdditionInfo.getCommUseDriverList();
        List<CommUseDriverInfoDTO> CommUseDriverList = new ArrayList<>();
        commUseDriverList.forEach(x->{
            CommUseDriverInfoDTO commUseDriverInfoDTO = new CommUseDriverInfoDTO();
            BeanUtils.copyProperties(x,commUseDriverInfoDTO);
            CommUseDriverList.add(commUseDriverInfoDTO);
        });
        return CommUseDriverList;
    }
}
