package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UsercontactMapper;
import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserContact;
import com.xin.xinChat.model.enums.UserContactEnum;
import com.xin.xinChat.model.enums.UserContactStatusEnum;
import com.xin.xinChat.model.vo.UserSearchVo;
import com.xin.xinChat.service.GroupInfoService;
import com.xin.xinChat.service.UserContactService;
import com.xin.xinChat.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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
                if (groupInfo == null) {
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
        UserContact userContact = getById(queryWrapper);
        userSearchVo.setStatus(userContact == null ? null : userContact.getStatus());
        return userSearchVo;
    }
}




