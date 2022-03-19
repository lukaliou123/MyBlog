package com.example.blogapi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class UserVo {

    private String nickname;

    private String avatar;
    //防止前端 精度损失 把id转为string
    //@JsonSerialize(using = ToStringSerializer.class)
    private String id;
}