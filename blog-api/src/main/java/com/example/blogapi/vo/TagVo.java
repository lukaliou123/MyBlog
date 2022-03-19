package com.example.blogapi.vo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class TagVo {
    //防止前端 精度损失 把id转为string
    //@JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String tagName;

    private String avatar;
}
