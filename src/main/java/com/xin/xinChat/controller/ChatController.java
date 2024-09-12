package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.config.AppConfig;
import com.xin.xinChat.constant.FileConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.manager.CosManager;
import com.xin.xinChat.model.dto.Message.MessageBaseRequest;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.dto.Message.MessageSendRequest;
import com.xin.xinChat.model.dto.file.UploadFileRequest;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.model.entity.ChatMessage;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.enums.FileUploadBizEnum;
import com.xin.xinChat.service.ChatMessageService;
import com.xin.xinChat.service.UserService;
import com.xin.xinChat.utils.DateUtils;
import com.xin.xinChat.utils.RedisUtils;
import com.xin.xinChat.utils.SysSettingUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xin.xinChat.constant.FileConstant.FILE_SIZE;
import static com.xin.xinChat.constant.RedisKeyConstant.REDIS_USER_UPLOAD_FILE_EXPIRE_TIME;
import static com.xin.xinChat.constant.RedisKeyConstant.REDIS_USER_UPLOAD_FILE_KEY;

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
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private SysSettingUtil sysSettingUtil;

    @Resource
    private RedisUtils redisUtils;


    @Resource
    private AppConfig appConfig;

    @PostMapping("/msg/sendMessage")
    @ApiOperation("发送消息")
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
        User loginUser = userService.getLoginUser();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileName(fileName);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileType(fileType);
        chatMessage.setMessageType(messageType);
        chatMessage.setSendUserAvatar(loginUser.getUserAvatar());
        MessageSendDTO messageSendDTO = chatMessageService.saveMessage(loginUser,chatMessage);
        return ResultUtils.success(messageSendDTO);
    }

    @PostMapping("/msg/recall")
    @ApiOperation("撤回消息")
    public BaseResponse<Boolean> recallMessage(@RequestBody MessageBaseRequest messageBaseRequest) {
        Long messageId = messageBaseRequest.getMessageId();
        chatMessageService.recallMessage(messageId);
        return ResultUtils.success(true);
    }

    /**
     * 文件上传
     * 当我们向群组或者私聊发送文件时，需要先上传文件到 COS，然后获取到 COS 上传后的文件地址，再保存到数据库中。
     * 这个时候在数据库中的消息为0：发送中，保存完消息之后就调用这个接口上传图片，之后就对数据库中消息的发送状态进行修改
     *
     * @param multipartFile     文件
     * @param uploadFileRequest 上传请求
     * @return
     */
    @PostMapping("/msg/upload")
    @ApiOperation("发送文件")
    public BaseResponse<Map<String, String>> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                                        UploadFileRequest uploadFileRequest) {
        User loginUser = userService.getLoginUser();
        String biz = uploadFileRequest.getBiz();
        Long messageId = uploadFileRequest.getMessageId();
        //判断消息是否存在
        ChatMessage chatMessage = chatMessageService.getById(messageId);
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        //获取文件类型后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        Map<String, String> result = new HashMap<>();
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验文件md5值防止重复上传
        String md5;
        try {
            md5 = DigestUtil.md5Hex(multipartFile.getBytes());
            String fileUrl = redisUtils.get(REDIS_USER_UPLOAD_FILE_KEY + loginUser.getId() + ":" + md5);
            //如果是上传的是视频那么会有视频的封面信息，通过逗号隔开
            if (StringUtils.isNotBlank(fileUrl)) {
                log.warn("文件重复上传");
                if (Arrays.asList(FileConstant.VIDEO_FILE_EXTENSION).contains(fileSuffix)) {
                    String[] split = fileUrl.split(",");
                    result.put("fileUrl", split[0]);
                    result.put("videoCoverUrl", split[1]);
                    chatMessageService.saveFile(chatMessage, messageId, split[0], split[1]);
                } else {
                    result.put("fileUrl", fileUrl);
                    chatMessageService.saveFile(chatMessage, messageId, fileUrl, null);
                }
                return ResultUtils.success(result);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理文件失败");
        }
        //文件校验
        validFile(multipartFile, fileUploadBizEnum);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        if (chatMessage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断是否是当前用户发送的
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
            String videCoverPath = null;
            String videoCoverUrl = null;
            //如何上传的是视频就获取封面并且返回
            if (Arrays.asList(FileConstant.VIDEO_FILE_EXTENSION).contains(fileSuffix)) {
                //将视频的filepath的后缀改为.jpg
                videCoverPath = filepath.replaceAll("\\.[^.]+$", ".jpg");
                videoCoverUrl = FileConstant.COS_HOST + cosManager.generateSnapshot(filepath, videCoverPath);
                String fileUrlAndCoverUrl = fileUrl + "," + videoCoverUrl;
                redisUtils.set(REDIS_USER_UPLOAD_FILE_KEY + loginUser.getId() + ":" + md5, fileUrlAndCoverUrl, REDIS_USER_UPLOAD_FILE_EXPIRE_TIME, TimeUnit.MINUTES);
            }else {
                redisUtils.set(REDIS_USER_UPLOAD_FILE_KEY + loginUser.getId() + ":" + md5, fileUrl, REDIS_USER_UPLOAD_FILE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
            chatMessageService.saveFile(chatMessage, messageId, fileUrl, videoCoverUrl);
            result.put("fileUrl", fileUrl);
            result.put("videoCoverUrl", videoCoverUrl);
            return ResultUtils.success(result);
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
     * 根据 id 下载
     *
     * @return
     */
    @GetMapping("/msg/download")
    @SaCheckLogin
    public void downloadFile(Long messageId, HttpServletResponse response) throws IOException {
        ChatMessage chatMessage = chatMessageService.getById(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser();
        String filePath = chatMessage.getFilePath();
        String fileName = chatMessage.getFileName();
        filePath = filePath.replace(FileConstant.COS_HOST,"");
        COSObjectInputStream cosObjectInput = null;
        try {
            chatMessageService.checkFileAuth(loginUser, chatMessage);
            COSObject cosObject = cosManager.getObject(filePath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " +  filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
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
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小" + maxFileSize + "M");
            }
        }
    }

}
