package com.xin.xinChat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.enums.MessageTypeEnum;

/**
* @author 15712
* @description 针对表【groupInfo】的数据库操作Service
* @createDate 2024-06-20 20:45:56
*/
public interface GroupInfoService extends IService<GroupInfo> {

    String saveGroup(GroupInfo groupInfo);

    void dismissGroup(String groupOwnerId, String groupId);

    void addOrRemoveGroupMember(User loginUser, String groupId, String selectContactId, Integer opType);

    void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);
}
