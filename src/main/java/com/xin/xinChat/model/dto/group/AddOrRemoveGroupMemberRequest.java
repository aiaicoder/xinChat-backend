package com.xin.xinChat.model.dto.group;

import lombok.Data;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/28 20:55
 */
@Data
public class AddOrRemoveGroupMemberRequest {

    /**
     * 群组id
     */
    private String groupId;
    /**
     * 群组成员id
     */
    private String selectContactIds;

    /**
     * 操作类型
     */
    private Integer opType;
}
