package com.example.blogapi.handler;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.LoginService;
import com.example.blogapi.util.UserThreadLocal;
import com.example.blogapi.vo.ErrorCode;
import com.example.blogapi.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在执行controller方法（Handler)之前进行执行
        /**
         * 1.需要判断 请求的接口路径 是否为HandlerMethod(controller方法）
         * 2.判断token是否为空，如果为空 未登录
         * 3.如果token不为空，登陆验证
         * 4.如果认证成功 放行即可
         */
        if(!(handler instanceof HandlerMethod)){
            //handler 可能是 RequestResourceHandler Springboot 程序 访问静态资源 默认去classpath下的Static目录去查询

            return true;
        }
        String token = request.getHeader("Authorization");

        //加入日志，查看请求的uri和方，token

        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");

        if(StringUtils.isBlank(token)){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }

        SysUser sysUser = loginService.checkToken(token);
        if(sysUser == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        //登录验证成功 放行
        //我希望在controller中直接获取用户的信息，怎么获取？
        UserThreadLocal.put(sysUser);
        return true;
    }
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
//    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //如果不删除 ThreadLocal中用完的信息 会有内存泄露的风险
        UserThreadLocal.remove();
    }
}
