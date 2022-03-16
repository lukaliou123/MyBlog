package com.example.blogapi.controller;

import com.example.blogapi.service.LoginService;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("register")
public class RegisterController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public Result register(@RequestBody LoginParam loginParam){
        //sso 单点登录 后期如果把登陆注册功能 踢出去（单独的服务，可以独立提供接口服务）
        return loginService.register(loginParam);
    }
}
