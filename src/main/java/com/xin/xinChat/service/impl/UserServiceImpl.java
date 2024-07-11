package com.xin.xinChat.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.config.AppConfig;
import com.xin.xinChat.constant.CommonConstant;
import com.xin.xinChat.constant.RedisKeyConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UserMapper;
import com.xin.xinChat.model.dto.user.UserQueryRequest;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserBeauty;
import com.xin.xinChat.model.enums.*;
import com.xin.xinChat.model.vo.LoginUserVO;
import com.xin.xinChat.model.vo.UserVO;
import com.xin.xinChat.service.UserService;
import com.xin.xinChat.utils.SqlUtils;
import com.xin.xinChat.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.xin.xinChat.constant.UserConstant.*;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/liyupi">小新</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {




    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserBeautyServiceImpl userBeautyService;


    @Resource
    private AppConfig appConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String userRegister(String email, String userPassword, String checkPassword, String checkCode, String checkCodeKey) {
        // 1. 校验
        if (StringUtils.isAnyBlank(email, userPassword, checkPassword, checkCode, checkCodeKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (!checkCode.equals(stringRedisTemplate.opsForValue().get(RedisKeyConstant.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
            log.error("checkCodeKey:{}", checkCodeKey);
            stringRedisTemplate.delete(RedisKeyConstant.REDIS_KEY_CHECK_CODE + checkCodeKey);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片验证码错误");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8 || checkPassword.length() > 32 || userPassword.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //检测账号是否包含特殊字符
        String validPattern = "^([a-z0-9A-Z]+[-|\\\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\\\.)+[a-zA-Z]{2,}$";
        Matcher matcher = Pattern.compile(validPattern).matcher(email);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法邮箱");
        }
        synchronized (email.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱重复");
            }
            //生成用户id
            String userId = StringUtil.getUserId();
            //查询靓号
            QueryWrapper<UserBeauty> beautyQueryWrapper = new QueryWrapper<>();
            beautyQueryWrapper.eq("email", email);
            UserBeauty userBeauty = userBeautyService.getOne(beautyQueryWrapper);
            //首先判断是否是靓号，并且是未使用
            boolean useBeautyAccount = userBeauty != null && userBeauty.getStatus().equals(BeautyAccountStatusEnum.NO_USE.getStatus());
            if (useBeautyAccount) {
                //如果改邮箱分配了靓号，就将生成的id切换为靓号Id
                userId = UserContactEnum.USER.getPrefix() + userBeauty.getUserid();
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            Date curTime = new Date();
            User user = new User();
            user.setId(userId);
            user.setUserPassword(encryptPassword);
            //插入默认头像和默认姓名
            user.setUserAvatar(DEFAULT_AVATAR);
            user.setUserName(DEFAULT_USERNAME);
            user.setEmail(email);
            user.setLastOffTime(curTime.getTime());
            user.setJoinType(JoinTypeEnum.APPLY.getType());
            user.setSex(1);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            if (useBeautyAccount) {
                UserBeauty updataUserBeauty = new UserBeauty();
                updataUserBeauty.setStatus(BeautyAccountStatusEnum.USING.getStatus());
                updataUserBeauty.setId(userBeauty.getId());
                userBeautyService.updateById(updataUserBeauty);
            }
            //TODO 创建机器人好友
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String email, String userPassword, String checkCode, String checkCodeKey) {
        // 1. 校验
        if (StringUtils.isAnyBlank(email, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号密码为空");
        }
        if (email.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        if (!checkCode.equals(stringRedisTemplate.opsForValue().get(RedisKeyConstant.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
            log.error("checkCodeKey:{}", checkCodeKey);
            stringRedisTemplate.delete(RedisKeyConstant.REDIS_KEY_CHECK_CODE + checkCodeKey);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片验证码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, email cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        } else if (user.getUserStatus().equals(UserStatusEnum.DISABLE.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已被封禁");
        }
        StpUtil.login(user.getId());
        // 3. 记录用户的登录态
        StpUtil.getSession().set(USER_LOGIN_STATE, user);
        //设置token，返回给前端
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        user.setToken(tokenInfo.getTokenValue());
        String adminEmail = appConfig.getAdminEmail();
        String userEmail = user.getEmail();
        //如果有配置中的管理员邮箱那么就给改用户设置管理员，前提是该用户之前不是管理员
        if (ArrayUtil.contains(adminEmail.split(","), userEmail) && !user.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
            user.setUserRole(UserRoleEnum.ADMIN.getValue());
        }
        //todo 查询我的联系人
        //todo 查询我的群组
        return this.getLoginUserVO(user);
    }



    @Override
    public User getLoginUser() {
        // 先判断是否已登录
        try {
            // 先判断是否已登录
            Object userObj = StpUtil.getSession().get(USER_LOGIN_STATE);
            User currentUser = (User) userObj;
            if (currentUser == null || currentUser.getId() == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            return currentUser;
        } catch (NotLoginException e) {
            // 根据 NotLoginException 的类型返回不同的异常信息
            switch (e.getType()) {
                case NotLoginException.NOT_TOKEN:
                    throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, ErrorCode.NOT_LOGIN_ERROR.getMessage());
                case NotLoginException.INVALID_TOKEN:
                    throw new BusinessException(ErrorCode.INVALID_TOKEN_ERROR, ErrorCode.INVALID_TOKEN_ERROR.getMessage());
                case NotLoginException.TOKEN_TIMEOUT:
                    throw new BusinessException(ErrorCode.TOKEN_TIMEOUT_MESSAGE, ErrorCode.TOKEN_TIMEOUT_MESSAGE.getMessage());
                case NotLoginException.BE_REPLACED:
                    throw new BusinessException(ErrorCode.BE_REPLACED_MESSAGE, ErrorCode.BE_REPLACED_MESSAGE.getMessage());
                case NotLoginException.KICK_OUT:
                    throw new BusinessException(ErrorCode.KICK_OUT_ERROR, ErrorCode.KICK_OUT_ERROR.getMessage());
                case NotLoginException.TOKEN_FREEZE:
                    throw new BusinessException(ErrorCode.TOKEN_FREEZE_ERROR, ErrorCode.TOKEN_FREEZE_ERROR.getMessage());
            }
        }
        return null;
    }


    /**
     * 用户登陆注销(通过框架)
     * @return
     */
    @Override
    public boolean userLogout() {
        if (StpUtil.getLoginIdDefaultNull() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        StpUtil.logout();
        //todo 关闭ws连接
        return true;
    }


    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        loginUserVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String email  = userQueryRequest.getEmail();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(email), "email", email);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(User loginUser) {
        String userId = loginUser.getId();
        //先查询不会锁行，如果先进行更新操作会锁行，这样效率就会慢
        User dbUser = getById(userId);
        boolean b = updateById(loginUser);
        String contactName = null;
        if (dbUser.getUserName().equals(loginUser.getUserName())) {
            contactName = loginUser.getUserName();
        }
        //todo 更新会话信息中的昵称信息
        return b;
    }

    /**
     * 强制踢出
     * @param userId
     */
    @Override
    public void forceKickOut(String userId) {
        StpUtil.kickout(userId);
        //todo 发送消息通知
    }

    /**
     * 判断是否是本人
     * @param userId
     * @return
     */
    @Override
    public boolean isOneSelf(String userId) {
        User loginUser = getLoginUser();
        return loginUser.getId().equals(userId);
    }

}
