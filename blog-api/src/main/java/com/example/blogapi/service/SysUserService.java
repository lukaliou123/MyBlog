package com.example.blogapi.service;

import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.vo.Result;

public interface SysUserService {

    SysUser findUserById(Long id);

    SysUser findUser(String account, String password);

    /**
     * 根据Token查询用户信息
     * @param token
     * @return
     */
    Result findUserByToken(String token);
}
