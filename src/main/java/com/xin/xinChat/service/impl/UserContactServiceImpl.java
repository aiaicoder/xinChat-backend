package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UserContactMapper;
import com.xin.xinChat.model.entity.UserContact;
import com.xin.xinChat.model.enums.UserContactStatusEnum;
import com.xin.xinChat.service.UserContactService;
import com.xin.xinChat.utils.RedisUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author 15712
 * @description 针对表【userContact(联系人表)】的数据库操作Service实现
 * @createDate 2024-06-20 20:46:55
 */
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
        implements UserContactService {

    @Resource
    private RedisUtils redisUtils;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delContact(String userId, String contactId, Integer status) {
        //查看是否是好友，或者是否是联系人
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("userId", userId);
        userContactQueryWrapper.eq("contactId", contactId);
        UserContact olduserContact = getBaseMapper().selectOne(userContactQueryWrapper);
        if (olduserContact == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该联系人不存在");
        }
        //获取操作状态枚举
        UserContactStatusEnum userContactStatusEnum = UserContactStatusEnum.getEnumByStatus(status);
        if (userContactStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "操作状态错误");
        }
        //移除好友
        UpdateWrapper<UserContact> userContactUpdateWrapper = new UpdateWrapper<>();
        userContactUpdateWrapper.eq("userId", userId);
        userContactUpdateWrapper.eq("contactId", contactId);
        userContactUpdateWrapper.set("status", status);
        boolean myContact = update(userContactUpdateWrapper);
        UpdateWrapper<UserContact> friendContactUpdateWrapper = new UpdateWrapper<>();
        //移除对方好友关系
        if (UserContactStatusEnum.DEL.equals(userContactStatusEnum)) {
            //对方删除，这边就是被删除
            friendContactUpdateWrapper.set("status", UserContactStatusEnum.DEL_BE.getStatus());
        } else if (UserContactStatusEnum.BLACKLIST.equals(userContactStatusEnum)) {
            friendContactUpdateWrapper.set("status", UserContactStatusEnum.BLACKLIST_BE.getStatus());
        }
        friendContactUpdateWrapper.eq("userId", contactId);
        friendContactUpdateWrapper.eq("contactId", userId);
        boolean friendContact = update(friendContactUpdateWrapper);
        //从我的好友列表缓存中删除对方
        redisUtils.delUserContactInfo(userId,contactId);
        //从对方好友缓存列表中删除自己
        redisUtils.delUserContactInfo(contactId,userId);
        return friendContact && myContact;
    }
}




