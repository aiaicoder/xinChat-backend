package com.xin.xinChat.model.dto.Message;
import lombok.Data;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/23 20:37
 */
@Data
public class MessageSendRequest {
    //联系人id
    private String contactId;
    //消息内容
    private String messageContent;
    //消息类型
    private Integer messageType;
    //文件大小
    private Long fileSize;
    //文件名
    private String fileName;
    //文件类型
    private Integer fileType;
}
