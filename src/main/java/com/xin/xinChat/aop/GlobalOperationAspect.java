package com.xin.xinChat.aop;

import com.xin.xinChat.annotation.GlobalInterceptor;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/23 19:43
 */
@Aspect
@Component("globalOperationAspect")
@Slf4j
public class GlobalOperationAspect {
    @Resource
    private UserService userService;

    /**
     * 前置校验
     */
    @Before("@annotation(com.xin.xinChat.annotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint joinPoint) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            GlobalInterceptor globalInterceptor = method.getAnnotation(GlobalInterceptor.class);
            if (globalInterceptor == null) {
                return;
            }
            if (globalInterceptor.checkLogin() || globalInterceptor.checkAdmin()) {
                checkLogin(globalInterceptor.checkAdmin());
            }
        } catch (Exception e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    public boolean checkLogin(Boolean checkAdmin) {
        //获取全局的请求对象
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        //todo 根据token获取用户信息,后续业务处理根据自己的情况制定
        return false;
    }

}
