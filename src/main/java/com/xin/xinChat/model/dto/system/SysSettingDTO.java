package com.xin.xinChat.model.dto.system;

import com.xin.xinChat.constant.UserConstant;
import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/15 20:05
 */

@Data
public class SysSettingDTO implements Serializable {

    private static final long serialVersionUID = 421543029156924860L;
    /**
     * 最大群聊数量
     */
    private Integer maxGroupCount = 5;

    /**
     * 最大群聊人数
     */
    private Integer maxGroupMemberCount = 500;


    /**
     * 最大图片大小
     */
    private Integer maxImageSize = 6;

    /**
     * 用户图片大小
     */
    private Integer maxUserAvatarSize = 5;

    /**
     * 最大视频大小
     */
    private Integer maxVideoSize = 20;

    /**
     * 最大文件大小
     */
    private Integer maxFileSize = 30;

    /**
     * 机器人uid
     */
    private String robotUid = UserConstant.ROBOT_UID;

    /**
     * 机器人头像
     */
    private String robotAvatar ="https://img2.baidu.com/it/u=3388033680,62498000&fm=253&fmt=auto&app=138&f=JPEG?w=200&h=200";

    /**
     * 机器人昵称
     */
    private String robotNickName ="XinChat";

    /**
     * 机器人欢迎语
     */
    private String robotWelcome ="欢迎使用XinChat";
}
