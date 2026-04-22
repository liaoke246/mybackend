package com.example.demo.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageSaveRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    @Size(max = 128, message = "联系方式长度不能超过128个字符")
    private String contact;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容长度不能超过500个字符")
    private String content;
}
