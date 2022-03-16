package com.example.blogapi.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

@Data
public class SysUser {

//    @TableId(type = IdType.ASSIGN_ID)//默认ID类型
    //以后  用户多了要进行分表操作，Id需要用分布式ID
//    @TableId(type = IdType.AUTO) //数据库自增
    private Long id;

    private String account;

    private Integer admin;

    private String avatar;

    private Long createDate;

    private Integer deleted;

    private String email;

    private Long lastLogin;

    private String mobilePhoneNumber;

    private String nickname;

    private String password;

    private String salt;

    private String status;
}