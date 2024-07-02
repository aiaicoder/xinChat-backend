package com.xin.xinChat.model.dto.group;

import lombok.Data;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/29 20:27
 */
@Data
public class GroupInfoQueryRequest {
    String groupId;
    String groupName;
    String userId;
}
