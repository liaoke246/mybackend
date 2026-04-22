package com.example.demo.service;

import com.example.demo.model.request.MessageSaveRequest;
import com.example.demo.model.response.MessageResponse;
import com.example.demo.model.response.PageResponse;

public interface MessageBoardService {

    PageResponse<MessageResponse> listMessages(Integer pageNum, Integer pageSize);

    MessageResponse getMessage(Long id);

    MessageResponse createMessage(MessageSaveRequest request);

    MessageResponse updateMessage(Long id, MessageSaveRequest request);

    void deleteMessage(Long id);
}
