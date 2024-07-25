package com.xin.xinChat.controller;

import cn.hutool.core.io.FileUtil;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.config.AppConfig;
import com.xin.xinChat.constant.FileConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.manager.CosManager;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.dto.Message.MessageSendRequest;
import com.xin.xinChat.model.dto.file.UploadFileRequest;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.model.entity.ChatMessage;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.enums.FileUploadBizEnum;
import com.xin.xinChat.service.ChatMessageService;
import com.xin.xinChat.service.ChatSessionUserService;
import com.xin.xinChat.service.UserService;
import com.xin.xinChat.utils.DateUtils;
import com.xin.xinChat.utils.SysSettingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

import static com.xin.xinChat.constant.FileConstant.FILE_SIZE;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/23 20:32
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {
    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatSessionUserService chatSessionUserService;


    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private SysSettingUtil sysSettingUtil;


    @Resource
    private AppConfig appConfig;

    @PostMapping("/sendMessage")
    public BaseResponse<MessageSendDTO> sendMessage(@RequestBody MessageSendRequest messageSendRequest) {
        if (messageSendRequest == null) {
            return null;
        }
        String contactId = messageSendRequest.getContactId();
        String messageContent = messageSendRequest.getMessageContent();
        Integer messageType = messageSendRequest.getMessageType();
        Long fileSize = messageSendRequest.getFileSize();
        String fileName = messageSendRequest.getFileName();
        Integer fileType = messageSendRequest.getFileType();
        if (contactId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择你要发送的对象");
        }
        if (StringUtils.isBlank(messageContent)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发送消息不能为空");
        }
        if (messageContent.length() > 500) {
            messageContent = messageContent.substring(0, 500);
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileName(fileName);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileType(fileType);
        chatMessage.setMessageType(messageType);
        MessageSendDTO messageSendDTO = chatMessageService.saveMessage(chatMessage);
        return ResultUtils.success(messageSendDTO);
    }

    /**
     * 文件上传
     * 当我们向群组或者私聊发送文件时，需要先上传文件到 COS，然后获取到 COS 上传后的文件地址，再保存到数据库中。
     * 这个时候在数据库中的消息为0：发送中，保持完消息之后就调用这个接口上传图片，之后就对数据库中消息的发送状态进行修改
     *
     * @param multipartFile     文件
     * @param uploadFileRequest 上传请求
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest) {
        String biz = uploadFileRequest.getBiz();
        Long messageId = uploadFileRequest.getMessageId();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //文件校验
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser();
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        //判断消息是否存在
        ChatMessage chatMessage = chatMessageService.getById(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!chatMessage.getSendUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Long sendTime = chatMessage.getSendTime();
        //格式化到月份
        String update = DateUtils.format(new Date(sendTime), DateUtils.YYYYMM);
        String filepath = String.format("/%s/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), update, filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            String fileUrl = FileConstant.COS_HOST + filepath;
            chatMessageService.saveFile(chatMessage, loginUser.getId(), messageId, fileUrl);
            return ResultUtils.success(fileUrl);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        SysSettingDTO sysSetting = sysSettingUtil.getSysSetting();
        Integer maxUserAvatarSize = sysSetting.getMaxUserAvatarSize();
        Integer maxImageSize = sysSetting.getMaxImageSize();
        Integer maxVideoSize = sysSetting.getMaxVideoSize();
        Integer maxFileSize = sysSetting.getMaxFileSize();
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > maxUserAvatarSize * FILE_SIZE) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像大小不能超过" + maxUserAvatarSize + "M");
            }
            if (!Arrays.asList(FileConstant.IMAGE_FILE_EXTENSION).contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        } else if (FileUploadBizEnum.PICTURE.equals(fileUploadBizEnum)) {
            if (fileSize > maxImageSize * FILE_SIZE) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过" + maxImageSize + "M");
            }
            if (!Arrays.asList(FileConstant.IMAGE_FILE_EXTENSION).contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        } else if (FileUploadBizEnum.VIDEO.equals(fileUploadBizEnum)) {
            if (fileSize > maxVideoSize * FILE_SIZE) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频大小不能超过" + maxVideoSize + "M");
            }
            if (!Arrays.asList(FileConstant.VIDEO_FILE_EXTENSION).contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        } else {
            if (fileSize > maxFileSize * FILE_SIZE) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小" + maxVideoSize + "M");
            }
        }
    }

}