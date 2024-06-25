package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.model.dto.group.SaveGroupRequest;
import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.service.GroupInfoService;
import com.xin.xinChat.service.UserService;
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
    private UserService userService;

    @PostMapping("/saveGroup")
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

    @PostMapping("/loadMyGroup")
    @SaCheckLogin
    public BaseResponse<List<GroupInfo>> loadMyGroup() {
        User loginUser = userService.getLoginUser();
        QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
        groupInfoQueryWrapper.eq("groupOwnerId", loginUser.getId());
        groupInfoQueryWrapper.orderByDesc("createTime");
        return ResultUtils.success(groupinfoService.list(groupInfoQueryWrapper));
    }

}
