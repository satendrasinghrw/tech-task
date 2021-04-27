package com.smaato.server;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import com.koloboke.collect.map.hash.HashLongIntMap;
import com.koloboke.collect.map.hash.HashLongIntMaps;
import com.koloboke.collect.set.hash.HashLongSet;
import com.koloboke.collect.set.hash.HashLongSets;
import com.koloboke.function.IntObjPredicate;
import com.smaato.common.Constant;
import com.smaato.common.Event;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.smaato.common.Constant.CACHE_CLEARED;


public final class ServerMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerMessageHandler.class);
    private final CacheManagerAPI cache;

    public ServerMessageHandler(CacheManagerAPI cache) {
        this.cache = cache;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if(msg == null) {
            LOG.info("no message at this time");
            return;
        }

        final long queryId = msg.readLong();
        final int appId = msg.readInt();

        ByteBuf buf = ctx.alloc().heapBuffer(Constant.MSG_BYTES);
        buf.writeLong(queryId);
        if (Event.CLEAN_CACHE == queryId) {
            cache.clean(queryId, appId);
            buf.writeInt(CACHE_CLEARED);
        } else {
            int ret = cache.checkAndUpdate(queryId, appId);
            buf.writeInt(ret);
        }
        ctx.channel().writeAndFlush(buf);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        LOG.info("Client connected: {}", ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.warn("Closing connection for client: {}, exception: {} ",  ctx, cause);
        ctx.close();
    }
}
