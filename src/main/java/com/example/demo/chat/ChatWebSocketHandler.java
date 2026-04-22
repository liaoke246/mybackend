package com.example.demo.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ChannelHandler.Sharable
public class ChatWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final AttributeKey<UserSession> SESSION_KEY = AttributeKey.valueOf("chat.user.session");
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_NAME_LENGTH = 20;
    private static final int MAX_CONTENT_LENGTH = 500;

    private final String chatPath;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ObjectMapper objectMapper, ChatServerProperties properties) {
        this.chatPath = normalizePath(properties.getPath());
        this.objectMapper = objectMapper;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame frame) {
        JsonNode payload;
        try {
            payload = objectMapper.readTree(frame.text());
        } catch (JsonProcessingException exception) {
            return;
        }

        String type = readText(payload, "type");
        if (type == null) {
            return;
        }

        switch (type) {
            case "join" -> handleJoin(context.channel(), payload);
            case "message" -> handleMessage(context.channel(), payload);
            default -> {
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete) {
            if (matchesChatPath(handshakeComplete.requestUri())) {
                CHANNELS.add(context.channel());
                UserSession session = getOrCreateSession(context.channel());
                sendToChannel(context.channel(), Map.of(
                        "type", "init",
                        "userId", session.getUserId()
                ));
            }
            return;
        }
        super.userEventTriggered(context, event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        Channel channel = context.channel();
        UserSession session = channel.attr(SESSION_KEY).get();
        CHANNELS.remove(channel);
        if (session != null && session.hasJoined()) {
            broadcast(Map.of(
                    "type", "leave",
                    "userId", session.getUserId(),
                    "name", session.getName(),
                    "onlineCount", CHANNELS.size()
            ));
        }
        super.channelInactive(context);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        context.close();
    }

    private void handleJoin(Channel channel, JsonNode payload) {
        String name = readText(payload, "name");
        if (!isValidName(name)) {
            return;
        }

        UserSession session = getOrCreateSession(channel);
        if (session.hasJoined()) {
            return;
        }

        session.setName(name.trim());
        broadcast(Map.of(
                "type", "join",
                "userId", session.getUserId(),
                "name", session.getName(),
                "onlineCount", CHANNELS.size()
        ));
    }

    private void handleMessage(Channel channel, JsonNode payload) {
        UserSession session = channel.attr(SESSION_KEY).get();
        if (session == null || !session.hasJoined()) {
            return;
        }

        String content = readText(payload, "content");
        if (!isValidContent(content)) {
            return;
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "message");
        message.put("userId", session.getUserId());
        message.put("name", session.getName());
        message.put("content", content.trim());
        message.put("time", LocalTime.now().format(TIME_FORMATTER));
        broadcast(message);
    }

    private UserSession getOrCreateSession(Channel channel) {
        UserSession session = channel.attr(SESSION_KEY).get();
        if (session != null) {
            return session;
        }

        UserSession newSession = new UserSession(UUID.randomUUID().toString());
        channel.attr(SESSION_KEY).set(newSession);
        return newSession;
    }

    private void broadcast(Map<String, Object> payload) {
        CHANNELS.writeAndFlush(new TextWebSocketFrame(writeJson(payload)));
    }

    private void sendToChannel(Channel channel, Map<String, Object> payload) {
        channel.writeAndFlush(new TextWebSocketFrame(writeJson(payload)));
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize chat payload", exception);
        }
    }

    private boolean matchesChatPath(String requestUri) {
        return requestUri != null
                && (chatPath.equals(requestUri)
                || (chatPath + "/").equals(requestUri)
                || requestUri.startsWith(chatPath + "?"));
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/ws/chat";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String readText(JsonNode payload, String fieldName) {
        JsonNode node = payload.get(fieldName);
        if (node == null || !node.isTextual()) {
            return null;
        }
        return node.asText();
    }

    private boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.trim().length() <= MAX_NAME_LENGTH;
    }

    private boolean isValidContent(String content) {
        return content != null && !content.isBlank() && content.trim().length() <= MAX_CONTENT_LENGTH;
    }

}
