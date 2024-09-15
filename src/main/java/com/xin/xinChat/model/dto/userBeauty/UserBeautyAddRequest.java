package com.xin.xinChat.model.dto.userBeauty;

import lombok.Data;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/11 20:52
 */
@Data
public class UserBeautyAddRequest {

    private Integer id;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 0：未使用 1：已使用
     */
    private Integer status;

    /**
     * 用户邮箱
     */
    private String email;
}
