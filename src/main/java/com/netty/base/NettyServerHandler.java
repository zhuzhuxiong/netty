package com.netty.base;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.nio.charset.StandardCharsets;

/**
 * @author zzx
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 处理读取事件--读取来自客户端数据
     * @param ctx 包含 channel、pipeline
     * @param msg 来自客户端的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        ChannelPipeline pipeline = ctx.pipeline();
        System.out.println("Server read thread：" + Thread.currentThread().getName());
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("Server receive clinet msg:" + byteBuf.toString());

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Server read-complete thread：" + Thread.currentThread().getName());

        ByteBuf byteBuf = Unpooled.copiedBuffer("HelloClient".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
