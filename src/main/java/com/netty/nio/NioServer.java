package com.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 *
 * @author zzx
 */
public class NioServer {

    public static void main(String[] args) throws IOException {
        //创建serverSocketChannel 设置为非阻塞
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //Selector模式为非阻塞，所以必须设置为false非阻塞，否则报错
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(9000));

        //创建多路复用 selector
        Selector selector = Selector.open();
        //serverSocketChannel 注册到 selector，并关注OP_ACCEPT事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            System.out.println("wait event...");
            //循环监听selectedKeys，select()阻塞
            int select = selector.select();

            System.out.println("event occurrence...");

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            //select()向下走说明有事件发生，循环处理
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                //删除已经在处理的key
                iterator.remove();
                handle(selectionKey);
            }
        }

    }

    private static void handle(SelectionKey selectionKey) throws IOException {
        //服务端：客户端连接事件； OP_CONNECT为客户端关注的事件
        if (selectionKey.isAcceptable()) {
            System.out.println("client connect event...");
            //selectionKey为注册的对应channel，所以可以根据需要强转
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            /**
             * NIO非阻塞体现：此处accept方法是阻塞的，
             * 但是这里因为是发生了连接事件，所以这个方法会马上执行完，不会阻塞
                处理完连接请求不会继续等待客户端的数据发送*/
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            //向selector注册channel关注的事件
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
        }else if (selectionKey.isReadable()) {
            System.out.println("client readable event...");
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            //分配一块新的的ByteBuffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            /**NIO非阻塞体现: 首先read方法不会阻塞，其次这种事件响应模型，
             * 当调用到read方法时肯定是发生了客户端发送数据的事件*/
            int read = socketChannel.read(byteBuffer);
            if (read != -1) {
                System.out.println("server read client msg:" + new String(byteBuffer.array(),0, read));
            }
            ByteBuffer bufferWrite = ByteBuffer.wrap("hello client".getBytes(StandardCharsets.UTF_8));
            socketChannel.write(bufferWrite);
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }else if (selectionKey.isWritable()) {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            // NIO事件触发是水平触发
            // 使用Java的NIO编程的时候，在没有数据可以往外写的时候要取消写事件，
            // 在有数据往外写的时候再注册写事件
            System.out.println("server write event");
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }
}
