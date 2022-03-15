package com.example.blogapi.service.impl;

import com.example.blogapi.dao.mapper.SysUserMapper;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if(sysUser == null){
            sysUser = new SysUser();
            sysUser.setNickname("蓝路");
        }
        return sysUser;
    }
}
