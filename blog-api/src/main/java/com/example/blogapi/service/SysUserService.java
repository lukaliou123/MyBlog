package com.example.blogapi.service;

import com.example.blogapi.dao.pojo.SysUser;

public interface SysUserService {

    SysUser findUserById(Long id);
}
