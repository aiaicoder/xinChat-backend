package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.model.dto.group.AddOrRemoveGroupMemberRequest;
import com.xin.xinChat.model.dto.group.GroupInfoQueryRequest;
import com.xin.xinChat.model.dto.group.SaveGroupRequest;
import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserContact;
import com.xin.xinChat.model.enums.GroupInfoEnum;
import com.xin.xinChat.model.enums.MessageTypeEnum;
import com.xin.xinChat.model.enums.UserContactStatusEnum;
import com.xin.xinChat.model.vo.GroupInfoVo;
import com.xin.xinChat.service.GroupInfoService;
import com.xin.xinChat.service.UserContactService;
import com.xin.xinChat.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/23 19:19
 */
@RestController
@RequestMapping("/group")
public class GroupInfoController {

    @Resource
    private GroupInfoService groupinfoService;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserService userService;

    @PostMapping("/saveGroup")
    @ApiOperation("创建或者修改群组")
    @SaCheckLogin
    public BaseResponse<String> saveGroup(@RequestBody SaveGroupRequest saveGroupRequest) {
        String groupOwnerId = userService.getLoginUser().getId();
        String groupId = saveGroupRequest.getGroupId();
        String groupName = saveGroupRequest.getGroupName();
        String groupNotice = saveGroupRequest.getGroupNotice();
        Integer joinType = saveGroupRequest.getJoinType();
        String groupAvatar = saveGroupRequest.getGroupAvatar();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);
        groupInfo.setGroupAvatar(groupAvatar);
        groupInfo.setGroupOwnerId(groupOwnerId);
        String groupIdStr = groupinfoService.saveGroup(groupInfo);
        return ResultUtils.success(groupIdStr);
    }

    /**
     * 加载自己创建的群聊
     * @return
     */
    @PostMapping("/loadMyGroup")
    @ApiOperation("获取自己的群聊")
    @SaCheckLogin
    public BaseResponse<List<GroupInfo>> loadMyGroup() {
        User loginUser = userService.getLoginUser();
        QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
        groupInfoQueryWrapper.eq("groupOwnerId", loginUser.getId());
        groupInfoQueryWrapper.eq("status", GroupInfoEnum.NORMAL.getStatus());
        groupInfoQueryWrapper.orderByDesc("createTime");
        return ResultUtils.success(groupinfoService.list(groupInfoQueryWrapper));
    }


    @PostMapping("/getGroupInfo")
    @ApiOperation("获得群聊信息")
    @SaCheckLogin
    public BaseResponse<GroupInfo> getGroupInfo(@RequestBody GroupInfoQueryRequest groupInfoQueryRequest) {
        if (groupInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser();
        String groupId = groupInfoQueryRequest.getGroupId();
        GroupInfo groupInfo = getDetailGroupInfo(loginUser, groupId);
        QueryWrapper<UserContact> groupInfoQueryWrapper = new QueryWrapper<>();
        groupInfoQueryWrapper.eq("contactId", groupId);
        groupInfoQueryWrapper.eq("status", GroupInfoEnum.NORMAL.getStatus());
        long count = userContactService.count(groupInfoQueryWrapper);
        groupInfo.setMemberCount(count);
        return ResultUtils.success(groupInfo);
    }

    /**
     * 获取聊天会话群聊详情，查群组中的成员
     * @param groupInfoQueryRequest
     * @return
     */
    @PostMapping("/getGroupInfoChat")
    @SaCheckLogin
    @ApiOperation("获得群聊信息")
    public BaseResponse<GroupInfoVo> getGroupInfoChat(@RequestBody GroupInfoQueryRequest groupInfoQueryRequest) {
        if (groupInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "参数为空");
        }
        String groupId = groupInfoQueryRequest.getGroupId();
        User loginUser = userService.getLoginUser();
        GroupInfo detailGroupInfo = getDetailGroupInfo(loginUser, groupId);
        QueryWrapper<UserContact> groupInfoQueryWrapper = new QueryWrapper<>();
        //查询相关群聊
        groupInfoQueryWrapper.eq("contactId", groupId);
        groupInfoQueryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
        groupInfoQueryWrapper.orderByAsc("createTime");
        List<UserContact> userContacts = userContactService.list(groupInfoQueryWrapper);
        //这里进行联表查询，查询到对应的群成员
        userContacts.forEach(userContact -> {
            User user = userService.getById(userContact.getUserId());
            userContact.setContactName(user.getUserName());
            userContact.setSex(user.getSex());
        });
        GroupInfoVo groupInfoVo = new GroupInfoVo();
        //组装，返回前端
        groupInfoVo.setGroupInfo(detailGroupInfo);
        groupInfoVo.setUserContactList(userContacts);
        return ResultUtils.success(groupInfoVo);
    }

    @PostMapping("/addOrRemoveGroupMember")
    @ApiOperation("添加或者移除群聊成员")
    @SaCheckLogin
    public BaseResponse<String> addOrRemoveGroupMember(@RequestBody AddOrRemoveGroupMemberRequest addOrRemoveGroupMemberRequest) {
        User loginUser = userService.getLoginUser();
        if (addOrRemoveGroupMemberRequest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "参数为空");
        }
        String groupId = addOrRemoveGroupMemberRequest.getGroupId();
        String selectContactIds = addOrRemoveGroupMemberRequest.getSelectContactIds();
        Integer opType = addOrRemoveGroupMemberRequest.getOpType();
        groupinfoService.addOrRemoveGroupMember(loginUser, groupId, selectContactIds, opType);
        return ResultUtils.success("操作成功");
    }


    @PostMapping("/leaveGroup")
    @ApiOperation("退出群聊")
    @SaCheckLogin
    public BaseResponse<String> leaveGroup(String groupId) {
        User loginUser = userService.getLoginUser();
        String userId = loginUser.getId();
        if (groupId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "参数为空");
        }
        groupinfoService.leaveGroup(userId, groupId, MessageTypeEnum.LEAVE_GROUP);
        return ResultUtils.success("已退出群聊");
    }


    @PostMapping("/dismissGroup")
    @ApiOperation("解散群聊")
    @SaCheckLogin
    public BaseResponse<String> dismissGroup(String groupId) {
        User loginUser = userService.getLoginUser();
        String userId = loginUser.getId();
        if (groupId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "参数为空");
        }
        groupinfoService.dismissGroup(userId, groupId);
        return ResultUtils.success("已解散群聊");
    }


    private GroupInfo getDetailGroupInfo(User loginUser, String groupId) {
        QueryWrapper<UserContact> groupInfoQueryWrapper = new QueryWrapper<>();
        groupInfoQueryWrapper.eq("userId", loginUser.getId());
        groupInfoQueryWrapper.eq("contactId", groupId);
        UserContact userContact = userContactService.getOne(groupInfoQueryWrapper);
        if (userContact == null || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "查找群组不存在或你不在群聊");
        }
        GroupInfo groupInfo = groupinfoService.getById(groupId);
        groupInfo.setContactStatus(userContact.getStatus());
        if (groupInfo == null || !groupInfo.getStatus().equals(GroupInfoEnum.NORMAL.getStatus())){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "群组不存在或已解散");
        }
        return groupInfo;
    }

}
