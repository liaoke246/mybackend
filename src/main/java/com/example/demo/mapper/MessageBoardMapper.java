package com.example.demo.mapper;

import com.example.demo.model.entity.MessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageBoardMapper {

    List<MessageEntity> selectMessages(@Param("offset") int offset, @Param("limit") int limit);

    MessageEntity selectById(@Param("id") Long id);

    long countMessages();

    int insertMessage(MessageEntity message);

    int updateMessage(MessageEntity message);

    int deleteById(@Param("id") Long id);
}
