package com.example.blogapi.service;

import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.LoginParam;

public interface LoginService {

    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);
}
