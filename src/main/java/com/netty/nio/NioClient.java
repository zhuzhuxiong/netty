package com.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @author zzx
 */
public class NioClient {

    //多路复用
    private Selector selector;

    public static void main(String[] args) throws IOException {
        NioClient client = new NioClient();
        //连接服务端
        client.initClient("localhost", 9000);
        //处理事件
        client.connect();
    }


    private void initClient(String ip, int port) throws IOException {
        //客户端 通道，设置非阻塞
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        //开启多路复用器
        selector = Selector.open();
        //客户端连接服务器,需要再下述connect()中调用channel.finishConnect()才真正完成连接
        socketChannel.connect(new InetSocketAddress(ip, port));
        //channel注册OP_CONNECT事件
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    private void connect() throws IOException {
        //循环处理事件
        while (true) {
            int select = selector.select();
            Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                //链接事件
                if (selectionKey.isConnectable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    //真正实现连接
                    if (socketChannel.isConnectionPending()) {
                        socketChannel.finishConnect();
                    }
                    socketChannel.configureBlocking(false);
                    //向服务端发送消息
                    ByteBuffer byteBuffer = ByteBuffer.wrap("hello Server".getBytes(StandardCharsets.UTF_8));
                    socketChannel.write(byteBuffer);
                    //关注OP_READ事件，获取服务端消息
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    read(selectionKey);
                }
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(byteBuffer);
        if (read != -1) {
            System.out.println("client receive msg:" + new String(byteBuffer.array(), 0, read));
        }
    }
}
