package com.xin.xinChat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.GroupInfoMapper;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.UserContact;
import com.xin.xinChat.model.enums.UserContactEnum;
import com.xin.xinChat.model.enums.UserContactStatusEnum;
import com.xin.xinChat.service.GroupInfoService;
import com.xin.xinChat.service.UserContactService;
import com.xin.xinChat.utils.StringUtil;
import com.xin.xinChat.utils.SysSettingUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author 15712
 * @description 针对表【groupInfo】的数据库操作Service实现
 * @createDate 2024-06-20 20:45:56
 */
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo>
        implements GroupInfoService {

    @Resource
    private UserContactService userContactService;


    @Resource
    private SysSettingUtil sysSettingUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveGroup(GroupInfo groupInfo) {
        String groupId = groupInfo.getGroupId();
        String groupOwnerId = groupInfo.getGroupOwnerId();
        String groupAvatar = groupInfo.getGroupAvatar();
        //如果id为空表示新增
        if (StringUtils.isBlank(groupId)) {
            String groupIdStr = StringUtil.getGroupId();
            groupInfo.setGroupId(groupIdStr);
            QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("groupOwnerId", groupOwnerId);
            long count = this.count(queryWrapper);
            SysSettingDTO sysSetting = sysSettingUtil.getSysSetting();
            if (count > sysSetting.getMaxGroupCount()) {
                throw new BusinessException(ErrorCode.MAX_GROUP_COUNT_ERROR, "最多支持创建" + sysSetting.getMaxGroupCount() + "个群聊");
            }
            if (StringUtils.isBlank(groupAvatar)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "群聊头像不能为空");
            }
            boolean save = this.save(groupInfo);
            //将群聊设置为联系人
            UserContact userContact = new UserContact();
            //设置联系id
            userContact.setContactId(groupIdStr);
            //设置好友状态
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            //设置联系类型为群聊
            userContact.setContactType(UserContactEnum.GROUP.getType());
            userContact.setUserId(groupOwnerId);
            boolean savaUserContact = userContactService.save(userContact);
            //todo 创建会话
            //todo 发送消息
            if (save && savaUserContact) {
                return groupIdStr;
            }

        }else {
            GroupInfo oldGroupInfo = this.getById(groupId);
            if (!oldGroupInfo.getGroupOwnerId().equals(groupId)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            GroupInfo newGroupInfo = new GroupInfo();
            BeanUtil.copyProperties(groupInfo,newGroupInfo);
            boolean b = this.updateById(newGroupInfo);
            //todo 更新相关表冗余信息
            //todo 修改群名称发送ws消息
            if (b){
                return groupId;
            }
        }
        return null;
    }
}




