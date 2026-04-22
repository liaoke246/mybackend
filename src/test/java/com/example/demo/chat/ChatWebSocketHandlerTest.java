package com.example.demo.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatWebSocketHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatServerProperties properties = defaultProperties();

    @Test
    void shouldSendInitJoinAndMessage() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new ChatWebSocketHandler(objectMapper, properties));

        channel.pipeline().fireUserEventTriggered(
                new WebSocketServerProtocolHandler.HandshakeComplete("/ws/chat", EmptyHttpHeaders.INSTANCE, null)
        );

        TextWebSocketFrame initFrame = channel.readOutbound();
        assertNotNull(initFrame);
        JsonNode initPayload = readPayload(initFrame);
        assertEquals("init", initPayload.get("type").asText());
        String userId = initPayload.get("userId").asText();
        assertNotNull(userId);
        initFrame.release();

        channel.writeInbound(new TextWebSocketFrame("{\"type\":\"join\",\"name\":\"张三\"}"));
        TextWebSocketFrame joinFrame = channel.readOutbound();
        assertNotNull(joinFrame);
        JsonNode joinPayload = readPayload(joinFrame);
        assertEquals("join", joinPayload.get("type").asText());
        assertEquals(userId, joinPayload.get("userId").asText());
        assertEquals("张三", joinPayload.get("name").asText());
        assertEquals(1, joinPayload.get("onlineCount").asInt());
        joinFrame.release();

        channel.writeInbound(new TextWebSocketFrame("{\"type\":\"message\",\"content\":\"你好大家\"}"));
        TextWebSocketFrame messageFrame = channel.readOutbound();
        assertNotNull(messageFrame);
        JsonNode messagePayload = readPayload(messageFrame);
        assertEquals("message", messagePayload.get("type").asText());
        assertEquals(userId, messagePayload.get("userId").asText());
        assertEquals("张三", messagePayload.get("name").asText());
        assertEquals("你好大家", messagePayload.get("content").asText());
        assertNotNull(messagePayload.get("time").asText());
        messageFrame.release();

        channel.finishAndReleaseAll();
    }

    @Test
    void shouldIgnoreMalformedAndUnknownMessages() {
        EmbeddedChannel channel = new EmbeddedChannel(new ChatWebSocketHandler(objectMapper, properties));

        channel.pipeline().fireUserEventTriggered(
                new WebSocketServerProtocolHandler.HandshakeComplete("/ws/chat", EmptyHttpHeaders.INSTANCE, null)
        );

        TextWebSocketFrame initFrame = channel.readOutbound();
        assertNotNull(initFrame);
        initFrame.release();

        channel.writeInbound(new TextWebSocketFrame("{bad json"));
        assertNull(channel.readOutbound());

        channel.writeInbound(new TextWebSocketFrame("{\"type\":\"unknown\"}"));
        assertNull(channel.readOutbound());

        channel.finishAndReleaseAll();
    }

    private JsonNode readPayload(TextWebSocketFrame frame) throws Exception {
        return objectMapper.readTree(frame.text());
    }

    private ChatServerProperties defaultProperties() {
        ChatServerProperties chatServerProperties = new ChatServerProperties();
        chatServerProperties.setPath("/ws/chat");
        return chatServerProperties;
    }

}
