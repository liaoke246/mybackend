package com.example.demo.mapper;

import com.example.demo.model.entity.MessageEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageBoardMapper {

    @Results(id = "messageEntityResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "contact", column = "contact"),
            @Result(property = "content", column = "content"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("""
            SELECT id, nickname, contact, content, created_at, updated_at
            FROM board_message
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<MessageEntity> selectMessages(@Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            SELECT id, nickname, contact, content, created_at, updated_at
            FROM board_message
            WHERE id = #{id}
            """)
    @ResultMap("messageEntityResultMap")
    MessageEntity selectById(@Param("id") Long id);

    @Select("""
            SELECT COUNT(1)
            FROM board_message
            """)
    long countMessages();

    @Insert("""
            INSERT INTO board_message (nickname, contact, content, created_at, updated_at)
            VALUES (#{nickname}, #{contact}, #{content}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(MessageEntity message);

    @Update("""
            UPDATE board_message
            SET nickname = #{nickname},
                contact = #{contact},
                content = #{content},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateMessage(MessageEntity message);

    @Delete("""
            DELETE FROM board_message
            WHERE id = #{id}
            """)
    int deleteById(@Param("id") Long id);
}
