package com.xin.xinChat.model.dto.Message;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/17 20:43
 */
@Data
public class MessageSendDTO<T> implements Serializable {

    private static final long serialVersionUID = -8385732700714377982L;

    //消息ID
    private Long messageId;
    //会话ID
    private String sessionId;
    //发送人Id
    private String sendUserId;
    //发送人名称
    private String sendUserName;
    //联系人Id
    private String contactId;
    //联系人姓名
    private String contactName;
    //消息内容
    private String messageContent;
    //最后消息
    private String lastMessage;
    //发送类型
    private Integer messageType;
    //发送时间
    private Long sendTime;
    //联系人类型
    private Integer contactType;
    //扩展信息
    private T extendData;
    //消息状态 0：发送中 1：已发送 对于文件是异步上传用状态处理
    private Integer status;
    //成员数量
    private long memberCount;
    //文件信息
    private Long fileSize;
    private String fileName;
    private Integer fileType;

    public String getLastMessage() {
        if (StringUtils.isEmpty(lastMessage)){
            return messageContent;
        }
        return lastMessage;
    }

}
