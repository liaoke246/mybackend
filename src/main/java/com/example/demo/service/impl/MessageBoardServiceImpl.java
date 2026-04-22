package com.example.demo.service.impl;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.MessageBoardMapper;
import com.example.demo.model.entity.MessageEntity;
import com.example.demo.model.request.MessageSaveRequest;
import com.example.demo.model.response.MessageResponse;
import com.example.demo.model.response.PageResponse;
import com.example.demo.service.MessageBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageBoardServiceImpl implements MessageBoardService {

    private final MessageBoardMapper messageBoardMapper;

    @Override
    public PageResponse<MessageResponse> listMessages(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<MessageResponse> list = messageBoardMapper.selectMessages(offset, pageSize).stream()
                .map(MessageResponse::fromEntity)
                .toList();
        long total = messageBoardMapper.countMessages();
        return PageResponse.<MessageResponse>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .list(list)
                .build();
    }

    @Override
    public MessageResponse getMessage(Long id) {
        return MessageResponse.fromEntity(requireMessage(id));
    }

    @Override
    @Transactional
    public MessageResponse createMessage(MessageSaveRequest request) {
        MessageEntity entity = MessageEntity.builder()
                .nickname(request.getNickname())
                .contact(request.getContact())
                .content(request.getContent())
                .build();
        messageBoardMapper.insertMessage(entity);
        return getMessage(entity.getId());
    }

    @Override
    @Transactional
    public MessageResponse updateMessage(Long id, MessageSaveRequest request) {
        requireMessage(id);
        MessageEntity entity = MessageEntity.builder()
                .id(id)
                .nickname(request.getNickname())
                .contact(request.getContact())
                .content(request.getContent())
                .build();
        messageBoardMapper.updateMessage(entity);
        return getMessage(id);
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        requireMessage(id);
        messageBoardMapper.deleteById(id);
    }

    private MessageEntity requireMessage(Long id) {
        MessageEntity entity = messageBoardMapper.selectById(id);
        if (entity == null) {
            throw new ResourceNotFoundException("留言不存在，id=" + id);
        }
        return entity;
    }
}
