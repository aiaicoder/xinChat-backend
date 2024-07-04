package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UsercontactMapper;
import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserContact;
import com.xin.xinChat.model.entity.UserContactApply;
import com.xin.xinChat.model.enums.*;
import com.xin.xinChat.model.vo.UserSearchVo;
import com.xin.xinChat.service.GroupInfoService;
import com.xin.xinChat.service.UserContactApplyService;
import com.xin.xinChat.service.UserContactService;
import com.xin.xinChat.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author 15712
 * @description 针对表【userContact(联系人表)】的数据库操作Service实现
 * @createDate 2024-06-20 20:46:55
 */
@Service
public class UserContactServiceImpl extends ServiceImpl<UsercontactMapper, UserContact>
        implements UserContactService {
    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private GroupInfoService groupInfoService;

    @Resource
    private UserContactApplyService userContactApplyService;


    @Override
    public UserSearchVo search(String userId, String contactId) {
        UserContactEnum enumByPrefix = UserContactEnum.getEnumByPrefix(contactId);
        if (enumByPrefix == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索类型错误");
        }
        UserSearchVo userSearchVo = new UserSearchVo();
        switch (enumByPrefix) {
            case USER:
                User user = userService.getById(contactId);
                if (user == null) {
                    return null;
                }
                userSearchVo.setNickName(user.getUserName());
                userSearchVo.setSex(user.getSex() == 1 ? "男" : "女");
                userSearchVo.setAreaName(user.getAreaName());
                System.out.println(userSearchVo);
                break;
            case GROUP:
                GroupInfo groupInfo = groupInfoService.getById(contactId);
                if (groupInfo == null || groupInfo.getStatus().equals(GroupInfoEnum.DELETE.getStatus())) {
                    return null;
                }
                userSearchVo.setNickName(groupInfo.getGroupName());
                break;
        }
        userSearchVo.setContactId(contactId);
        //设置联系人或者群组类型
        userSearchVo.setContactType(enumByPrefix.toString());
        //如果是搜索自己那么就直接设置为朋友
        if (userId.equals(contactId)) {
            userSearchVo.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return userSearchVo;
        }
        //查询是否是好友
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("contactId", contactId);
        UserContact userContact = getOne(queryWrapper);
        userSearchVo.setStatus(userContact == null ? null : userContact.getStatus());
        return userSearchVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(String applyInfo, String contactId) {
        User loginUser = userService.getLoginUser();
        if (StringUtils.isBlank(applyInfo)){
            //默认申请信息
            applyInfo = String.format(UserConstant.DEFAULT_APPLY_INFO,loginUser.getUserName());
        }
        UserContactEnum typeEnum = UserContactEnum.getEnumByPrefix(contactId);
        if (typeEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
        }
        //申请人id
        String userApplyId = loginUser.getId();
        //接受者id
        String receiveId = contactId;

        //是直接添加还是需要审核
        Integer joinType = null;

        //申请时间
        Long applyTime = System.currentTimeMillis();


        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userApplyId);
        queryWrapper.eq("contactId", contactId);
        UserContact userContact = this.getOne(queryWrapper);
        if (userContact !=null && userContact.getStatus().equals(UserContactStatusEnum.BLACKLIST_BE.getStatus())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"你已被拉黑，暂时无法添加");
        }
        if (UserContactEnum.GROUP == typeEnum){
            GroupInfo groupInfo = groupInfoService.getById(contactId);
            if (groupInfo == null || groupInfo.getStatus().equals(GroupInfoEnum.DELETE.getStatus())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"群聊不存在或已解散");
            }
            receiveId = groupInfo.getGroupOwnerId();
            joinType = groupInfo.getJoinType();
        }else {
            //对方信息
            User user = userService.getById(contactId);
            if (user == null){
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
            }
            joinType = user.getJoinType();
            if (JoinTypeEnum.JOIN.getType().equals(joinType)){
                //todo 添加联系人
                return joinType;
            }
            //查询申请记录是否存在
            QueryWrapper<UserContactApply> userContactApplyQueryWrapper = new QueryWrapper<>();
            userContactApplyQueryWrapper.eq("applyUserId",userApplyId);
            userContactApplyQueryWrapper.eq("receiveUserId",receiveId);
            UserContactApply contactApply = userContactApplyService.getOne(userContactApplyQueryWrapper);
            if (contactApply == null){
                UserContactApply userContactApply = new UserContactApply();
                userContactApply.setApplyUserId(userApplyId);
                userContactApply.setReceiveUserId(receiveId);
                userContactApply.setApplyInfo(applyInfo);
                userContactApply.setContactType(typeEnum.getType());
                userContactApply.setLastApplyTime(applyTime);
                userContactApply.setContactId(contactId);
                userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
                userContactApplyService.save(userContactApply);
            }else {
                //直接更新即可
                UserContactApply userContactApply = new UserContactApply();
                userContactApply.setApplyInfo(applyInfo);
                userContactApply.setLastApplyTime(applyTime);
                userContactApply.setApplyId(contactApply.getApplyId());
                userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
                boolean b = userContactApplyService.updateById(userContactApply);
                if (!b){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"添加失败");
                }
            }
            if (contactApply == null || contactApply.getStatus().equals(UserContactApplyStatusEnum.INIT.getStatus())){
                //todo 发送ws消息，让用户知晓
            }
            return joinType;
        }

        return null;
    }
}




