package com.smaato.client;

import com.smaato.codec.MessageDecoder;
import com.smaato.codec.MessageEncoder;
import com.smaato.common.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.smaato.common.Constant.NON_DUPLICATE;

public class CacheClient implements DistCacheClient {
    private static final Logger LOG = LoggerFactory.getLogger(CacheClient.class);
    private static final int EVENT_LOOP_THREAD = 1;

    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final String host;
    private final int port;
    private final BlockingQueue<ByteBuf> queue;

    public CacheClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.queue = new ArrayBlockingQueue<>(1);
    }

    @Override
    public void start() throws InterruptedException {
        LOG.info("CacheClient is starting");
        final Bootstrap b = new Bootstrap();
        bossGroup = new NioEventLoopGroup(EVENT_LOOP_THREAD);
        workerGroup = new NioEventLoopGroup(EVENT_LOOP_THREAD);
        b.group(bossGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new MessageEncoder());
                        p.addLast(new MessageDecoder());
                        p.addLast(new ClientMessageHandler(queue));
                    }
                }).validate();
        final ChannelFuture connectFuture = b.connect().sync().await();
        channel = connectFuture.channel();
        channel.closeFuture();
        LOG.info("CacheClient is started at {}", channel.remoteAddress());
    }

    @Override
    public void stop() {
        if (channel == null) {
            throw new IllegalStateException("CacheClient is not started.");
        }
        LOG.info("Stopping CacheClient");
        try {
            channel.close().sync().await();
            channel = null;
        } catch (InterruptedException e) {
            LOG.error("Error while stopping CacheClient", e);
        } finally {
            bossGroup.shutdownGracefully();
            bossGroup = null;
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }

    @Override
    public boolean deduplication(long queryId, int appId) throws InterruptedException {
        send(queryId, appId);
        ByteBuf buf = queue.take();
        long repQueryId = buf.readLong();
        int duplication = buf.readInt();
        return queryId == repQueryId && duplication == NON_DUPLICATE ? false : true;
    }

    private void send(long queryId, int appId) {
        ByteBuf buf = channel.alloc().heapBuffer(Constant.MSG_BYTES);
        buf.writeLong(queryId);
        buf.writeInt(appId);
        channel.writeAndFlush(buf);
    }
}
