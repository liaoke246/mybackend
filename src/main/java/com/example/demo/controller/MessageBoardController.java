package com.example.demo.controller;

import com.example.demo.model.request.MessageSaveRequest;
import com.example.demo.model.response.ApiResponse;
import com.example.demo.model.response.MessageResponse;
import com.example.demo.model.response.PageResponse;
import com.example.demo.service.MessageBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
@Tag(name = "留言板接口")
public class MessageBoardController {

    private final MessageBoardService messageBoardService;

    @GetMapping
    @Operation(summary = "分页查询留言")
    public ApiResponse<PageResponse<MessageResponse>> listMessages(
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {
        return ApiResponse.success(messageBoardService.listMessages(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询留言详情")
    public ApiResponse<MessageResponse> getMessage(@PathVariable Long id) {
        return ApiResponse.success(messageBoardService.getMessage(id));
    }

    @PostMapping
    @Operation(summary = "新增留言")
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(@Valid @RequestBody MessageSaveRequest request) {
        MessageResponse response = messageBoardService.createMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("留言创建成功", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新留言")
    public ApiResponse<MessageResponse> updateMessage(@PathVariable Long id,
                                                      @Valid @RequestBody MessageSaveRequest request) {
        return ApiResponse.success("留言更新成功", messageBoardService.updateMessage(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除留言")
    public ApiResponse<Void> deleteMessage(@PathVariable Long id) {
        messageBoardService.deleteMessage(id);
        return ApiResponse.success("留言删除成功", null);
    }
}
