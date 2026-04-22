package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MessageBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListSeedMessages() throws Exception {
        mockMvc.perform(get("/api/messages")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list[0].id").isNumber());
    }

    @Test
    void shouldCreateMessage() throws Exception {
        String requestBody = """
                {
                  "nickname": "David",
                  "contact": "david@example.com",
                  "content": "后端接口已经准备好了，可以开始联调。"
                }
                """;

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("留言创建成功"))
                .andExpect(jsonPath("$.data.nickname").value("David"))
                .andExpect(jsonPath("$.data.content").value("后端接口已经准备好了，可以开始联调。"));
    }

    @Test
    void shouldAllowCorsPreflightForFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/messages")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
