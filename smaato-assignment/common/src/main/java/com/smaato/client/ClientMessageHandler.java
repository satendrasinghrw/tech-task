package com.smaato.client;

import com.smaato.common.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.channels.Pipe;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;

public class ClientMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final BlockingQueue<ByteBuf> queue;

    public ClientMessageHandler(BlockingQueue<ByteBuf> queue) {
        this.queue = queue;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if(msg != null) {
            ByteBuf buf = ctx.alloc().heapBuffer(Constant.MSG_BYTES);
            buf.writeLong(msg.readLong());
            buf.writeInt(msg.readInt());
            queue.put(buf);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        State.INSTANCE.serverActive =  true;
    }
}
