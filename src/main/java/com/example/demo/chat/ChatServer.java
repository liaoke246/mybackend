package com.example.demo.chat;

import com.example.demo.DemoApplication;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "chat.server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChatServer implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    private final ChatServerProperties properties;
    private final ChatServerInitializer chatServerInitializer;

    private volatile boolean running;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public ChatServer(ChatServerProperties properties, ChatServerInitializer chatServerInitializer) {
        this.properties = properties;
        this.chatServerInitializer = chatServerInitializer;
    }

    public static void main(String[] args) {
        DemoApplication.main(args);
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(chatServerInitializer);

            serverChannel = bootstrap.bind(properties.getPort()).sync().channel();
            running = true;
            startCloseWatcher();
            log.info("Chat WebSocket server started on port {} with path {}", properties.getPort(), properties.getPath());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            shutdownEventLoops();
            throw new IllegalStateException("Interrupted while starting chat server", exception);
        } catch (Exception exception) {
            shutdownEventLoops();
            throw new IllegalStateException("Failed to start chat server", exception);
        }
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }

        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
        } finally {
            shutdownEventLoops();
            running = false;
            log.info("Chat WebSocket server stopped");
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    private void startCloseWatcher() {
        Thread.ofPlatform().name("chat-server-close-watcher").daemon(true).start(() -> {
            try {
                if (serverChannel != null) {
                    serverChannel.closeFuture().sync();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                synchronized (ChatServer.this) {
                    shutdownEventLoops();
                    running = false;
                }
            }
        });
    }

    private synchronized void shutdownEventLoops() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
            workerGroup = null;
        }
        serverChannel = null;
    }

}
