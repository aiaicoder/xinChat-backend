package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UserContactApplyMapper;
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
import com.xin.xinChat.utils.SysSettingUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 15712
 * @description 针对表【userContactApply(联系人申请)】的数据库操作Service实现
 * @createDate 2024-06-20 20:47:02
 */
@Service
public class UsercontactapplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
        implements UserContactApplyService {

    @Resource
    private UserService userService;

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private UserContactService userContactService;


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
                if (groupInfo == null || groupInfo.getStatus().equals(GroupInfoEnum.DISMISSAL.getStatus())) {
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
        UserContact userContact = userContactService.getOne(queryWrapper);
        userSearchVo.setStatus(userContact == null ? null : userContact.getStatus());
        return userSearchVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(String applyInfo, String contactId) {
        User loginUser = userService.getLoginUser();
        if (StringUtils.isBlank(applyInfo)) {
            //默认申请信息
            applyInfo = String.format(UserConstant.DEFAULT_APPLY_INFO, loginUser.getUserName());
        }
        UserContactEnum typeEnum = UserContactEnum.getEnumByPrefix(contactId);
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
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
        UserContact userContact = userContactService.getOne(queryWrapper);
        if (userContact != null && ArrayUtils.contains(
                new Integer[]{UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus(),
                        UserContactStatusEnum.BLACKLIST_BE.getStatus()},
                userContact.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "你已被拉黑，暂时无法添加");
        }
        if (UserContactEnum.GROUP == typeEnum) {
            GroupInfo groupInfo = groupInfoService.getById(contactId);
            if (groupInfo == null || groupInfo.getStatus().equals(GroupInfoEnum.DISMISSAL.getStatus())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "群聊不存在或已解散");
            }
            receiveId = groupInfo.getGroupOwnerId();
            joinType = groupInfo.getJoinType();
        } else {
            //对方信息
            User user = userService.getById(contactId);
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }
            joinType = user.getJoinType();
            if (JoinTypeEnum.JOIN.getType().equals(joinType)) {
                //todo 添加联系人
                return joinType;
            }
        }
        //查询申请记录是否存在
        QueryWrapper<UserContactApply> userContactApplyQueryWrapper = new QueryWrapper<>();
        userContactApplyQueryWrapper.eq("applyUserId", userApplyId);
        userContactApplyQueryWrapper.eq("receiveUserId", receiveId);
        UserContactApply contactApply = getOne(userContactApplyQueryWrapper);
        if (contactApply == null) {
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyUserId(userApplyId);
            userContactApply.setReceiveUserId(receiveId);
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setContactType(typeEnum.getType());
            userContactApply.setLastApplyTime(applyTime);
            userContactApply.setContactId(contactId);
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            save(userContactApply);
        } else {
            //直接更新即可
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setLastApplyTime(applyTime);
            userContactApply.setApplyId(contactApply.getApplyId());
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            boolean b = updateById(userContactApply);
            if (!b) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加失败");
            }
        }
        if (contactApply == null || contactApply.getStatus().equals(UserContactApplyStatusEnum.INIT.getStatus())) {
            //todo 发送ws消息，让用户知晓
        }
        return joinType;
    }


    @Override
    public boolean dealWithApply(Integer applyId, Integer status) {
        User loginUser = userService.getLoginUser();
        if (applyId == null || status == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        UserContactApplyStatusEnum typeEnum = UserContactApplyStatusEnum.getEnumByStatus(status);
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        UserContactApply contactApply = getById(applyId);
        if (contactApply == null || status.equals(UserContactApplyStatusEnum.INIT.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友申请不存在或申请状态有误");
        }
        if (!contactApply.getReceiveUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "处理失败");
        }
        //更新申请状态
        UserContactApply updateContactApply = new UserContactApply();
        updateContactApply.setApplyId(applyId);
        updateContactApply.setStatus(status);
        updateContactApply.setLastApplyTime(System.currentTimeMillis());
        UpdateWrapper<UserContactApply> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("status", UserContactApplyStatusEnum.INIT.getStatus());
        boolean update = this.update(updateContactApply, updateWrapper);
        if (!update) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理失败");
        }
        if (status.equals(UserContactApplyStatusEnum.AGREE.getStatus())) {
            addContact(contactApply.getApplyUserId(),
                    contactApply.getReceiveUserId(),
                    contactApply.getContactId(),
                    contactApply.getContactType(),
                    contactApply.getContactName());
            return true;
        }
        if (UserContactApplyStatusEnum.BLACKLIST == typeEnum) {
            //被拉黑后更新userContact表先查询是否有对应记录
            String applyUserId = contactApply.getApplyUserId();
            String contactId = contactApply.getContactId();
            Integer contactType = contactApply.getContactType();
            QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
            userContactQueryWrapper.eq("userId", applyUserId);
            userContactQueryWrapper.eq("contactId", contactId);
            UserContact userContact = new UserContact();
            //有就执行更新操作
            if (userContactService.getOne(userContactQueryWrapper) != null) {
                //更新
                userContact.setUserId(applyUserId);
                userContact.setContactId(contactId);
                userContact.setContactType(contactType);
                userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
                userContactService.updateById(userContact);
            } else {
                //添加
                userContact.setUserId(applyUserId);
                userContact.setContactId(contactId);
                userContact.setContactType(contactType);
                userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
                userContactService.save(userContact);
            }
            return true;
        }
        return false;

    }

    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String contactName) {
        //如果是添加的是群，那么要先判断群是否已满
        if (UserContactEnum.GROUP.getType().equals(contactType)) {
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("contactId", contactId);
            queryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            long count = userContactService.count(queryWrapper);
            //获取后台默认系统设置
            SysSettingUtil sysSettingUtil = new SysSettingUtil();
            if (count >= sysSettingUtil.getSysSetting().getMaxGroupCount()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "群已满");
            }
        }
        //同意双方都添加好友
        List<UserContact> userContactList = new ArrayList<>();
        //添加人添加好友
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(receiveUserId);
        userContact.setContactType(contactType);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContactList.add(userContact);
        //如果加入的不是群聊，而是申请人，那么被申请人也要同时添加上好友，群聊则不需要
        if (UserContactEnum.USER.getType().equals(contactType)){
            UserContact userContactReceive = new UserContact();
            userContactReceive.setUserId(receiveUserId);
            userContactReceive.setContactId(applyUserId);
            userContactReceive.setContactType(contactType);
            userContactReceive.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContactList.add(userContactReceive);
        }
        //批量插入
        userContactService.saveBatch(userContactList);
        //todo 如果是好友，接受人也添加申请人为好友 添加缓存

        //todo 创建会话 发送消息
    }
}




