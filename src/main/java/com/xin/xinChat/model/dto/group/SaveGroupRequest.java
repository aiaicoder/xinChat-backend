package com.xin.xinChat.model.dto.group;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/23 19:24
 */
@Data
public class SaveGroupRequest implements Serializable {

    private static final long serialVersionUID = 3251327532987394979L;

    /**
     * 群组id
     */
    private String groupId;

    /**
     * 群组名称
     */
    private String groupName;

    /**
     * 群公告
     */
    private String groupNotice;

    /**
     * 加入群状态
     */
    private Integer joinType;
}
