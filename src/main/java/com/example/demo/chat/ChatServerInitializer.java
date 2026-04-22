package com.example.demo.chat;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Component;

@Component
public class ChatServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ChatServerProperties properties;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatServerInitializer(ChatServerProperties properties, ChatWebSocketHandler chatWebSocketHandler) {
        this.properties = properties;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new ChunkedWriteHandler())
                .addLast(new WebSocketServerProtocolHandler(properties.getPath(), null, true))
                .addLast(chatWebSocketHandler);
    }

}
