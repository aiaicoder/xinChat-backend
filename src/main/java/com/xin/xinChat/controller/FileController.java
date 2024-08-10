package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.constant.FileConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.manager.CosManager;
import com.xin.xinChat.model.dto.file.UploadFileRequest;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.model.entity.ChatMessage;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.enums.FileUploadBizEnum;
import com.xin.xinChat.service.ChatMessageService;
import com.xin.xinChat.service.UserService;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xin.xinChat.utils.DateUtils;
import com.xin.xinChat.utils.SysSettingUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.xin.xinChat.constant.FileConstant.FILE_SIZE;

/**
 * 文件接口
 *
 * @author <a href="https://github.com/liyupi">小新</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;


    @Resource
    private SysSettingUtil sysSettingUtil;

    @Resource
    private ChatMessageService chatMessageService;


    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @return
     */
    @PostMapping("/upload")
    @SaCheckLogin
    @ApiOperation("上传头像")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser();
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        //格式化到月份
        String uploadDate = DateUtils.format(new Date(System.currentTimeMillis()), DateUtils.YYYYMM);
        String filepath = String.format("/%s/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId() , uploadDate , filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
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
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > maxUserAvatarSize * FILE_SIZE) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像大小不能超过" + maxUserAvatarSize + "M");
            }
            if (!Arrays.asList(FileConstant.IMAGE_FILE_EXTENSION).contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
