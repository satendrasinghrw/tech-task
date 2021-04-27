package com.smaato.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.smaato.common.Constant.MSG_BYTES;

public class MessageDecoder extends ByteToMessageDecoder {
    public MessageDecoder() {
        setSingleDecode(true);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() >= MSG_BYTES) {
            final int noOfTimesToBeRead = in.readableBytes()/ MSG_BYTES;
            for (int i=0; i<noOfTimesToBeRead; i++) {
                ByteBuf buf = in.readRetainedSlice(MSG_BYTES);
                out.add(buf);
            }
        }
    }
}
