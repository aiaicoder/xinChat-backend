package com.xin.xinChat.model.vo;

import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.UserContact;
import lombok.Data;

import java.util.List;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/2 19:29
 */
@Data
public class GroupInfoVo {
    /**
     * 群信息
     */
    private GroupInfo groupInfo;
    /**
     * 群成员
     */
    private List<UserContact> userContactList;

}
