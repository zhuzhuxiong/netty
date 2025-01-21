package com.netty.im;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author
 */
public class NettyImServerHandler extends SimpleChannelInboundHandler<String> {

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        channelGroup.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + "已上线:" + sdf.format(new Date()));
        channelGroup.add(channel);

        System.out.println("[ 客户端 ]" + channel.remoteAddress() + "已上线:" + sdf.format(new Date()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        channelGroup.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + "已下线:" + sdf.format(new Date()));

        System.out.println("[ 客户端 ]" + channel.remoteAddress() + "已下线:" + sdf.format(new Date()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        Channel ctxChannel = channelHandlerContext.channel();

        channelGroup.forEach(channel -> {
            if (channel == ctxChannel) {
                channel.writeAndFlush(sdf.format(new Date()) + ":[自己] 发送了消息：" + msg);
            }else {
                channel.writeAndFlush(sdf.format(new Date()) + ":[ 客户端 ]" + channel.remoteAddress() + "发送了消息：" + msg + "\n");
            }
        });
    }
}
