package com.xin.xinChat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.xinChat.model.dto.user.UserQueryRequest;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.vo.LoginUserVO;
import com.xin.xinChat.model.vo.UserVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;


/**
 * 用户服务
 *
 * @author <a href="https://github.com/liyupi">小新</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param email   用户邮箱
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    String userRegister(String email, String userPassword, String checkPassword,String checkCode,String checkCodeKey);


    /**
     * 用户登录
     *
     * @param Email  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String Email, String userPassword);



    /**
     * 获取当前登录用户
     *
     * @return
     */
    User getLoginUser();


    /**
     * 用户登录
     * @return
     */
    boolean userLogout();

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

}
