package com.example.blogapi.dao.pojo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class Category {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String avatar;

    private String categoryName;

    private String description;
}
