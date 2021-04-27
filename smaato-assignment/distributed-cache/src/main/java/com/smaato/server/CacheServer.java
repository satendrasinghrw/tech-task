package com.smaato.server;

import com.smaato.codec.MessageDecoder;
import com.smaato.codec.MessageEncoder;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
public class CacheServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private final CacheManagerAPI cache;

    public CacheServer() {
        this.cache = new CacheManagerHandler();
    }

    public void start(int port) throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(1);
        final ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator())
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new MessageDecoder());
                        p.addLast(new ServerMessageHandler(cache));
                        p.addLast(new MessageEncoder());
                    }
                }).validate();

        ChannelFuture future = bootstrap.bind().sync();
        LOGGER.info("CacheServer is started at {}", future.channel().localAddress());
        channel = future.channel();
    }

    public void stop() {
        if (channel == null) {
            throw new IllegalStateException("CacheServer is not started.");
        }
        LOGGER.info("Stopping CacheServer");
        try {
            channel.close().sync();
            channel = null;
        } catch (InterruptedException e) {
            LOGGER.error("Error while stopping CacheServer", e);
        } finally {
            bossGroup.shutdownGracefully();
            bossGroup = null;
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }
}
