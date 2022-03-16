package com.example.blogapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.LoginService;
import com.example.blogapi.service.SysUserService;
import com.example.blogapi.util.JWTUtils;
import com.example.blogapi.vo.ErrorCode;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.LoginParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;


import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String slat = "mszlu!@#";

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 1.检查参数是否合法
         * 2.根据用户名和密码去user表中查询 是否存在
         * 3.如果不存在，登陆失败
         * 4.如果存在，使用jwt 生成token 返回给前端
         * 5.token放入redis,redis token:user信息 设置过期时间(登录认证的时候 先认证token字符串是否合法，去redis认证是否存在）
         *
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if(StringUtils.isBlank(account) || StringUtils.isBlank(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        //处理加密信息
        password = DigestUtils.md5Hex(password+slat);//此为真实密码
        SysUser sysUser = sysUserService.findUser(account,password);
        if(sysUser == null){
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        return Result.success(token);
    }
}
