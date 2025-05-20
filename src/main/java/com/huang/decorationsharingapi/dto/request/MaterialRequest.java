package com.huang.decorationsharingapi.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class MaterialRequest {
    @NotBlank(message = "素材标题不能为空")
    @Size(min = 3, max = 100, message = "标题长度应在3-100个字符之间")
    private String title;

    @Size(min = 3, max = 1000, message = "描述长度应在3-1000个字符之间")
    private String description;

    @NotNull(message = "请选择分类")
    private Long categoryId;

    private List<String> tags;

    private String license = "own";
}